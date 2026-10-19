package com.example.mobiledbclient.ui.OperationRates.kenya;

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

public class KenyaRatesFragment extends Fragment {

    private TextInputEditText u2kRate, u2kMin, u2kMax, k2uRate, k2uMin, k2uMax, U2KDate, K2UDate;
    private Button btnU2KSet, btnK2USet;
    private Kenya2UgxRate_db_instance kenyaDb;
    private Ugx2KenyaRate_db_instance ugxDb;
    private ApiService apiService;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.kenya_rate_detail, container, false);

        // Bind views (TextInputEditText keeps same IDs as your XML)
        U2KDate = view.findViewById(R.id.U2KDate);
        u2kRate = view.findViewById(R.id.U2Krate);
        u2kMin = view.findViewById(R.id.U2Kminimum);
        u2kMax = view.findViewById(R.id.U2Kmaximum);
        btnU2KSet = view.findViewById(R.id.U2Ksetbtn);

        K2UDate = view.findViewById(R.id.K2UDate);
        k2uRate = view.findViewById(R.id.K2Urate);
        k2uMin = view.findViewById(R.id.K2Uminimum);
        k2uMax = view.findViewById(R.id.K2Umaximum);
        btnK2USet = view.findViewById(R.id.K2Usetbtn);

        kenyaDb = Kenya2UgxRate_db_instance.getInstance(requireContext());
        ugxDb = Ugx2KenyaRate_db_instance.getInstance(requireContext());
        apiService = ApiClient.getClient().create(ApiService.class);

        // Pre-populate dates with today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        U2KDate.setText(today);
        K2UDate.setText(today);

        // Date pickers for both date fields
        View.OnClickListener showDatePicker = v -> {
            final Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dp = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                ((TextInputEditText) v).setText(formatted);
            }, y, m, d);
            dp.show();
        };

        U2KDate.setOnClickListener(showDatePicker);
        K2UDate.setOnClickListener(showDatePicker);

        // Load existing U2K record (background thread)
        new Thread(() -> {
            String date = today;
            Ugx2KenyaRate existing = ugxDb.ugx2KenyaRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                U2KDate.setText(date);
                if (existing != null) {
                    u2kRate.setText(String.valueOf(existing.ugx2kenyarate));
                    u2kMin.setText(String.valueOf(existing.min_transactionvalue));
                    u2kMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    u2kRate.setText("0");
                    u2kMin.setText("0");
                    u2kMax.setText("0");
                }
            });
        }).start();

        // Load existing K2U record (background thread)
        new Thread(() -> {
            String date = today;
            Kenya2UgxRate existing = kenyaDb.Kenya2UgxRate_queries().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                K2UDate.setText(date);
                if (existing != null) {
                    k2uRate.setText(String.valueOf(existing.kenya2Ugxrate));
                    k2uMin.setText(String.valueOf(existing.min_transactionvalue));
                    k2uMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    k2uRate.setText("0");
                    k2uMin.setText("0");
                    k2uMax.setText("0");
                }
            });
        }).start();

        // Button listeners
        btnU2KSet.setOnClickListener(v -> saveU2K());
        btnK2USet.setOnClickListener(v -> saveK2U());

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

    private void saveU2K() {
        String date = U2KDate.getText() != null ? U2KDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            U2KDate.setError("Date cannot be empty");
            U2KDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(u2kRate.getText() != null ? u2kRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(u2kMin.getText() != null ? u2kMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(u2kMax.getText() != null ? u2kMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Uganda → Kenya rate...");

        new Thread(() -> {
            Ugx2KenyaRate existing = ugxDb.ugx2KenyaRateDao().getRateByDate(date);
            if (existing != null) {
                existing.ugx2kenyarate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                ugxDb.ugx2KenyaRateDao().update(existing);
                pushU2K(existing);
            } else {
                Ugx2KenyaRate newRate = new Ugx2KenyaRate();
                newRate.ugx2kenyaratedate = date;
                newRate.ugx2kenyarate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                ugxDb.ugx2KenyaRateDao().insert(newRate);
                pushU2K(newRate);
            }
        }).start();
    }

    private void pushU2K(Ugx2KenyaRate rate) {
        apiService.addUgx2KenyaRate(rate).enqueue(new Callback<Ugx2KenyaRate>() {
            @Override
            public void onResponse(Call<Ugx2KenyaRate> call, Response<Ugx2KenyaRate> response) {
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
            public void onFailure(Call<Ugx2KenyaRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(), "Server not available, saved locally", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveK2U() {
        String date = K2UDate.getText() != null ? K2UDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            K2UDate.setError("Date cannot be empty");
            K2UDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(k2uRate.getText() != null ? k2uRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(k2uMin.getText() != null ? k2uMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(k2uMax.getText() != null ? k2uMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Kenya → Uganda rate...");

        new Thread(() -> {
            Kenya2UgxRate existing = kenyaDb.Kenya2UgxRate_queries().getRateByDate(date);
            if (existing != null) {
                existing.kenya2Ugxrate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                kenyaDb.Kenya2UgxRate_queries().update(existing);
                pushK2U(existing);
            } else {
                Kenya2UgxRate newRate = new Kenya2UgxRate();
                newRate.kenya2Ugxratedate = date;
                newRate.kenya2Ugxrate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                kenyaDb.Kenya2UgxRate_queries().insert(newRate);
                pushK2U(newRate);
            }
        }).start();
    }

    private void pushK2U(Kenya2UgxRate rate) {
        apiService.addKenya2UgxRate(rate).enqueue(new Callback<Kenya2UgxRate>() {
            @Override
            public void onResponse(Call<Kenya2UgxRate> call, Response<Kenya2UgxRate> response) {
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
            public void onFailure(Call<Kenya2UgxRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(), "Server not available, saved locally", Toast.LENGTH_SHORT).show();
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
