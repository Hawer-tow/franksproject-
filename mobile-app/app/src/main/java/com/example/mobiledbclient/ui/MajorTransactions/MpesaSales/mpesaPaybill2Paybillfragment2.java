package com.example.mobiledbclient.ui.MajorTransactions.MpesaSales;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobiledbclient.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;
import java.util.Set;

public class mpesaPaybill2Paybillfragment2 extends Fragment {

    private static final String TAG = "MpesaPhoneToPhoneFragment";

    private TextInputEditText etSenderPaybill;
    private TextInputEditText etRecipientPaybill;
    private TextInputEditText etRecipientPhone;
    private TextInputEditText etAmount;
    private TextInputEditText etRate;
    private TextInputEditText etDescription;

    private CircularProgressIndicator progressIndicator;

    private MpesaTransactionsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Inflating mpesa_transaction_2 layout");
        View view = inflater.inflate(R.layout.mpesa_transaction_2, container, false);

        // Bind UI
        etSenderPaybill = view.findViewById(R.id.etSenderPaybill);
        etRecipientPaybill = view.findViewById(R.id.etRecipientPaybill);
        etRecipientPhone = view.findViewById(R.id.etPhone);
        etAmount = view.findViewById(R.id.etAmount);
        etRate = view.findViewById(R.id.etRate);
        etDescription = view.findViewById(R.id.etDescription);
        progressIndicator = view.findViewById(R.id.progressSendMoney);

        viewModel = new ViewModelProvider(requireActivity())
                .get(MpesaTransactionsViewModel.class);

        // Restore drafts from ViewModel
        viewModel.getSenderPaybill().observe(getViewLifecycleOwner(), val -> setTextIfNotEmpty(etSenderPaybill, val));
        viewModel.getRecipientPaybill().observe(getViewLifecycleOwner(), val -> setTextIfNotEmpty(etRecipientPaybill, val));
        viewModel.getRecipientPhone().observe(getViewLifecycleOwner(), val -> setTextIfNotEmpty(etRecipientPhone, val));
        viewModel.getAmount().observe(getViewLifecycleOwner(), val -> setTextIfNotEmpty(etAmount, val));
        viewModel.getRate().observe(getViewLifecycleOwner(), val -> setTextIfNotEmpty(etRate, val));
        viewModel.getDescription().observe(getViewLifecycleOwner(), val -> setTextIfNotEmpty(etDescription, val));

        // Load last saved sender paybill from SharedPreferences (fallback)
        SharedPreferences prefs = requireContext().getSharedPreferences("mpesaPrefs", Context.MODE_PRIVATE);
        Set<String> paybills = prefs.getStringSet("shopPaybills", new HashSet<>());
        if (!paybills.isEmpty() && (viewModel.getSenderPaybill().getValue() == null || viewModel.getSenderPaybill().getValue().isEmpty())) {
            String first = paybills.iterator().next();
            etSenderPaybill.setText(first);
            viewModel.setSenderPaybill(first);
        }

        // Auto-scroll + Save drafts combined listener
        NestedScrollView nested = view.findViewById(R.id.nested_scroll);
        View.OnFocusChangeListener combinedListener = (v, hasFocus) -> {
            if (hasFocus && nested != null) {
                nested.post(() -> {
                    int targetY = v.getTop();
                    int offset = getResources().getDimensionPixelSize(R.dimen.focus_scroll_offset);
                    nested.smoothScrollTo(0, Math.max(0, targetY - offset));
                });
            } else if (!hasFocus) {
                String current = ((TextInputEditText) v).getText() != null
                        ? ((TextInputEditText) v).getText().toString().trim()
                        : "";

                if (v.getId() == R.id.etSenderPaybill) {
                    viewModel.setSenderPaybill(current);
                } else if (v.getId() == R.id.etRecipientPaybill) {
                    viewModel.setRecipientPaybill(current);
                } else if (v.getId() == R.id.etPhone) {
                    viewModel.setRecipientPhone(current);
                } else if (v.getId() == R.id.etAmount) {
                    viewModel.setAmount(current);
                } else if (v.getId() == R.id.etRate) {
                    viewModel.setRate(current);
                } else if (v.getId() == R.id.etDescription) {
                    viewModel.setDescription(current);
                }

            }
        };

        // Attach combined listener to all fields
        etSenderPaybill.setOnFocusChangeListener(combinedListener);
        etRecipientPaybill.setOnFocusChangeListener(combinedListener);
        etRecipientPhone.setOnFocusChangeListener(combinedListener);
        etAmount.setOnFocusChangeListener(combinedListener);
        etRate.setOnFocusChangeListener(combinedListener);
        etDescription.setOnFocusChangeListener(combinedListener);

        // Prevent inputs from blocking horizontal swipes
        enableTemporaryParentIntercept(etRecipientPhone);

        // Handle Send button
        view.findViewById(R.id.btnSendMoney).setOnClickListener(v -> {
            if (validateInputs()) {
                showLoading(true);
                viewModel.sendTransaction(
                        etSenderPaybill.getText().toString().trim(),
                        etRecipientPaybill.getText().toString().trim(),
                        etRecipientPhone.getText().toString().trim(),
                        etAmount.getText().toString().trim(),
                        etRate.getText().toString().trim(),
                        etDescription.getText().toString().trim(),
                        success -> showLoading(false) // callback to hide loader
                );
            }
        });

        return view;
    }

    private void setTextIfNotEmpty(TextInputEditText editText, String val) {
        if (val != null && !val.isEmpty()) {
            editText.setText(val);
        }
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

    private boolean validateInputs() {
        boolean valid = true;
        if (etRecipientPaybill.getText().toString().trim().isEmpty()) {
            setError(etRecipientPaybill, "Recipient paybill required");
            valid = false;
        }
        if (etAmount.getText().toString().trim().isEmpty()) {
            setError(etAmount, "Amount required");
            valid = false;
        }
        return valid;
    }

    private void setError(TextInputEditText editText, String error) {
        TextInputLayout layout = (TextInputLayout) editText.getParent().getParent();
        layout.setError(error);
    }

    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
