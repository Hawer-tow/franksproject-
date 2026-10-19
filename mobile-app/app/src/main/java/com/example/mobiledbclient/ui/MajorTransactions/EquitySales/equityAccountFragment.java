package com.example.mobiledbclient.ui.MajorTransactions.EquitySales;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class equityAccountFragment extends Fragment {

    private AutoCompleteTextView edtSender;
    private TextInputEditText edtReceiver;
    private TextInputEditText edtAmount;
    private TextInputEditText edtRate;
    private MaterialButton btnSendMoney;

    private AlertDialog loadingDialog;
    private EquitySalesViewModel viewModel;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equity_account_to_account, container, false);

        // Bind views
        edtSender = view.findViewById(R.id.edtSender);
        edtReceiver = view.findViewById(R.id.edtReceiver);
        edtAmount = view.findViewById(R.id.edtAmount);
        edtRate = view.findViewById(R.id.edtRate);
        btnSendMoney = view.findViewById(R.id.btnSendMoney);

        // Fragment-scoped ViewModel so account and phone cards don’t share state
        viewModel = new ViewModelProvider(this).get(EquitySalesViewModel.class);

        // Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        // Populate sender account dropdown
        String[] accounts = new String[]{"1450160649886", "1450160649887", "1450160649888"};
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, accounts);
        edtSender.setAdapter(accountAdapter);

        // Restore values from ViewModel if available
        if (viewModel.getAccountSender().getValue() != null) {
            edtSender.setText(viewModel.getAccountSender().getValue(), false);
        }
        if (viewModel.getAccountReceiver().getValue() != null) {
            edtReceiver.setText(viewModel.getAccountReceiver().getValue());
        }
        if (viewModel.getAccountAmount().getValue() != null) {
            edtAmount.setText(viewModel.getAccountAmount().getValue());
        }
        if (viewModel.getAccountRate().getValue() != null) {
            edtRate.setText(viewModel.getAccountRate().getValue());
        }

        // Loading dialog
        loadingDialog = new AlertDialog.Builder(requireContext())
                .setView(R.layout.rates_loading_dialog)
                .setCancelable(false)
                .create();

        // Button click
        btnSendMoney.setOnClickListener(v -> {
            String sender = edtSender.getText().toString().trim();
            String receiver = edtReceiver.getText().toString().trim();
            String amountStr = edtAmount.getText().toString().trim();
            String rateStr = edtRate.getText().toString().trim();

            if (sender.isEmpty() || receiver.isEmpty() || amountStr.isEmpty() || rateStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading(true);

            // Build the FLAT request object that matches the server's UserStkPushInput
            EquityAccountSendMoneyRequest request = new EquityAccountSendMoneyRequest();
            request.setSenderAccount(sender);
            request.setRecipientAccount(receiver);
            request.setAmount(amountStr);
            request.setRate(rateStr);
            request.setTelco("BANK"); // Identify this as a bank transfer

            // Call API
            apiService.sendToAccount(request).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    showLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Transfer Initiated: " + response.body(), Toast.LENGTH_LONG).show();
                        clearInputs();
                    } else {
                        // If it's a 500 error, read the error body
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";
                            Toast.makeText(getContext(), "Failed: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    showLoading(false);
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        return view;
    }

    private void showLoading(boolean show) {
        if (loadingDialog == null) return;
        if (show) {
            try {
                if (!loadingDialog.isShowing()) loadingDialog.show();
            } catch (Exception ignored) { }
        } else {
            try {
                if (loadingDialog.isShowing()) loadingDialog.dismiss();
            } catch (Exception ignored) { }
        }
    }

    private void clearInputs() {
        edtSender.setText("");
        edtReceiver.setText("");
        edtAmount.setText("");
        edtRate.setText("");
    }

    private void hideKeyboard() {
        if (getActivity() == null) return;
        View view = getActivity().getCurrentFocus();
        if (view == null) view = getView();
        if (view == null) return;

        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try { loadingDialog.dismiss(); } catch (Exception ignored) {}
        }
    }
}
