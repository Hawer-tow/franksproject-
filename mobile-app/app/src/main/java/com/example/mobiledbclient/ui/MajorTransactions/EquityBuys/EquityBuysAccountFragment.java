package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class EquityBuysAccountFragment extends Fragment {

    private EquityBuysViewModel viewModel;
    private TextInputEditText etAccount, etAmount, etRate;
    private MaterialButton btnInitiate;
    private CircularProgressIndicator progressIndicator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equity_buys_account_card, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(EquityBuysViewModel.class);

        etAccount = view.findViewById(R.id.etAccountEquity);
        etAmount = view.findViewById(R.id.etAmountEquityAccount);
        etRate = view.findViewById(R.id.etRateEquityAccount);
        btnInitiate = view.findViewById(R.id.btnInitiateEquityAccount);
        progressIndicator = view.findViewById(R.id.progressEquityAccount);

        btnInitiate.setOnClickListener(v -> {
            progressIndicator.setVisibility(View.VISIBLE);
            btnInitiate.setEnabled(false);

            viewModel.setAccountNumber(etAccount.getText() != null ? etAccount.getText().toString().trim() : "");
            viewModel.setAccountAmount(etAmount.getText() != null ? etAmount.getText().toString().trim() : "");
            viewModel.setAccountRate(etRate.getText() != null ? etRate.getText().toString().trim() : "");

            // TODO: trigger API call or confirmation dialog here

            progressIndicator.setVisibility(View.GONE);
            btnInitiate.setEnabled(true);
        });

        return view;
    }
}
