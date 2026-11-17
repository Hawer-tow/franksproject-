package com.example.MobileDb.mobileappfilescontroller;

import com.example.MobileDb.security.CryptoUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@RestController
public class KeyController {

    @GetMapping("/api/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        try {
            // ✅ Load server public key from the classpath PEM file
            byte[] publicKeyBytes;
            try (InputStream is = new ClassPathResource("keys/mobile2serverencryption_decription_public_key.pem").getInputStream()) {
                publicKeyBytes = is.readAllBytes();
            }

            PublicKey publicKey = CryptoUtils.loadPublicKey(publicKeyBytes);

            // ✅ Encode to Base64 string for client consumption
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            return ResponseEntity.ok(Map.of("publicKey", publicKeyBase64));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to load public key"));
        }
    }
}
