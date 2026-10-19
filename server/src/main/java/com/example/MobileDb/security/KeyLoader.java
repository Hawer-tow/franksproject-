package com.example.MobileDb.security;

import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class KeyLoader {

    public static PrivateKey loadPrivateKeyFromClasspath(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String pem = new String(bytes, StandardCharsets.UTF_8);
            return parsePemToPrivateKey(pem);
        }
    }

    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        String pem = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        return parsePemToPrivateKey(pem);
    }

    private static PrivateKey parsePemToPrivateKey(String pem) throws Exception {
        // Updated to be more aggressive with cleaning to match the Mathenge snippet
        String key = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\r\\n", "")
                .replaceAll("\\n", "")
                .trim();
                
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
