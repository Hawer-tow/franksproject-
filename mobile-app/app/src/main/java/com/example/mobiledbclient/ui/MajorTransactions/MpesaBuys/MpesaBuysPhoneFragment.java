package com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys.StkPushRequest;
import com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys.StkPushResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MpesaBuysPhoneFragment extends Fragment {

    private TextInputEditText etPhone, etAmount, etRate;
    private MaterialButton btnInitiate;
    private CircularProgressIndicator progressIndicator;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mpesa_buys_phone_card, container, false);

        etPhone = view.findViewById(R.id.etPhoneMpesa);
        etAmount = view.findViewById(R.id.etAmountMpesa);
        etRate = view.findViewById(R.id.etRateMpesa);
        btnInitiate = view.findViewById(R.id.btnInitiateMpesa);
        progressIndicator = view.findViewById(R.id.progressMpesaPhone);

        // ✅ Updated to pass context to ensure X-User-Id header is added
        apiService = ApiClient.getApiService(requireContext());

        btnInitiate.setOnClickListener(v -> initiateStkPush());

        return view;
    }

    private void initiateStkPush() {
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String rateStr = etRate.getText() != null ? etRate.getText().toString().trim() : "";

        if (phone.isEmpty() || amountStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(getContext(), "Phone, Amount and Rate are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Normalize phone number
        phone = normalizePhoneNumber(phone);
        if (phone == null) {
            Toast.makeText(getContext(), "Invalid phone number format. Use 07XXXXXXXX or +2547XXXXXXXX", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount, rate;
        try {
            amount = Double.parseDouble(amountStr);
            rate = Double.parseDouble(rateStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(getContext(), "Invalid amount or rate", Toast.LENGTH_SHORT).show();
            return;
        }

        StkPushRequest request = new StkPushRequest(phone, amount, rate);

        // Show processing state
        progressIndicator.setVisibility(View.VISIBLE);
        btnInitiate.setEnabled(false);
        btnInitiate.setText("Processing...");

        Call<StkPushResponse> call = apiService.initiateStkPush(request);
        call.enqueue(new Callback<StkPushResponse>() {
            @Override
            public void onResponse(@NonNull Call<StkPushResponse> call, @NonNull Response<StkPushResponse> response) {
                resetButtonState();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(),
                            "Payment initiated: " + response.body().getCustomerMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Transaction failed. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StkPushResponse> call, @NonNull Throwable t) {
                resetButtonState();
                Toast.makeText(getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper to normalize phone numbers
    private String normalizePhoneNumber(String input) {
        // Remove spaces
        String phone = input.replaceAll("\\s+", "");

        if (phone.startsWith("+")) {
            phone = phone.substring(1); // remove +
        }

        if (phone.startsWith("0")) {
            // Convert 07XXXXXXXX → 2547XXXXXXXX
            phone = "254" + phone.substring(1);
        }

        // Validate length and prefix
        if (phone.matches("^2547\\d{8}$")) {
            return phone;
        } else {
            return null; // invalid format
        }
    }


    private void resetButtonState() {
        progressIndicator.setVisibility(View.GONE);
        btnInitiate.setEnabled(true);
        btnInitiate.setText("Initiate Payment");
    }
}
