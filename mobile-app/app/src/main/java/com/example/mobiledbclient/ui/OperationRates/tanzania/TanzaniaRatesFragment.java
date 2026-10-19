package com.example.mobiledbclient.ui.OperationRates.tanzania;

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

public class TanzaniaRatesFragment extends Fragment {

    private TextInputEditText u2tRate, u2tMin, u2tMax, t2uRate, t2uMin, t2uMax, U2TDate, T2UDate;
    private Button btnU2TSet, btnT2USet;
    private Tanzania2UgxRate_db_instance tanzaniaDb;
    private Ugx2TanzaniaRate_db_instance ugxDb;
    private ApiService apiService;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tanzania_rate_detail, container, false);

        // Bind Material inputs (IDs unchanged)
        U2TDate = view.findViewById(R.id.U2TDate);
        u2tRate = view.findViewById(R.id.U2Trate);
        u2tMin = view.findViewById(R.id.U2Tminimum);
        u2tMax = view.findViewById(R.id.U2Tmaximum);
        btnU2TSet = view.findViewById(R.id.U2Tsetbtn);

        T2UDate = view.findViewById(R.id.T2UDate);
        t2uRate = view.findViewById(R.id.T2Urate);
        t2uMin = view.findViewById(R.id.T2Umimimum);
        t2uMax = view.findViewById(R.id.T2Umaximum);
        btnT2USet = view.findViewById(R.id.T2Usetbtn);

        tanzaniaDb = Tanzania2UgxRate_db_instance.getInstance(requireContext());
        ugxDb = Ugx2TanzaniaRate_db_instance.getInstance(requireContext());
        apiService = ApiClient.getClient().create(ApiService.class);

        // Pre-populate dates
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        U2TDate.setText(today);
        T2UDate.setText(today);

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

        U2TDate.setOnClickListener(showDatePicker);
        T2UDate.setOnClickListener(showDatePicker);

        // Load existing U2T record
        new Thread(() -> {
            String date = today;
            Ugx2TanzaniaRate existing = ugxDb.Ugx2TanzaniaRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                U2TDate.setText(date);
                if (existing != null) {
                    u2tRate.setText(String.valueOf(existing.ugx2tanzaniarate));
                    u2tMin.setText(String.valueOf(existing.min_transactionvalue));
                    u2tMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    u2tRate.setText("0");
                    u2tMin.setText("0");
                    u2tMax.setText("0");
                }
            });
        }).start();

        // Load existing T2U record
        new Thread(() -> {
            String date = today;
            Tanzania2UgxRate existing = tanzaniaDb.Tanzania2UgxRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                T2UDate.setText(date);
                if (existing != null) {
                    t2uRate.setText(String.valueOf(existing.tanzania2Ugxrate));
                    t2uMin.setText(String.valueOf(existing.min_transactionvalue));
                    t2uMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    t2uRate.setText("0");
                    t2uMin.setText("0");
                    t2uMax.setText("0");
                }
            });
        }).start();

        btnU2TSet.setOnClickListener(v -> saveU2T());
        btnT2USet.setOnClickListener(v -> saveT2U());

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

    private void saveU2T() {
        String date = U2TDate.getText() != null ? U2TDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            U2TDate.setError("Date cannot be empty");
            U2TDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(u2tRate.getText() != null ? u2tRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(u2tMin.getText() != null ? u2tMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(u2tMax.getText() != null ? u2tMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Uganda → Tanzania rate...");

        new Thread(() -> {
            Ugx2TanzaniaRate existing = ugxDb.Ugx2TanzaniaRateDao().getRateByDate(date);
            if (existing != null) {
                existing.ugx2tanzaniarate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                ugxDb.Ugx2TanzaniaRateDao().update(existing);
                pushU2T(existing);
            } else {
                Ugx2TanzaniaRate newRate = new Ugx2TanzaniaRate();
                newRate.ugx2tanzaniaratedate = date;
                newRate.ugx2tanzaniarate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                ugxDb.Ugx2TanzaniaRateDao().insert(newRate);
                pushU2T(newRate);
            }
        }).start();
    }

    private void saveT2U() {
        String date = T2UDate.getText() != null ? T2UDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            T2UDate.setError("Date cannot be empty");
            T2UDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(t2uRate.getText() != null ? t2uRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(t2uMin.getText() != null ? t2uMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(t2uMax.getText() != null ? t2uMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Tanzania → Uganda rate...");

        new Thread(() -> {
            Tanzania2UgxRate existing = tanzaniaDb.Tanzania2UgxRateDao().getRateByDate(date);
            if (existing != null) {
                existing.tanzania2Ugxrate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                tanzaniaDb.Tanzania2UgxRateDao().update(existing);
                pushT2U(existing);
            } else {
                Tanzania2UgxRate newRate = new Tanzania2UgxRate();
                newRate.tanzania2Ugxratedate = date;
                newRate.tanzania2Ugxrate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                tanzaniaDb.Tanzania2UgxRateDao().insert(newRate);
                pushT2U(newRate);
            }
        }).start();
    }

    private void pushU2T(Ugx2TanzaniaRate rate) {
        apiService.addUgx2TanzaniaRate(rate).enqueue(new Callback<Ugx2TanzaniaRate>() {
            @Override
            public void onResponse(Call<Ugx2TanzaniaRate> call, Response<Ugx2TanzaniaRate> response) {
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
            public void onFailure(Call<Ugx2TanzaniaRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(),
                            "Server not available, saved locally",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void pushT2U(Tanzania2UgxRate rate) {
        apiService.addTanzania2UgxRate(rate).enqueue(new Callback<Tanzania2UgxRate>() {
            @Override
            public void onResponse(Call<Tanzania2UgxRate> call, Response<Tanzania2UgxRate> response) {
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
            public void onFailure(Call<Tanzania2UgxRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(),
                            "Server not available, saved locally",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private double parseDoubleOrZero(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
