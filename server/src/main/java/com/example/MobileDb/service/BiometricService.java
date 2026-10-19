package com.example.MobileDb.service;

import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BiometricService {

    // In-memory store for demo purposes — replace with DB lookups
    private final Map<String, PublicKey> userKeys = new ConcurrentHashMap<>();

    /**
     * Register a public key for a user when biometrics are enabled.
     * In production, persist this in your database.
     */
    public void registerKey(String username, String biometricKeyAlias, PublicKey publicKey) {
        userKeys.put(username + ":" + biometricKeyAlias, publicKey);
    }

    /**
     * Validate a biometric token by verifying its signature against the stored public key.
     *
     * @param username          The username
     * @param biometricToken    Base64-encoded signature from client
     * @param biometricKeyAlias Key alias used during enrollment
     * @return true if signature is valid, false otherwise
     */
    public boolean validateToken(String username, String biometricToken, String biometricKeyAlias) {
        try {
            if (biometricToken == null || biometricToken.isBlank()) return false;
            if (biometricKeyAlias == null || biometricKeyAlias.isBlank()) return false;

            // Lookup stored public key
            PublicKey publicKey = userKeys.get(username + ":" + biometricKeyAlias);
            if (publicKey == null) {
                return false; // No key registered for this user/alias
            }

            // Decode the signed token (signature)
            byte[] signedData = Base64.getDecoder().decode(biometricToken);

            // Example challenge — in production, issue a random challenge per login
            byte[] challenge = ("LOGIN_CHALLENGE:" + username).getBytes();

            // Verify signature
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(challenge);

            return sig.verify(signedData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
