package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EquityBuysPhoneFragment extends Fragment {

    private EquityBuysViewModel viewModel;
    private TextInputEditText etPhone, etAmount, etRate;
    private MaterialButton btnInitiate;
    private CircularProgressIndicator progressIndicator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equity_buys_phone_card, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(EquityBuysViewModel.class);

        etPhone = view.findViewById(R.id.etPhoneEquity);
        etAmount = view.findViewById(R.id.etAmountEquity);
        etRate = view.findViewById(R.id.etRateEquity);
        btnInitiate = view.findViewById(R.id.btnInitiateEquityPhone);
        progressIndicator = view.findViewById(R.id.progressEquityPhone);

        AutoCompleteTextView etTelco = view.findViewById(R.id.etTelcoEquity);

        String[] telcos = new String[]{"Safaricom", "Equitel"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                R.layout.telco_dropdown_item,
                telcos
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(R.id.tvTelcoItem);

                String telco = getItem(position);
                if ("Safaricom".equals(telco)) {
                    tv.setTextColor(getResources().getColor(R.color.Safaricom_shade));
                } else if ("Equitel".equals(telco)) {
                    tv.setTextColor(getResources().getColor(R.color.Equitel_shade));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                return getView(position, convertView, parent);
            }
        };

        etTelco.setAdapter(adapter);
        etTelco.setText("Safaricom", false);
        viewModel.setPhoneTelco("Safaricom");

        // Observe ViewModel
        viewModel.getPhoneTelco().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.equals(etTelco.getText().toString())) {
                etTelco.setText(val, false);
            }
        });
        viewModel.getPhoneNumber().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.equals(etPhone.getText().toString())) {
                etPhone.setText(val);
            }
        });
        viewModel.getPhoneAmount().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.equals(etAmount.getText().toString())) {
                etAmount.setText(val);
            }
        });
        viewModel.getPhoneRate().observe(getViewLifecycleOwner(), val -> {
            if (val != null && !val.equals(etRate.getText().toString())) {
                etRate.setText(val);
            }
        });

        // Save input changes into ViewModel
        etPhone.addTextChangedListener(new SimpleWatcher(text -> viewModel.setPhoneNumber(text)));
        etAmount.addTextChangedListener(new SimpleWatcher(text -> viewModel.setPhoneAmount(text)));
        etRate.addTextChangedListener(new SimpleWatcher(text -> viewModel.setPhoneRate(text)));
        etTelco.setOnItemClickListener((parent, v1, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            viewModel.setPhoneTelco(selected);
        });

        // Show confirmation dialog before initiating payment
        btnInitiate.setOnClickListener(v -> showConfirmationDialog());

        return view;
    }

    private void showConfirmationDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.equitytelcostkpushconfirmation, null);

        TextView tvTelco = dialogView.findViewById(R.id.tvTelcoConfirm);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhoneConfirm);
        TextView tvAmount = dialogView.findViewById(R.id.tvAmountConfirm);
        TextView tvRate = dialogView.findViewById(R.id.tvRateConfirm);

        String telco = viewModel.getPhoneTelco().getValue();
        if (telco == null || telco.isEmpty()) {
            telco = "Safaricom"; // fallback
        }

        tvTelco.setText("Telco: " + telco);
        tvPhone.setText("Phone: " + viewModel.getPhoneNumber().getValue());
        tvAmount.setText("Amount: " + viewModel.getPhoneAmount().getValue());
        tvRate.setText("Rate: " + viewModel.getPhoneRate().getValue());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Payment")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> initiatePayment())
                .setNegativeButton("Cancel", (dialog, which) ->
                        Toast.makeText(getContext(), "Please correct your details.", Toast.LENGTH_SHORT).show()
                )
                .show();
    }



    private void initiatePayment() {
        progressIndicator.setVisibility(View.VISIBLE);
        btnInitiate.setText("Processing payment...");
        btnInitiate.setEnabled(false);

        StkPushRequest request = new StkPushRequest();
        request.setPhoneNumber(viewModel.getPhoneNumber().getValue());
        request.setAmount(viewModel.getPhoneAmount().getValue());
        request.setRate(viewModel.getPhoneRate().getValue());
        request.setTelco(viewModel.getPhoneTelco().getValue());

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<StkPushResponse> call = apiService.initiateEquityStkPush(request);

        call.enqueue(new Callback<StkPushResponse>() {
            @Override
            public void onResponse(Call<StkPushResponse> call, Response<StkPushResponse> response) {
                progressIndicator.setVisibility(View.GONE);
                btnInitiate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    btnInitiate.setText("Payment Sent");
                    Toast.makeText(getContext(),
                            "Checkout ID: " + response.body().getCheckoutRequestId(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    btnInitiate.setText("Retry Payment");
                    Toast.makeText(getContext(),
                            "Error: " + response.message(),
                            Toast.LENGTH_SHORT).show();

                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("API_ERROR", "Code: " + response.code() + " Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Failed to read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<StkPushResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                btnInitiate.setEnabled(true);
                btnInitiate.setText("Retry Payment");
                Toast.makeText(getContext(),
                        "Failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class SimpleWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> consumer;

        SimpleWatcher(java.util.function.Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            consumer.accept(s.toString());
        }
    }
}
