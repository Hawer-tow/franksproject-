package com.example.MobileDb.service;

import com.example.MobileDb.entity.kisenyi.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fingerprint login.
 *
 * The phone keeps a key pair in its Android Keystore that can only be used after a fingerprint
 * check; the server stores the public half on the user (users.biometric_public_key). To log in,
 * the phone signs {@link #loginMessage} for the current time and the server verifies it here.
 * A signature is accepted once, and only within a few minutes of the phone's timestamp.
 */
@Service
public class BiometricService {

    private static final Logger log = LoggerFactory.getLogger(BiometricService.class);

    static final long MAX_CLOCK_SKEW_MS = 5 * 60 * 1000;

    private final Map<Integer, Long> lastAcceptedTimestamp = new ConcurrentHashMap<>();
    private final Clock clock;

    public BiometricService() {
        this(Clock.systemUTC());
    }

    BiometricService(Clock clock) {
        this.clock = clock;
    }

    /** The exact text the phone signs. Must match BiometricUtils.loginMessage in the app. */
    public static String loginMessage(String username, String deviceId, long timestamp) {
        return "FrankMpesa-login|" + username + "|" + deviceId + "|" + timestamp;
    }

    /** Validates a Base64 X.509 public key sent by the phone and returns it in canonical Base64. */
    public String normalisePublicKey(String base64) {
        try {
            return Base64.getEncoder().encodeToString(parsePublicKey(base64).getEncoded());
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new IllegalArgumentException("Invalid fingerprint public key", e);
        }
    }

    /** True when the signature proves a fingerprint check on the user's registered phone. */
    public boolean verifyLogin(User user, String deviceId, Long timestamp, String signatureBase64) {
        if (user == null || user.getBiometricPublicKey() == null || deviceId == null
                || timestamp == null || signatureBase64 == null || signatureBase64.isBlank()) {
            return false;
        }
        long now = clock.millis();
        if (Math.abs(now - timestamp) > MAX_CLOCK_SKEW_MS) {
            log.warn("Fingerprint login for {} rejected: phone clock {} ms off", user.getUsername(), now - timestamp);
            return false;
        }
        Long last = lastAcceptedTimestamp.get(user.getId());
        if (last != null && timestamp <= last) {
            log.warn("Fingerprint login for {} rejected: signature already used", user.getUsername());
            return false;
        }
        try {
            PublicKey key = parsePublicKey(user.getBiometricPublicKey());
            Signature sig = Signature.getInstance("EC".equals(key.getAlgorithm()) ? "SHA256withECDSA" : "SHA256withRSA");
            sig.initVerify(key);
            sig.update(loginMessage(user.getUsername(), deviceId, timestamp).getBytes(StandardCharsets.UTF_8));
            boolean ok = sig.verify(Base64.getDecoder().decode(signatureBase64));
            if (ok) lastAcceptedTimestamp.put(user.getId(), timestamp);
            return ok;
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            log.warn("Fingerprint login for {} rejected: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }

    static PublicKey parsePublicKey(String base64) throws GeneralSecurityException {
        byte[] bytes = Base64.getDecoder().decode(base64.trim());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        try {
            return KeyFactory.getInstance("EC").generatePublic(spec);
        } catch (GeneralSecurityException notEc) {
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }
}
