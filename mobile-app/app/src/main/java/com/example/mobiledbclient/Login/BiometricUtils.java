package com.example.mobiledbclient.Login;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.biometric.BiometricManager;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

/**
 * Fingerprint login keys.
 *
 * Each profile gets an EC P-256 key pair in the Android Keystore. The private key never leaves the
 * phone and can only sign after a fingerprint check (BiometricPrompt + CryptoObject); the server
 * keeps the public key and verifies the signed login message.
 */
public class BiometricUtils {

    private static final String KEYSTORE_NAME = "AndroidKeyStore";

    /** True when the phone has a strong biometric (fingerprint) set up and ready to use. */
    public static boolean canUseFingerprint(Context context) {
        return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /** Creates (or replaces) the profile's key pair and returns the public key as Base64 X.509. */
    public static String createKeyPair(int userId) throws Exception {
        removeBiometricKey(userId);
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_NAME);
        generator.initialize(new KeyGenParameterSpec.Builder(getAlias(userId), KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(true)       // every signature needs a fingerprint
                .setInvalidatedByBiometricEnrollment(true) // adding a new fingerprint disables the key
                .build());
        KeyPair pair = generator.generateKeyPair();
        return Base64.encodeToString(pair.getPublic().getEncoded(), Base64.NO_WRAP);
    }

    /**
     * Signature object to hand to BiometricPrompt. Throws KeyPermanentlyInvalidatedException (and
     * deletes the key) when the phone's fingerprints changed since fingerprint login was set up.
     */
    public static Signature getSignature(int userId) throws Exception {
        PrivateKey key = (PrivateKey) loadKeyStore().getKey(getAlias(userId), null);
        if (key == null) {
            throw new IllegalStateException("Fingerprint login is not set up for this profile");
        }
        Signature signature = Signature.getInstance("SHA256withECDSA");
        try {
            signature.initSign(key);
        } catch (KeyPermanentlyInvalidatedException e) {
            removeBiometricKey(userId);
            throw e;
        }
        return signature;
    }

    /** The exact text the server verifies (BiometricService.loginMessage). */
    public static String loginMessage(String username, String deviceId, long timestamp) {
        return "FrankMpesa-login|" + username + "|" + deviceId + "|" + timestamp;
    }

    /** Signs with a Signature that BiometricPrompt has unlocked; returns Base64. */
    public static String sign(Signature unlocked, String message) throws Exception {
        unlocked.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(unlocked.sign(), Base64.NO_WRAP);
    }

    /** True when this profile has a fingerprint signing key on this phone. */
    public static boolean isBiometricEnrolled(int userId) {
        try {
            KeyStore.Entry entry = loadKeyStore().getEntry(getAlias(userId), null);
            return entry instanceof KeyStore.PrivateKeyEntry; // older app versions stored a different key type
        } catch (Exception e) {
            return false;
        }
    }

    public static void removeBiometricKey(int userId) {
        try {
            KeyStore keyStore = loadKeyStore();
            String alias = getAlias(userId);
            if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias);
        } catch (Exception ignored) {
            // nothing to remove
        }
    }

    public static String getAlias(int userId) {
        return "biometric_key_user_" + userId;
    }

    private static KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);
        return keyStore;
    }
}
