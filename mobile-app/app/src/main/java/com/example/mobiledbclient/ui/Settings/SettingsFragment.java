package com.example.mobiledbclient.ui.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.mobiledbclient.Login.User.User_db_instance;
import com.example.mobiledbclient.Login.User.User_queries;
import com.example.mobiledbclient.R;


import com.example.mobiledbclient.Login.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.Executors;

import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_db_instance;

import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_queries;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_queries;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_queries;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_queries;

import com.example.mobiledbclient.ui.Kisenyi.expenses.ExpenseAdapter;
import com.example.mobiledbclient.ui.Kisenyi.buys.BuyingAdapter;

import com.example.mobiledbclient.Login.User.UserRepository;
import com.example.mobiledbclient.Login.PasswordResetRequest;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;


public class SettingsFragment extends Fragment {

    private SwitchMaterial themeSwitch;
    private SwitchMaterial securitySwitch;
    private MaterialButton clearLocalDataBtn;
    private MaterialButton resetPasswordBtn;
    private RadioGroup dataRangeGroup;
    private SharedPreferences prefs;

    private ExpenseAdapter expenseAdapter;
    private BuyingAdapter buyingAdapter;
    private BuyingAdapter sellingAdapter;
    private BuyingAdapter openAndCloseAdapter;
    private UserRepository userRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        userRepository = new UserRepository(requireContext());


        themeSwitch = view.findViewById(R.id.Theme_change);
        securitySwitch = view.findViewById(R.id.fingerprintsecurity);
        clearLocalDataBtn = view.findViewById(R.id.clearLocalData);
        resetPasswordBtn = view.findViewById(R.id.resetPasswordBtn);
        dataRangeGroup = view.findViewById(R.id.dataRangeGroup);

        int activeUserId = SessionManager.getActiveUser(requireContext());

        // --- PER-USER THEME INITIALIZATION ---
        String themeKey = "selectedTheme_" + activeUserId;
        String themeName = prefs.getString(themeKey, "AppTheme_Light");
        themeSwitch.setChecked("AppTheme_Dark".equals(themeName));

        // --- PER-USER SECURITY INITIALIZATION ---
        String securityKey = "securityEnabled_" + activeUserId;
        boolean securityEnabled = prefs.getBoolean(securityKey, true);
        securitySwitch.setChecked(securityEnabled);

        // --- DATA RANGE INITIALIZATION ---
        String range = prefs.getString("data_range", "all");
        switch (range) {
            case "3days": dataRangeGroup.check(R.id.range3days); break;
            case "7days": dataRangeGroup.check(R.id.range7days); break;
            case "monthly": dataRangeGroup.check(R.id.rangeMonthly); break;
            default: dataRangeGroup.check(R.id.rangeAll); break;
        }

// --- THEME SWITCH HANDLER ---
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(themeKey, isChecked ? "AppTheme_Dark" : "AppTheme_Light");
            editor.apply();

            // Apply theme change directly
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);

            // Do not call requireActivity().recreate()
            // That was causing the app to restart and default back to login
        });


        // --- SECURITY SWITCH HANDLER ---
        securitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prefs.edit().putBoolean(securityKey, true).apply();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Disable Security")
                        .setMessage("Are you sure you want to disable security for your profile? This will turn off BOTH password and biometric login.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            prefs.edit().putBoolean(securityKey, false).apply();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                            securitySwitch.setChecked(true);
                        })
                        .show();
            }
        });

        // --- DATA RANGE HANDLER ---
        dataRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (checkedId == R.id.range3days) editor.putString("data_range", "3days");
            else if (checkedId == R.id.range7days) editor.putString("data_range", "7days");
            else if (checkedId == R.id.rangeMonthly) editor.putString("data_range", "monthly");
            else editor.putString("data_range", "all");
            editor.apply();
        });

        // --- CLEAR LOCAL DATA HANDLER ---
        clearLocalDataBtn.setOnClickListener(v -> confirmAndClearLocalData());

        // --- RESET PASSWORD HANDLER ---
        resetPasswordBtn.setOnClickListener(v -> showResetPasswordDialog(activeUserId));

        return view;
    }

    private void showResetPasswordDialog(int userId) {
        final View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_password, null);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set New Password")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
                    TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(requireContext(), "Password fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Use ApiClient + ApiService
                    // ✅ FIXED: Using context-safe getApiService() to ensure header support
                    ApiService apiService = ApiClient.getApiService(requireContext());

                    PasswordResetRequest request = new PasswordResetRequest(newPassword);

                    // ✅ FIXED: Pass the request as both @Body and @Tag for SecurityInterceptor fallback
                    apiService.setPassword(userId, request, request).enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void confirmAndClearLocalData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear local data")
                .setMessage("This will permanently remove cached and locally stored records. Continue?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Clear", (d, w) -> {
                    d.dismiss();
                    runClearLocalData();
                })
                .show();
    }

    private void runClearLocalData() {
        Toast.makeText(requireContext(), "Clearing local data...", Toast.LENGTH_SHORT).show();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                expense_queries expenseDao = expense_db_instance.getInstance(requireContext()).expense_Queries();
                Buying_queries buyingDao = Buying_db_instance.getInstance(requireContext()).buying_Queries();
                Selling_queries sellingDao = Selling_db_instance.getInstance(requireContext()).selling_Queries();
                OpenAndClose_queries openAndCloseDao = OpenAndClose_db_instance.getInstance(requireContext()).openAndClose_Queries();
                User_queries userDao = User_db_instance.getInstance(requireContext()).userQueries();

                userDao.clearAll();
                expenseDao.clearAll();
                buyingDao.clearAll();
                sellingDao.clearAll();
                openAndCloseDao.clearAll();

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Local databases cleared", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception ex) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to clear data", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Adapter setters (optional)
    public void setExpenseAdapter(ExpenseAdapter adapter) { this.expenseAdapter = adapter; }
    public void setBuyingAdapter(BuyingAdapter adapter) { this.buyingAdapter = adapter; }
    public void setSellingAdapter(BuyingAdapter adapter) { this.sellingAdapter = adapter; }
    public void setOpenAndCloseAdapter(BuyingAdapter adapter) { this.openAndCloseAdapter = adapter; }
}
