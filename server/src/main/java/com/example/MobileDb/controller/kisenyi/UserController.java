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

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
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

            // Logging incoming request values
            logger.info("Login attempt for username: {}", loginRequest.getUsername());
            logger.debug("Raw password received: {}", loginRequest.getPassword());
            logger.debug("Stored password hash: {}", user.getPasswordHash());
            logger.debug("Biometric flag: {}", loginRequest.isBiometricLogin());
            logger.debug("DeviceId received: {}", loginRequest.getDeviceId());
            logger.debug("BiometricToken received: {}", loginRequest.getBiometricToken());
            logger.debug("BiometricKeyAlias received: {}", loginRequest.getBiometricKeyAlias());
            logger.info("Raw LoginRequest object: {}", loginRequest);
       

            // Admin bypass
            if (Boolean.TRUE.equals(user.getIsAdmin())) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                logger.info("Admin login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(Map.of(
                        "status", "true",
                        "code", "200",
                        "message", "Admin login successful"
                ));
            }

            // Enforce access_status
            if (user.getAccessStatus() == null || user.getAccessStatus() != 1) {
                logger.warn("Access denied for inactive user: {}", user.getUsername());
                return ResponseEntity.status(403).body(Map.of(
                        "status", "false",
                        "code", "40301",
                        "message", "Access denied: inactive user"
                ));
            }

            // Biometric login flow
            if (loginRequest.isBiometricLogin()) {
                logger.info("Biometric login requested for user: {}", user.getUsername());

                boolean valid = biometricService.validateToken(
                    loginRequest.getUsername(),
                    loginRequest.getBiometricToken(),
                    loginRequest.getBiometricKeyAlias()
                );

                logger.debug("Biometric validation result for {}: {}", user.getUsername(), valid);

                if (!Boolean.TRUE.equals(user.getBiometricEnabled())) {
                    if (encoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                        user.setLastLogin(LocalDateTime.now());
                        userRepository.save(user);
                        logger.info("Password fallback login successful for user: {}", user.getUsername());
                        return ResponseEntity.ok(Map.of(
                                "status", "true",
                                "code", "200",
                                "message", "Password login fallback successful"
                        ));
                    }
                    logger.warn("Biometric login failed: biometrics not enrolled for {}", user.getUsername());
                    return ResponseEntity.status(403).body(Map.of(
                            "status", "false",
                            "code", "40302",
                            "message", "Biometrics not enrolled for this user"
                    ));
                }

                if (user.getDeviceId() == null || !user.getDeviceId().equals(loginRequest.getDeviceId())) {
                    logger.warn("Device mismatch for user: {}", user.getUsername());
                    return ResponseEntity.status(403).body(Map.of(
                            "status", "false",
                            "code", "40303",
                            "message", "Device mismatch: biometrics only allowed on registered device"
                    ));
                }

                if (loginRequest.getBiometricToken() == null || loginRequest.getBiometricKeyAlias() == null
                        || !valid) {
                    logger.error("Invalid biometric credentials for user: {}", user.getUsername());
                    return ResponseEntity.status(401).body(Map.of(
                            "status", "false",
                            "code", "401",
                            "message", "Invalid biometric credentials"
                    ));
                }

                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                logger.info("Biometric login successful for user: {}", user.getUsername());
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

    // Enable biometrics with public key
    @PostMapping("/{id}/enable-biometrics")
    public ResponseEntity<?> enableBiometrics(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {

        User user = userRepository.findById(id).orElseThrow();

        String deviceId = payload.get("deviceId");
        String biometricKeyAlias = payload.get("biometricKeyAlias");
        String publicKeyBase64 = payload.get("publicKey");

        if (deviceId == null || biometricKeyAlias == null || publicKeyBase64 == null) {
            logger.warn("Enable biometrics failed: missing parameters for user {}", user.getUsername());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "false",
                    "code", "400",
                    "message", "Missing deviceId, biometricKeyAlias, or publicKey"
            ));
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);

            biometricService.registerKey(user.getUsername(), biometricKeyAlias, publicKey);

            user.setBiometricEnabled(true);
            user.setDeviceId(deviceId);
            user.setBiometricKeyAlias(biometricKeyAlias);
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            logger.info("Biometrics enabled for user: {}", user.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "true",
                    "code", "200",
                    "message", "Biometrics enabled for user",
                    "deviceId", deviceId,
                    "biometricKeyAlias", biometricKeyAlias
            ));
        } catch (Exception e) {
            logger.error("Failed to parse public key for user {}: {}", user.getUsername(), e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "false",
                    "code", "500",
                    "message", "Failed to parse public key"
            ));
        }
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

            		    logger.info("Resetting password for user {} with new raw password: {}", id, request.getNewPassword());

            		    String hashed = encoder.encode(request.getNewPassword());
            		    user.setPasswordHash(hashed);
            		    user.setUpdatedAt(LocalDateTime.now());

            		    userRepository.saveAndFlush(user);

            		    logger.info("Password successfully updated for user {}. Stored hash: {}", id, hashed);

            		    return ResponseEntity.ok(Map.of(
            		        "status", "true",
            		        "code", "200",
            		        "message", "Password updated"
            		    ));
            		}
}
