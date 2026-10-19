package com.example.mobiledbclient.ui.OperationRates.rwanda;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RwandaRatesFragment extends Fragment {

    private TextInputEditText u2rRate, u2rMin, u2rMax, r2uRate, r2uMin, r2uMax, U2RDate, R2UDate;
    private Button btnU2RSet, btnR2USet;
    private Rwanda2UgxRate_db_instance rwandaDb;
    private Ugx2RwandaRate_db_instance ugxDb;
    private ApiService apiService;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rwanda_rate_detail, container, false);

        // Bind Material TextInputEditText fields (IDs unchanged)
        U2RDate = view.findViewById(R.id.U2RDate);
        u2rRate = view.findViewById(R.id.U2Rrate);
        u2rMin = view.findViewById(R.id.U2Rminimum);
        u2rMax = view.findViewById(R.id.U2Rmaximum);
        btnU2RSet = view.findViewById(R.id.U2Rsetbtn);

        R2UDate = view.findViewById(R.id.R2UDate);
        r2uRate = view.findViewById(R.id.R2Urate);
        r2uMin = view.findViewById(R.id.R2Umimimum);
        r2uMax = view.findViewById(R.id.R2Umaximum);
        btnR2USet = view.findViewById(R.id.R2Usetbtn);

        rwandaDb = Rwanda2UgxRate_db_instance.getInstance(requireContext());
        ugxDb = Ugx2RwandaRate_db_instance.getInstance(requireContext());
        apiService = ApiClient.getClient().create(ApiService.class);

        // Pre-populate dates with today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        U2RDate.setText(today);
        R2UDate.setText(today);

        // Date pickers
        View.OnClickListener showDatePicker = v -> {
            final Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dp = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                ((TextInputEditText) v).setText(formatted);
            }, y, m, d);
            dp.show();
        };

        U2RDate.setOnClickListener(showDatePicker);
        R2UDate.setOnClickListener(showDatePicker);

        // Load existing U2R record (background thread)
        new Thread(() -> {
            String date = today;
            Ugx2RwandaRate existing = ugxDb.Ugx2RwandaRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                U2RDate.setText(date);
                if (existing != null) {
                    u2rRate.setText(String.valueOf(existing.ugx2rwandarate));
                    u2rMin.setText(String.valueOf(existing.min_transactionvalue));
                    u2rMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    u2rRate.setText("0");
                    u2rMin.setText("0");
                    u2rMax.setText("0");
                }
            });
        }).start();

        // Load existing R2U record (background thread)
        new Thread(() -> {
            String date = today;
            Rwanda2UgxRate existing = rwandaDb.Rwanda2UgxRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                R2UDate.setText(date);
                if (existing != null) {
                    r2uRate.setText(String.valueOf(existing.rwanda2Ugxrate));
                    r2uMin.setText(String.valueOf(existing.min_transactionvalue));
                    r2uMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    r2uRate.setText("0");
                    r2uMin.setText("0");
                    r2uMax.setText("0");
                }
            });
        }).start();

        // Button listeners
        btnU2RSet.setOnClickListener(v -> saveU2R());
        btnR2USet.setOnClickListener(v -> saveR2U());

        return view;
    }

    private void showLoadingDialog(String message) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.rates_loading_dialog, null);
        TextView loadingText = dialogView.findViewById(R.id.loadingText);
        loadingText.setText(message);

        loadingDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void saveU2R() {
        String date = U2RDate.getText() != null ? U2RDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            U2RDate.setError("Date cannot be empty");
            U2RDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(u2rRate.getText() != null ? u2rRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(u2rMin.getText() != null ? u2rMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(u2rMax.getText() != null ? u2rMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Uganda → Rwanda rate...");

        new Thread(() -> {
            Ugx2RwandaRate existing = ugxDb.Ugx2RwandaRateDao().getRateByDate(date);
            if (existing != null) {
                existing.ugx2rwandarate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                ugxDb.Ugx2RwandaRateDao().update(existing);
                pushU2R(existing);
            } else {
                Ugx2RwandaRate newRate = new Ugx2RwandaRate();
                newRate.ugx2rwandaratedate = date;
                newRate.ugx2rwandarate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                ugxDb.Ugx2RwandaRateDao().insert(newRate);
                pushU2R(newRate);
            }
        }).start();
    }

    private void saveR2U() {
        String date = R2UDate.getText() != null ? R2UDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            R2UDate.setError("Date cannot be empty");
            R2UDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(r2uRate.getText() != null ? r2uRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(r2uMin.getText() != null ? r2uMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(r2uMax.getText() != null ? r2uMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Rwanda → Uganda rate...");

        new Thread(() -> {
            Rwanda2UgxRate existing = rwandaDb.Rwanda2UgxRateDao().getRateByDate(date);
            if (existing != null) {
                existing.rwanda2Ugxrate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                rwandaDb.Rwanda2UgxRateDao().update(existing);
                pushR2U(existing);
            } else {
                Rwanda2UgxRate newRate = new Rwanda2UgxRate();
                newRate.rwanda2Ugxratedate = date;
                newRate.rwanda2Ugxrate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                rwandaDb.Rwanda2UgxRateDao().insert(newRate);
                pushR2U(newRate);
            }
        }).start();
    }

    private double parseDoubleOrZero(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private void pushU2R(Ugx2RwandaRate rate) {
        apiService.addUgx2RwandaRate(rate).enqueue(new Callback<Ugx2RwandaRate>() {
            @Override
            public void onResponse(Call<Ugx2RwandaRate> call, Response<Ugx2RwandaRate> response) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Saved successfully to server", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Saved locally, server error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Ugx2RwandaRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(),
                            "Server not available, saved locally",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void pushR2U(Rwanda2UgxRate rate) {
        apiService.addRwanda2UgxRate(rate).enqueue(new Callback<Rwanda2UgxRate>() {
            @Override
            public void onResponse(Call<Rwanda2UgxRate> call, Response<Rwanda2UgxRate> response) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Saved successfully to server",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Saved locally, server error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Rwanda2UgxRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(),
                            "Server not available, saved locally",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
