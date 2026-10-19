package com.example.mobiledbclient.ui.MajorTransactions.MpesaSales;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class mpesaPaybill2Phonefragment1 extends Fragment {

    private static final String TAG = "MpesaFragment1";

    private TextInputEditText etSenderPaybill;
    private TextInputEditText etRecipientPhone;
    private TextInputEditText etAmount;
    private TextInputEditText etRate;
    private TextInputEditText etDescription;
    private MaterialButton btnSendMoney;
    private CircularProgressIndicator progressSendMoney;
    private ApiService apiService;
    private MpesaTransactionsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Inflating mpesa_transaction_1");
        View view = inflater.inflate(R.layout.mpesa_transaction_1, container, false);

        // Bind UI
        etSenderPaybill = view.findViewById(R.id.etSenderPaybill);
        etRecipientPhone = view.findViewById(R.id.etPhone);
        etAmount = view.findViewById(R.id.etAmount);
        etRate = view.findViewById(R.id.etRate);
        etDescription = view.findViewById(R.id.etDescription);
        btnSendMoney = view.findViewById(R.id.btnSendMoney);
        progressSendMoney = view.findViewById(R.id.progressSendMoney);

        // ✅ Updated to pass context to ensure X-User-Id header is added
        apiService = ApiClient.getApiService(requireContext());

        // Shared ViewModel scoped to Activity
        viewModel = new ViewModelProvider(requireActivity())
                .get(MpesaTransactionsViewModel.class);

        // Restore draft values from ViewModel
        viewModel.getPaybillNumber().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.isEmpty()) etSenderPaybill.setText(val);
        });
        viewModel.getRecipientPhone1().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.isEmpty()) etRecipientPhone.setText(val);
        });
        viewModel.getAmount1().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.isEmpty()) etAmount.setText(val);
        });
        viewModel.getRate1().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.isEmpty()) etRate.setText(val);
        });
        viewModel.getDescription1().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.isEmpty()) etDescription.setText(val);
        });

        // Load last saved paybill from SharedPreferences (fallback)
        SharedPreferences prefs = requireContext().getSharedPreferences("mpesaPrefs", Context.MODE_PRIVATE);
        Set<String> paybills = prefs.getStringSet("senderPaybills", new HashSet<>());
        if (!paybills.isEmpty() && (viewModel.getPaybillNumber().getValue() == null || viewModel.getPaybillNumber().getValue().isEmpty())) {
            String first = paybills.iterator().next();
            etSenderPaybill.setText(first);
            viewModel.setPaybillNumber(first);
        }

        enableTemporaryParentIntercept(etRecipientPhone);
        enableTemporaryParentIntercept(etSenderPaybill);

        // Auto-scroll focused input into view + save drafts on blur
        NestedScrollView nested = view.findViewById(R.id.nested_scroll);
        if (nested != null) {
            View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
                if (hasFocus) {
                    nested.post(() -> {
                        int targetY = v.getTop();
                        int offset = getResources().getDimensionPixelSize(R.dimen.focus_scroll_offset);
                        nested.smoothScrollTo(0, Math.max(0, targetY - offset));
                    });
                } else {
                    String current = ((TextInputEditText) v).getText() != null ? ((TextInputEditText) v).getText().toString().trim() : "";
                    int id = v.getId();
                    if (id == R.id.etSenderPaybill) {
                        viewModel.setPaybillNumber(current);
                    } else if (id == R.id.etPhone) {
                        viewModel.setRecipientPhone1(current);
                    } else if (id == R.id.etAmount) {
                        viewModel.setAmount1(current);
                    } else if (id == R.id.etRate) {
                        viewModel.setRate1(current);
                    } else if (id == R.id.etDescription) {
                        viewModel.setDescription1(current);
                    }
                }
            };
            etSenderPaybill.setText("600998");
            etSenderPaybill.setEnabled(false); // makes it visible but inactive

            etRecipientPhone.setOnFocusChangeListener(focusListener);
            etAmount.setOnFocusChangeListener(focusListener);
            etRate.setOnFocusChangeListener(focusListener);
            etDescription.setOnFocusChangeListener(focusListener);
        }

        btnSendMoney.setOnClickListener(v -> sendMoney());

        return view;
    }

    private void enableTemporaryParentIntercept(View v) {
        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    private void sendMoney() {
        String senderPaybill = etSenderPaybill.getText() != null ? etSenderPaybill.getText().toString().trim() : "";
        String phone = etRecipientPhone.getText() != null ? etRecipientPhone.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String rateStr = etRate.getText() != null ? etRate.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (senderPaybill.isEmpty() || phone.isEmpty() || amountStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(getContext(), "Paybill, Phone, Amount and Rate are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        double rate;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(getContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException ex) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            rate = Double.parseDouble(rateStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(getContext(), "Invalid rate", Toast.LENGTH_SHORT).show();
            return;
        }

        String normalizedPhone = normalizeMsisdn(phone);
        if (normalizedPhone == null || !normalizedPhone.matches("^2547\\d{8}$")) {
            Toast.makeText(getContext(), "Enter a valid Kenyan mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        Selling selling = new Selling();
        selling.setPhoneNo(normalizedPhone);
        selling.setSellingAmount(amount);
        selling.setSellingRate(rate);
        selling.setSellDescription(description);

        Log.i(TAG, "Prepared selling request: paybill=" + senderPaybill + " phone=" + normalizedPhone + " amount=" + amount + " rate=" + rate);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Transaction" + "\n"  + "Do you wish to proceed with this transaction?")
                .setMessage("\n" + "Paybill: " + senderPaybill + "\n" + "Phone: " + normalizedPhone + "\n" + "Amount: " + amount + "\n" + "Rate: " + rate + "\n")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    btnSendMoney.setEnabled(false);
                    btnSendMoney.setText("Sending...");
                    progressSendMoney.setVisibility(View.VISIBLE);
                    progressSendMoney.setIndeterminate(true);

                    Call<Selling> call = apiService.sendMoneyDaraja(selling);
                    call.enqueue(new Callback<Selling>() {
                        @Override
                        public void onResponse(@NonNull Call<Selling> call, @NonNull Response<Selling> response) {
                            resetButtonState();
                            if (response.isSuccessful() && response.body() != null) {
                                Selling resp = response.body();
                                String txId = resp.getTransactionId() != null ? resp.getTransactionId() : "N/A";
                                Toast.makeText(getContext(), "Transaction Success: " + txId, Toast.LENGTH_LONG).show();
                                Log.i(TAG, "Transaction success, transactionId=" + txId);

                                // Add transaction to ViewModel
                                viewModel.addTransaction(resp);

                                clearForm();
                            } else {
                                Toast.makeText(getContext(), "Transaction Failed", Toast.LENGTH_LONG).show();
                                Log.w(TAG, "Transaction failed. HTTP " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Selling> call, @NonNull Throwable t) {
                            resetButtonState();
//                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            Toast.makeText(getContext(), "Error: The server is offline", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "sendMoney request failed", t);
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    // Do not clear form on cancel to preserve draft
                })
                .show();
    }

    private void resetButtonState() {
        btnSendMoney.setEnabled(true);
        btnSendMoney.setText("Send Money");
        progressSendMoney.setVisibility(View.GONE);
    }

    private void clearForm() {
//        etSenderPaybill.setText("");
        etRecipientPhone.setText("");
        etAmount.setText("");
        etRate.setText("");
        etDescription.setText("");

        // Also clear drafts in ViewModel
        viewModel.setPaybillNumber("");
        viewModel.setRecipientPhone1("");
        viewModel.setAmount1("");
        viewModel.setRate1("");
        viewModel.setDescription1("");
    }

    private String normalizeMsisdn(String msisdn) {
        if (msisdn == null) return null;
        msisdn = msisdn.replaceAll("\\s+", "").replaceAll("^\\+", "");
        if (msisdn.matches("^0\\d+")) {
            msisdn = msisdn.replaceFirst("^0", "254");
        }
        if (msisdn.matches("^7\\d{8}$")) {
            msisdn = "254" + msisdn;
        }
        msisdn = msisdn.replaceAll("\\D+", "");
        return msisdn;
    }
}
