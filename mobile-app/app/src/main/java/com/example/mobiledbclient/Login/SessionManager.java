package com.example.mobiledbclient.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.util.UUID;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "active_user_id";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_BIOMETRIC_PREFIX = "biometric_alias_";
    private static final String KEY_SERVER_PUBLIC_KEY = "server_public_key";

    public static void setActiveUser(Context context, int userId) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public static int getActiveUser(Context context) {
        if (context == null) return -1;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public static boolean hasActiveUser(Context context) {
        return getActiveUser(context) != -1;
    }

    public static void clearActiveUser(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
        
        SharedPreferences settingsPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        settingsPrefs.edit().remove("activeUserId").apply();
    }

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);

        if (deviceId == null) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        return deviceId;
    }

    public static void setBiometricAlias(Context context, int userId, String alias) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BIOMETRIC_PREFIX + userId, alias).apply();
    }

    public static String getBiometricAlias(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BIOMETRIC_PREFIX + userId, null);
    }

    /**
     * Stores the server's RSA public key.
     */
    public static void setServerPublicKey(Context context, String publicKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SERVER_PUBLIC_KEY, publicKey).apply();
    }

    /**
     * Retrieves the server's RSA public key.
     */
    public static String getServerPublicKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SERVER_PUBLIC_KEY, null);
    }
}
