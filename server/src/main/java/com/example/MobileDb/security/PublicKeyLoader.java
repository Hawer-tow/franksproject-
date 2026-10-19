package com.example.MobileDb.security;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for loading RSA public keys from PEM files.
 * Supports both classpath resources and filesystem paths.
 */
public class PublicKeyLoader {

    /**
     * Load a public key from the classpath (e.g. "keys/jenga_public.pem").
     */
	public static PublicKey loadPublicKeyFromClasspath(String resourcePath) throws Exception {
	    ClassPathResource resource = new ClassPathResource(resourcePath);
	    try (InputStream is = resource.getInputStream()) {
	        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
	        return parsePemToPublicKey(pem);
	    }
	}

    /**
     * Load a public key from an absolute or relative filesystem path.
     * Example: "/opt/keys/jenga_public.pem" or "./src/main/resources/keys/jenga_public.pem".
     */
 
	public static PublicKey loadPublicKey(String filePath) throws Exception {
	    String pem = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
	    return parsePemToPublicKey(pem);
	}
    /**
     * Parse a PEM string into a PublicKey object.
     * Expects X.509 format:
     * -----BEGIN PUBLIC KEY-----
     * (Base64 data)
     * -----END PUBLIC KEY-----
     */
    private static PublicKey parsePemToPublicKey(String pem) throws Exception {
        String key = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
