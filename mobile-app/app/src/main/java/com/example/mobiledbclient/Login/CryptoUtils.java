package com.example.mobiledbclient.Login;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    private static final String KEYSTORE_NAME = "AndroidKeyStore";
    private static final String CLIENT_RSA_ALIAS = "client_signing_key_rsa";

    /**
     * Generates a random 256-bit AES key.
     */
    public static byte[] generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey().getEncoded();
    }

    /**
     * Generates a random 16-byte IV for AES.
     */
    public static byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Encrypts plaintext using AES/CBC/PKCS5Padding.
     */
    public static byte[] encryptAES(String data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data.getBytes("UTF-8"));
    }

    /**
     * Decrypts ciphertext using AES/CBC/PKCS5Padding.
     */
    public static String decryptAES(byte[] encryptedData, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return new String(cipher.doFinal(encryptedData), "UTF-8");
    }

    /**
     * Encrypts a byte array using RSA/ECB/PKCS1Padding.
     */
    public static byte[] encryptRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts a byte array using RSA/ECB/PKCS1Padding.
     */
    public static byte[] decryptRSA(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * Signs data using the client's private RSA key (SHA256withRSA).
     */
    public static byte[] sign(String data, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(data.getBytes("UTF-8"));
        return privateSignature.sign();
    }

    /**
     * Retrieves the client's private RSA key from the Android Keystore.
     * Generates it if it doesn't exist.
     */
    public static PrivateKey getClientPrivateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);
        if (!keyStore.containsAlias(CLIENT_RSA_ALIAS)) {
            generateClientKeyPair();
        }
        return (PrivateKey) keyStore.getKey(CLIENT_RSA_ALIAS, null);
    }

    /**
     * Retrieves the client's public RSA key from the Android Keystore.
     */
    public static PublicKey getClientPublicKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);
        if (!keyStore.containsAlias(CLIENT_RSA_ALIAS)) {
            generateClientKeyPair();
        }
        return keyStore.getCertificate(CLIENT_RSA_ALIAS).getPublicKey();
    }

    private static void generateClientKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_NAME);
        kpg.initialize(new KeyGenParameterSpec.Builder(
                CLIENT_RSA_ALIAS,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build());
        kpg.generateKeyPair();
    }

    /**
     * Converts a PEM-formatted public key string to a PublicKey object.
     */
    public static PublicKey getPublicKeyFromString(String keyStr) throws Exception {
        String cleanKey = keyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] byteKey = Base64.decode(cleanKey, Base64.NO_WRAP);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static String toBase64(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }
    
    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
}
