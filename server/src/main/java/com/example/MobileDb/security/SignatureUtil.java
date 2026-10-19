package com.example.MobileDb.security;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignatureUtil {

    public static String generateSignature(String data, PrivateKey privateKey) {
        try {
            // Ensure data has no leading/trailing whitespace
            byte[] dataBytes = data.trim().getBytes(StandardCharsets.UTF_8);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(dataBytes);
            byte[] signedBytes = signature.sign();
            
            // Jenga HQ expects standard Base64 string without any line wraps
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating RSA signature", e);
        }
    }

    public static boolean verifySignature(String data, String signatureB64, PublicKey publicKey) {
        try {
            byte[] dataBytes = data.trim().getBytes(StandardCharsets.UTF_8);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(dataBytes);
            
            // Decode with MimeDecoder if you suspect line breaks, but standard is usually fine
            byte[] decodedSig = Base64.getDecoder().decode(signatureB64.trim());
            return signature.verify(decodedSig);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying RSA signature", e);
        }
    }
}
