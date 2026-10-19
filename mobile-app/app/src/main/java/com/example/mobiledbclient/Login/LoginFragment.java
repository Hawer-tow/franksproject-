package com.example.mobiledbclient.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.example.mobiledbclient.MainActivity;
import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.Login.SessionManager;
import com.example.mobiledbclient.Login.LoginRequest;
import com.example.mobiledbclient.Login.LoginResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private int userId;
    private String username;

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
        Button biometricBtn = root.findViewById(R.id.btnBiometric);
        MaterialTextView usernameView = root.findViewById(R.id.loginUsername);

        if (username != null && !username.isEmpty()) {
            usernameView.setText(username);
        } else {
            usernameView.setText("User");
        }

        loginBtn.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();
            if (!password.isEmpty()) {
                attemptLogin(password);
            } else {
                Toast.makeText(getContext(), "Please enter password", Toast.LENGTH_SHORT).show();
            }
        });

        biometricBtn.setOnClickListener(v -> handleBiometricLogin());

        return root;
    }

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
                    handleSuccessfulLogin(response.body());
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulLogin(LoginResponse resp) {
        // Save session
        SessionManager.setActiveUser(requireContext(), userId);

        // Enforce Biometric Enrollment prompt if not already enrolled
        checkBiometricEnrollment();

        if (resp.isAdmin()) {
            Toast.makeText(requireContext(), "Welcome Admin " + username, Toast.LENGTH_SHORT).show();
        }

        unlockApp(userId);
    }

    private void handleErrorResponse(int code) {
        String message;
        switch (code) {
            case 40301:
                message = "Access denied: inactive user";
                break;
            case 40302:
                message = "Biometrics not enrolled for this user";
                break;
            case 40101:
                message = "Authentication error: missing user context";
                break;
            default:
                message = "Login failed: Invalid credentials";
                break;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void checkBiometricEnrollment() {
        if (!BiometricUtils.isBiometricEnrolled(userId)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Enable Biometric Login")
                    .setMessage("Would you like to enable fingerprint/face login for this profile on this device?")
                    .setPositiveButton("Enable", (dialog, which) -> enrollBiometrics())
                    .setNegativeButton("Later", null)
                    .show();
        }
    }

    private void enrollBiometrics() {
        try {
            // 1. Generate hardware key
            BiometricUtils.generateBiometricKey(userId);
            String alias = BiometricUtils.getAlias(userId);
            String deviceId = SessionManager.getDeviceId(requireContext());

            // 2. Generate a token using the key (challenge string)
            String biometricToken = BiometricUtils.generateEnrollmentToken(userId);

            // 3. Prepare enrollment data
            Map<String, String> enrollmentData = new HashMap<>();
            enrollmentData.put("deviceId", deviceId);
            enrollmentData.put("biometricKeyAlias", alias);
            enrollmentData.put("biometricToken", biometricToken);

            // 4. Call server to link biometric alias to this device
            ApiService apiService = ApiClient.getApiService(requireContext());
            apiService.enableBiometrics(userId, enrollmentData).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful()) {
                        SessionManager.setBiometricAlias(requireContext(), userId, alias);
                        Toast.makeText(getContext(), "Biometrics enabled successfully", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 400) {
                        Toast.makeText(getContext(), "Enrollment failed: Missing deviceId or alias", Toast.LENGTH_LONG).show();
                    } else if (response.code() == 403) {
                        Toast.makeText(getContext(), "Access denied", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Enrollment failed: Server error " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    if (isAdded()) Toast.makeText(getContext(), "Network error during enrollment", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            if (isAdded()) Toast.makeText(getContext(), "Enrollment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }





private void handleBiometricLogin() {
    if (!BiometricUtils.isBiometricEnrolled(userId)) {
        Toast.makeText(requireContext(), "Biometrics not enrolled for this user", Toast.LENGTH_SHORT).show();
        return;
    }

    try {
        Cipher cipher = BiometricUtils.getCipher(userId);
        showBiometricPrompt(cipher);
    } catch (Exception e) {
        Toast.makeText(getContext(), "Biometric error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

private void showBiometricPrompt(Cipher cipher) {
    Executor executor = ContextCompat.getMainExecutor(requireContext());
    BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    if (!isAdded()) return;
                    try {
                        // Generate a token using the cipher from the authentication result
                        Cipher authCipher = result.getCryptoObject().getCipher();
                        String biometricToken = BiometricUtils.generateToken(authCipher);
                        performBiometricApiLogin(biometricToken);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Token generation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    if (isAdded() && errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        Toast.makeText(getContext(), errString, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login with Biometrics")
            .setSubtitle("Authenticate to access your account")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Use Password")
            .build();

    biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
}

private void performBiometricApiLogin(String biometricToken) {
    ApiService apiService = ApiClient.getApiService(requireContext());
    String deviceId = SessionManager.getDeviceId(requireContext());
    String alias = BiometricUtils.getAlias(userId);

    LoginRequest request = new LoginRequest(username, true, biometricToken, deviceId, alias);
    // ✅ Pass request as both @Body and @Tag for the SecurityInterceptor
    apiService.login(request, request).enqueue(new Callback<LoginResponse>() {
        @Override
        public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
            if (!isAdded()) return;
            if (response.isSuccessful() && response.body() != null) {
                handleSuccessfulLogin(response.body());
            } else {
                handleErrorResponse(response.code());
            }
        }
        @Override public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
            if (isAdded()) Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
        }
    });
}

private void unlockApp(int userId) {
    if (!isAdded()) return;
    if (getActivity() instanceof MainActivity) {
        ((MainActivity) getActivity()).unlockAndShowNavigation();
    }
}
}

