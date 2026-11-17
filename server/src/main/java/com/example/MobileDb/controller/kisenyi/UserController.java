package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.User;
import com.example.MobileDb.repository.kisenyi.UserRepository;
import com.example.MobileDb.service.BiometricService;
import com.example.MobileDb.dto.kisenyi.LoginRequest;
import com.example.MobileDb.dto.kisenyi.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BiometricService biometricService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Get all active users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getAccessStatus() != null && user.getAccessStatus() == 1)
                .toList();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Register new user (default password = "1234")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setPasswordHash(encoder.encode("1234"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setBiometricEnabled(false);
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Authenticate user
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Logging incoming request values (never the password, hash or biometric token)
            logger.info("Login attempt for username: {}", loginRequest.getUsername());
            logger.debug("Biometric flag: {}", loginRequest.isBiometricLogin());
            logger.debug("DeviceId received: {}", loginRequest.getDeviceId());
            logger.debug("BiometricKeyAlias received: {}", loginRequest.getBiometricKeyAlias());

            // Enforce access_status (admins are always active, but still need a valid password or biometric)
            boolean isAdmin = Boolean.TRUE.equals(user.getIsAdmin());
            if (!isAdmin && (user.getAccessStatus() == null || user.getAccessStatus() != 1)) {
                logger.warn("Access denied for inactive user: {}", user.getUsername());
                return ResponseEntity.status(403).body(Map.of(
                        "status", "false",
                        "code", "40301",
                        "message", "Access denied: inactive user"
                ));
            }

            // Fingerprint login: the phone signs a time-stamped message with its fingerprint-locked key
            if (loginRequest.isBiometricLogin()) {
                logger.info("Fingerprint login requested for user: {}", user.getUsername());

                if (!Boolean.TRUE.equals(user.getBiometricEnabled()) || user.getBiometricPublicKey() == null) {
                    return ResponseEntity.status(403).body(Map.of(
                            "status", "false",
                            "code", "40302",
                            "message", "Fingerprint login is not set up for this profile"
                    ));
                }

                if (user.getDeviceId() == null || !user.getDeviceId().equals(loginRequest.getDeviceId())) {
                    logger.warn("Fingerprint login from another device for user: {}", user.getUsername());
                    return ResponseEntity.status(403).body(Map.of(
                            "status", "false",
                            "code", "40303",
                            "message", "Fingerprint login only works on the phone where it was set up"
                    ));
                }

                if (!biometricService.verifyLogin(user, loginRequest.getDeviceId(),
                        loginRequest.getBiometricTimestamp(), loginRequest.getBiometricToken())) {
                    return ResponseEntity.status(401).body(Map.of(
                            "status", "false",
                            "code", "401",
                            "message", "Fingerprint could not be verified"
                    ));
                }

                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                logger.info("Fingerprint login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(Map.of(
                        "status", "true",
                        "code", "200",
                        "message", "Biometric login successful"
                ));
            } else {
                if (encoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                    logger.info("Password login successful for user: {}", user.getUsername());
                    return ResponseEntity.ok(Map.of(
                            "status", "true",
                            "code", "200",
                            "message", "Login successful"
                    ));
                } else {
                    logger.error("Password login failed for user: {}", user.getUsername());
                }
            }
        }

        logger.error("Invalid credentials for username: {}", loginRequest.getUsername());
        return ResponseEntity.status(401).body(Map.of(
                "status", "false",
                "code", "401",
                "message", "Invalid credentials"
        ));
    }

    // Set up fingerprint login: store the phone's public key after checking the user's password
    @PostMapping("/{id}/enable-biometrics")
    public ResponseEntity<?> enableBiometrics(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {

        Optional<User> found = userRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("status", "false", "code", "404", "message", "User not found"));
        }
        User user = found.get();

        String deviceId = payload.get("deviceId");
        String biometricKeyAlias = payload.get("biometricKeyAlias");
        String publicKeyBase64 = payload.get("publicKey");
        String password = payload.get("password");

        if (deviceId == null || biometricKeyAlias == null || publicKeyBase64 == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "false",
                    "code", "400",
                    "message", "Missing deviceId, biometricKeyAlias or publicKey"
            ));
        }

        // Only the profile owner may attach a fingerprint key to it
        if (password == null || !encoder.matches(password, user.getPasswordHash())) {
            logger.warn("Fingerprint setup refused for {}: wrong password", user.getUsername());
            return ResponseEntity.status(401).body(Map.of(
                    "status", "false",
                    "code", "401",
                    "message", "Password is incorrect"
            ));
        }

        try {
            user.setBiometricPublicKey(biometricService.normalisePublicKey(publicKeyBase64));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "false",
                    "code", "400",
                    "message", "The phone sent an invalid fingerprint key"
            ));
        }
        user.setBiometricEnabled(true);
        user.setDeviceId(deviceId);
        user.setBiometricKeyAlias(biometricKeyAlias);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Fingerprint login set up for user: {}", user.getUsername());
        return ResponseEntity.ok(Map.of(
                "status", "true",
                "code", "200",
                "message", "Fingerprint login is on",
                "deviceId", deviceId,
                "biometricKeyAlias", biometricKeyAlias
        ));
    }

    // Turn fingerprint login off (body: {"enabled": false}); turning it on goes through enable-biometrics
    @PutMapping("/{id}/biometric-status")
    public ResponseEntity<?> updateBiometricStatus(@PathVariable Integer id, @RequestBody Map<String, Boolean> payload) {
        Optional<User> found = userRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("status", "false", "code", "404", "message", "User not found"));
        }
        User user = found.get();
        if (Boolean.TRUE.equals(payload.get("enabled"))) {
            if (user.getBiometricPublicKey() == null) {
                return ResponseEntity.status(409).body(Map.of("status", "false", "code", "409",
                        "message", "Set up fingerprint login on the phone first"));
            }
            user.setBiometricEnabled(true);
        } else {
            user.setBiometricEnabled(false);
            user.setBiometricPublicKey(null);
            user.setBiometricKeyAlias(null);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Fingerprint login {} for user: {}", Boolean.TRUE.equals(user.getBiometricEnabled()) ? "on" : "off", user.getUsername());
        return ResponseEntity.ok(Map.of("status", "true", "code", "200",
                "message", Boolean.TRUE.equals(user.getBiometricEnabled()) ? "Fingerprint login is on" : "Fingerprint login is off"));
    }


            		
            		
            		// Reset password
            		@PostMapping("/{id}/set-password")
            		public ResponseEntity<?> setPassword(@PathVariable Integer id, @RequestBody PasswordResetRequest request) {
            		    if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            		        logger.warn("Password reset failed: empty password for user {}", id);
            		        return ResponseEntity.badRequest().body(Map.of(
            		            "status", "false",
            		            "code", "400",
            		            "message", "New password cannot be null or empty"
            		        ));
            		    }

            		    User user = userRepository.findById(id).orElseThrow();

            		    logger.info("Resetting password for user {}", id);

            		    String hashed = encoder.encode(request.getNewPassword());
            		    user.setPasswordHash(hashed);
            		    user.setUpdatedAt(LocalDateTime.now());

            		    userRepository.saveAndFlush(user);

            		    logger.info("Password successfully updated for user {}", id);

            		    return ResponseEntity.ok(Map.of(
            		        "status", "true",
            		        "code", "200",
            		        "message", "Password updated"
            		    ));
            		}
}
