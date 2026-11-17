package com.example.mobiledbclient.Login;

import android.content.Context;
import android.os.Bundle;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.MainActivity;
import com.example.mobiledbclient.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.security.Signature;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Login for the chosen profile: password, or fingerprint once it has been set up on this phone. */
public class LoginFragment extends Fragment {
    private int userId;
    private String username;

    /** Password just used to log in; needed once to turn fingerprint login on. Cleared afterwards. */
    private String lastPassword;
    private Button biometricBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt("user_id", -1);
            username = getArguments().getString("username", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        EditText passwordInput = root.findViewById(R.id.editPassword);
        Button loginBtn = root.findViewById(R.id.btnLogin);
        biometricBtn = root.findViewById(R.id.btnBiometric);
        MaterialTextView usernameView = root.findViewById(R.id.loginUsername);

        usernameView.setText(username != null && !username.isEmpty() ? username : "User");

        loginBtn.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();
            if (!password.isEmpty()) {
                attemptLogin(password);
            } else {
                Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            }
        });

        // Fingerprint button only when it is set up for this profile on this phone
        boolean fingerprintReady = FingerprintSetup.isOn(requireContext(), userId)
                && BiometricUtils.canUseFingerprint(requireContext());
        biometricBtn.setVisibility(fingerprintReady ? View.VISIBLE : View.GONE);
        biometricBtn.setOnClickListener(v -> handleBiometricLogin());

        // Open the fingerprint prompt straight away when it is available
        if (fingerprintReady && savedInstanceState == null) {
            root.post(() -> {
                if (isAdded()) handleBiometricLogin();
            });
        }

        return root;
    }

    // ------------------------------------------------------------------ password

    private void attemptLogin(String password) {
        ApiService apiService = ApiClient.getApiService(requireContext());
        String deviceId = SessionManager.getDeviceId(requireContext());
        LoginRequest request = new LoginRequest(username, password, deviceId);

        // ✅ Pass request as both @Body and @Tag for the SecurityInterceptor
        apiService.login(request, request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    lastPassword = password;
                    handleSuccessfulLogin(response.body());
                } else {
                    showError(response, "Login failed: wrong password");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error: could not reach the server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulLogin(LoginResponse resp) {
        Context context = requireContext();
        SessionManager.setActiveUser(context, userId);

        if (resp.isAdmin()) {
            Toast.makeText(context, "Welcome Admin " + username, Toast.LENGTH_SHORT).show();
        }

        offerFingerprintLogin();
        unlockApp();
    }

    /** After a password login, ask once whether to use the fingerprint next time. */
    private void offerFingerprintLogin() {
        Context context = requireActivity();
        String password = lastPassword;
        lastPassword = null;
        if (password == null
                || !BiometricUtils.canUseFingerprint(context)
                || FingerprintSetup.isOn(context, userId)
                || FingerprintSetup.wasOffered(context, userId)) {
            return;
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle("Use your fingerprint next time?")
                .setMessage("Log in to " + username + " with your fingerprint on this phone instead of typing your password. You can change this in Settings.")
                .setPositiveButton("Turn on", (dialog, which) ->
                        FingerprintSetup.enable(context, userId, password, (ok, message) ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Not now", (dialog, which) -> FingerprintSetup.markOffered(context, userId))
                .show();
    }

    // ------------------------------------------------------------------ fingerprint

    private void handleBiometricLogin() {
        Context context = requireContext();
        if (!FingerprintSetup.isOn(context, userId)) {
            Toast.makeText(context, "Fingerprint login is not set up. Log in with your password, then turn it on in Settings.", Toast.LENGTH_LONG).show();
            biometricBtn.setVisibility(View.GONE);
            return;
        }
        try {
            showBiometricPrompt(BiometricUtils.getSignature(userId));
        } catch (KeyPermanentlyInvalidatedException e) {
            FingerprintSetup.forgetLocally(context, userId);
            biometricBtn.setVisibility(View.GONE);
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Fingerprints changed")
                    .setMessage("The fingerprints on this phone changed, so fingerprint login was turned off. Log in with your password and turn it on again in Settings.")
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(context, "Fingerprint error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBiometricPrompt(Signature signature) {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        if (!isAdded()) return;
                        try {
                            long timestamp = System.currentTimeMillis();
                            String deviceId = SessionManager.getDeviceId(requireContext());
                            Signature unlocked = result.getCryptoObject().getSignature();
                            String token = BiometricUtils.sign(unlocked,
                                    BiometricUtils.loginMessage(username, deviceId, timestamp));
                            performBiometricApiLogin(token, timestamp, deviceId);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Fingerprint error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        if (isAdded() && errorCode != BiometricPrompt.ERROR_USER_CANCELED
                                && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                && errorCode != BiometricPrompt.ERROR_CANCELED) {
                            Toast.makeText(getContext(), errString, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Log in to FrankMpesa")
                .setSubtitle(username)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Use password")
                .build();

        biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(signature));
    }

    private void performBiometricApiLogin(String token, long timestamp, String deviceId) {
        ApiService apiService = ApiClient.getApiService(requireContext());
        LoginRequest request = new LoginRequest(username, true, token, deviceId, BiometricUtils.getAlias(userId));
        request.setBiometricTimestamp(timestamp);

        // ✅ Pass request as both @Body and @Tag for the SecurityInterceptor
        apiService.login(request, request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                    return;
                }
                if (response.code() == 403) {
                    // Server no longer has this phone's key: switch fingerprint off here too
                    FingerprintSetup.forgetLocally(requireContext(), userId);
                    biometricBtn.setVisibility(View.GONE);
                }
                showError(response, "Fingerprint login failed. Use your password.");
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error: could not reach the server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------------------------------------------------ helpers

    /** Shows the server's own message ({"message": "..."}) when there is one. */
    private void showError(Response<?> response, String fallback) {
        String message = fallback;
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                String fromServer = new JSONObject(body).optString("message", "");
                if (!fromServer.isEmpty()) message = fromServer;
            }
        } catch (Exception ignored) {
            // keep the fallback
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void unlockApp() {
        if (!isAdded()) return;
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).unlockAndShowNavigation();
        }
    }
}
