package com.example.MobileDb.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.spec.PKCS8EncodedKeySpec;

public class CryptoUtils {

    private static final String RSA_ALGO = "RSA/ECB/PKCS1Padding";
    private static final String AES_ALGO = "AES/CBC/PKCS5Padding";

    // ✅ Load server private key (from keystore or PEM file)
    public static PrivateKey loadPrivateKey(byte[] keyBytes) throws Exception {
        String pem = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(byte[] keyBytes) throws Exception {
        String pem = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }


    // ✅ Decrypt AES key using server private key
    public static SecretKey decryptAESKey(String encryptedKeyBase64, PrivateKey privateKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGO);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(Base64.getDecoder().decode(encryptedKeyBase64));
        return new SecretKeySpec(aesKeyBytes, "AES");
    }

    // ✅ Decrypt payload using AES key + IV


    public static String decryptPayload(String encryptedPayload, String ivBase64, SecretKey aesKey) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(ivBase64);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // must match Android
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

        byte[] decodedPayload = Base64.getDecoder().decode(encryptedPayload);
        byte[] decryptedBytes = cipher.doFinal(decodedPayload);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // ✅ Verify signature (client signs payload with its private key)
    public static boolean verifySignature(String payload, String signatureBase64, PublicKey clientPublicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(clientPublicKey);
        sig.update(payload.getBytes(StandardCharsets.UTF_8));
        return sig.verify(Base64.getDecoder().decode(signatureBase64));
    }
}
