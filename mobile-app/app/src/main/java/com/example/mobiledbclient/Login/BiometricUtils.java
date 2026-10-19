package com.example.mobiledbclient.Login;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class BiometricUtils {

    private static final String KEYSTORE_NAME = "AndroidKeyStore";

    /**
     * Generates a unique AES secret key for the user in the Android Keystore.
     */
    public static void generateBiometricKey(int userId) throws Exception {
        String alias = getAlias(userId);
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);

        if (!keyStore.containsAlias(alias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME);

            keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true) // Requires biometrics
                    .setInvalidatedByBiometricEnrollment(true) // Key invalidated if new biometric added
                    .build());

            keyGenerator.generateKey();
        }
    }

    /**
     * Gets a Cipher object for the user's secret key.
     * Handles KeyPermanentlyInvalidatedException by throwing it for the caller to handle.
     */
    public static Cipher getCipher(int userId) throws Exception {
        String alias = getAlias(userId);
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);

        SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (KeyPermanentlyInvalidatedException e) {
            // If the key is invalidated, remove it so it can be regenerated
            removeBiometricKey(userId);
            throw e;
        }
        return cipher;
    }

    /**
     * Generates a token for enrollment using the user's key.
     * This encrypts a fixed challenge string to prove key ownership.
     */
    public static String generateEnrollmentToken(int userId) throws Exception {
        Cipher cipher = getCipher(userId);
        byte[] encrypted = cipher.doFinal("enroll_challenge".getBytes("UTF-8"));
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    /**
     * Generates a token for login using the cipher returned by BiometricPrompt.
     * This encrypts a fixed challenge string to prove biometric authentication.
     */
    public static String generateToken(Cipher cipher) throws Exception {
        byte[] encrypted = cipher.doFinal("login_challenge".getBytes("UTF-8"));
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    public static boolean isBiometricEnrolled(int userId) {
        try {
            String alias = getAlias(userId);
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
            keyStore.load(null);
            return keyStore.containsAlias(alias);
        } catch (Exception e) {
            return false;
        }
    }

    public static void removeBiometricKey(int userId) {
        try {
            String alias = getAlias(userId);
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
            keyStore.load(null);
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias);
            }
        } catch (Exception ignored) {
        }
    }

    public static String getAlias(int userId) {
        return "biometric_key_user_" + userId;
    }
}
