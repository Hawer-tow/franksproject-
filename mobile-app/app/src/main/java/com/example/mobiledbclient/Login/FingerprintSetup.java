package com.example.mobiledbclient.Login;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Turns fingerprint login on and off for a profile on this phone. Used by the login screen
 * (offer after a password login) and by the Settings switch.
 */
public final class FingerprintSetup {

    public interface Result {
        void done(boolean ok, String message);
    }

    private FingerprintSetup() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    private static String enabledKey(int userId) {
        return "fingerprintEnabled_" + userId;
    }

    private static String offeredKey(int userId) {
        return "fingerprintOffered_" + userId;
    }

    /** Fingerprint login is on for this profile and its key is still on the phone. */
    public static boolean isOn(Context context, int userId) {
        return prefs(context).getBoolean(enabledKey(userId), false) && BiometricUtils.isBiometricEnrolled(userId);
    }

    /** Whether the "use your fingerprint next time?" question was already asked for this profile. */
    public static boolean wasOffered(Context context, int userId) {
        return prefs(context).getBoolean(offeredKey(userId), false);
    }

    public static void markOffered(Context context, int userId) {
        prefs(context).edit().putBoolean(offeredKey(userId), true).apply();
    }

    /**
     * Creates the fingerprint key and registers its public key with the server. The server checks
     * the profile's password so nobody can attach their own fingerprint to someone else's profile.
     */
    public static void enable(Context context, int userId, String password, Result result) {
        Context app = context.getApplicationContext();
        if (!BiometricUtils.canUseFingerprint(app)) {
            result.done(false, "Add a fingerprint in your phone's settings first");
            return;
        }
        String publicKey;
        try {
            publicKey = BiometricUtils.createKeyPair(userId);
        } catch (Exception e) {
            result.done(false, "This phone could not create a fingerprint key");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("deviceId", SessionManager.getDeviceId(app));
        body.put("biometricKeyAlias", BiometricUtils.getAlias(userId));
        body.put("publicKey", publicKey);
        body.put("password", password);

        ApiService api = ApiClient.getApiService(app);
        api.enableBiometrics(userId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    prefs(app).edit().putBoolean(enabledKey(userId), true).putBoolean(offeredKey(userId), true).apply();
                    SessionManager.setBiometricAlias(app, userId, BiometricUtils.getAlias(userId));
                    result.done(true, "Fingerprint login is on");
                } else {
                    BiometricUtils.removeBiometricKey(userId);
                    result.done(false, response.code() == 401
                            ? "Password is incorrect"
                            : "Could not turn on fingerprint login (error " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                BiometricUtils.removeBiometricKey(userId);
                result.done(false, "No connection to the server. Try again when you are online.");
            }
        });
    }

    /** Turns fingerprint login off on this phone and tells the server to forget the key. */
    public static void disable(Context context, int userId) {
        Context app = context.getApplicationContext();
        BiometricUtils.removeBiometricKey(userId);
        prefs(app).edit().putBoolean(enabledKey(userId), false).apply();
        ApiClient.getApiService(app)
                .updateBiometricStatus(userId, Collections.singletonMap("enabled", false))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        // the key is already gone from the phone, so nothing else to do
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        // offline: the server key is useless without the phone's private key
                    }
                });
    }

    /** Forget the profile's fingerprint state locally (e.g. after the phone's fingerprints changed). */
    public static void forgetLocally(Context context, int userId) {
        BiometricUtils.removeBiometricKey(userId);
        prefs(context.getApplicationContext()).edit().putBoolean(enabledKey(userId), false).apply();
    }
}
