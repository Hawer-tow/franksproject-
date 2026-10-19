package com.example.mobiledbclient.ui.MajorTransactions.EquitySales;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class equityPhoneFragment extends Fragment {

    private AutoCompleteTextView edtAccountSend;
    private AutoCompleteTextView edtTelcoSend;
    private TextInputEditText edtRecipientPhone;
    private TextInputEditText edtAmountSend;
    private TextInputEditText edtRateSend;
    private MaterialButton btnSendMoney;

    private EquitySalesViewModel viewModel;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equity_account_to_phone, container, false);

        // Bind views
        edtAccountSend = view.findViewById(R.id.edtAccountSend);
        edtTelcoSend = view.findViewById(R.id.edtTelcoSend);
        edtRecipientPhone = view.findViewById(R.id.edtRecipientPhone);
        edtAmountSend = view.findViewById(R.id.edtAmountSend);
        edtRateSend = view.findViewById(R.id.edtRateSend);
        btnSendMoney = view.findViewById(R.id.btnSendMoney);

        // ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(EquitySalesViewModel.class);

        // Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        // Populate account dropdown
        String[] accounts = new String[]{"1450160649886", "1450160649887", "1450160649888"};
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, accounts);
        edtAccountSend.setAdapter(accountAdapter);

        // Populate telco dropdown
        String[] telcos = new String[]{"MPESA", "AIRTELMONEY"};
        ArrayAdapter<String> telcoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, telcos);
        edtTelcoSend.setAdapter(telcoAdapter);

        // Scroll focus behavior
        NestedScrollView nested = view.findViewById(R.id.nested_scroll_sendmoney);
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && nested != null) {
                nested.post(() -> nested.smoothScrollTo(0, v.getTop()));
            }
        };
        edtRecipientPhone.setOnFocusChangeListener(focusListener);
        edtAmountSend.setOnFocusChangeListener(focusListener);
        edtRateSend.setOnFocusChangeListener(focusListener);

        // Restore values from ViewModel if available
        if (viewModel.getPhoneAccount().getValue() != null) {
            edtAccountSend.setText(viewModel.getPhoneAccount().getValue(), false);
        }
        if (viewModel.getPhoneTelco().getValue() != null) {
            edtTelcoSend.setText(viewModel.getPhoneTelco().getValue(), false);
        }
        if (viewModel.getPhoneNumber().getValue() != null) {
            edtRecipientPhone.setText(viewModel.getPhoneNumber().getValue());
        }
        if (viewModel.getPhoneAmount().getValue() != null) {
            edtAmountSend.setText(viewModel.getPhoneAmount().getValue());
        }
        if (viewModel.getPhoneRate().getValue() != null) {
            edtRateSend.setText(viewModel.getPhoneRate().getValue());
        }

        // Button click
        btnSendMoney.setOnClickListener(v -> {
            String account = edtAccountSend.getText() != null ? edtAccountSend.getText().toString().trim() : "";
            String telco = edtTelcoSend.getText() != null ? edtTelcoSend.getText().toString().trim() : "";
            String phone = edtRecipientPhone.getText() != null ? edtRecipientPhone.getText().toString().trim() : "";
            String amountStr = edtAmountSend.getText() != null ? edtAmountSend.getText().toString().trim() : "";
            String rateStr = edtRateSend.getText() != null ? edtRateSend.getText().toString().trim() : "";

            if (account.isEmpty() || telco.isEmpty() || phone.isEmpty() || amountStr.isEmpty() || rateStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update ViewModel fields (for persistence)
            viewModel.setPhoneAccount(account);
            viewModel.setPhoneTelco(telco);
            viewModel.setPhoneNumber(phone);
            viewModel.setPhoneAmount(amountStr);
            viewModel.setPhoneRate(rateStr);

            // Show confirmation popup before sending
            showConfirmationDialog(account, telco, phone, amountStr, rateStr);
        });

        return view;
    }

    /**
     * Confirmation popup dialog
     */
    private void showConfirmationDialog(String account, String telco, String phone, String amount, String rate) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_confirm_sendmoney, null);

        TextView tvAccount = dialogView.findViewById(R.id.tvAccountConfirm);
        TextView tvTelco = dialogView.findViewById(R.id.tvTelcoConfirm);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhoneConfirm);
        TextView tvAmount = dialogView.findViewById(R.id.tvAmountConfirm);
        TextView tvRate = dialogView.findViewById(R.id.tvRateConfirm);

        tvAccount.setText("Account: " + account);
        tvTelco.setText("Telco: " + telco);
        tvPhone.setText("Phone: " + phone);
        tvAmount.setText("Amount: " + amount);
        tvRate.setText("Rate: " + rate);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Send Money")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> performSendMoneyRequest(account, phone, amount, telco, rate))
                .setNegativeButton("Cancel", (dialog, which) ->
                        Toast.makeText(getContext(), "Please correct your details.", Toast.LENGTH_SHORT).show()
                )
                .show();
    }

    /**
     * Actually performs the API call
     */
    private void performSendMoneyRequest(String account, String phone, String amount, String telco, String rate) {
        SendMoneyRequest request = new SendMoneyRequest(account, phone, amount, telco, rate);

        Call<String> call = apiService.sendMoney(request);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Send Money request sent: " + response.body(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getContext(), "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
