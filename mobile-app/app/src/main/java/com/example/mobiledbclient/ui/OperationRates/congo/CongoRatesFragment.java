package com.example.mobiledbclient.ui.OperationRates.congo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class CongoRatesFragment extends Fragment {

    private TextInputEditText u2cRate, u2cMin, u2cMax, c2uRate, c2uMin, c2uMax, U2CDate, C2UDate;
    private View btnU2CSet, btnC2USet;
    private Congo2UgxRate_db_instance congoDb;
    private Ugx2CongoRate_db_instance ugxDb;
    private ApiService apiService;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.congo_rate_detail, container, false);

        // Bind Material inputs (IDs unchanged)
        U2CDate = view.findViewById(R.id.U2CDate);
        u2cRate = view.findViewById(R.id.U2Crate);
        u2cMin = view.findViewById(R.id.U2Cminimum);
        u2cMax = view.findViewById(R.id.U2Cmaximum);
        btnU2CSet = view.findViewById(R.id.U2Csetbtn);

        C2UDate = view.findViewById(R.id.C2UDate);
        c2uRate = view.findViewById(R.id.C2Urate);
        c2uMin = view.findViewById(R.id.C2Umimimum);
        c2uMax = view.findViewById(R.id.C2Umaximum);
        btnC2USet = view.findViewById(R.id.C2Usetbtn);

        // Flag overlay (optional tap)
        ImageView flag = view.findViewById(R.id.flagImage_cg);
        if (flag != null) {
            flag.setImageResource(R.drawable.congo_rates);
            flag.setOnClickListener(v -> Toast.makeText(requireContext(), "Congo rates", Toast.LENGTH_SHORT).show());
        }

        congoDb = Congo2UgxRate_db_instance.getInstance(requireContext());
        ugxDb = Ugx2CongoRate_db_instance.getInstance(requireContext());
        apiService = ApiClient.getClient().create(ApiService.class);

        // Pre-populate dates with today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        U2CDate.setText(today);
        C2UDate.setText(today);

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

        U2CDate.setOnClickListener(showDatePicker);
        C2UDate.setOnClickListener(showDatePicker);

        // Load existing U2C record (background thread)
        new Thread(() -> {
            String date = today;
            Ugx2CongoRate existing = ugxDb.Ugx2CongoRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                U2CDate.setText(date);
                if (existing != null) {
                    u2cRate.setText(String.valueOf(existing.ugx2congorate));
                    u2cMin.setText(String.valueOf(existing.min_transactionvalue));
                    u2cMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    u2cRate.setText("0");
                    u2cMin.setText("0");
                    u2cMax.setText("0");
                }
            });
        }).start();

        // Load existing C2U record (background thread)
        new Thread(() -> {
            String date = today;
            Congo2UgxRate existing = congoDb.Congo2UgxRateDao().getRateByDate(date);
            requireActivity().runOnUiThread(() -> {
                C2UDate.setText(date);
                if (existing != null) {
                    c2uRate.setText(String.valueOf(existing.congo2Ugxrate));
                    c2uMin.setText(String.valueOf(existing.min_transactionvalue));
                    c2uMax.setText(String.valueOf(existing.max_transactionValue));
                } else {
                    c2uRate.setText("0");
                    c2uMin.setText("0");
                    c2uMax.setText("0");
                }
            });
        }).start();

        btnU2CSet.setOnClickListener(v -> saveU2C());
        btnC2USet.setOnClickListener(v -> saveC2U());

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

    private void saveU2C() {
        String date = U2CDate.getText() != null ? U2CDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            U2CDate.setError("Date cannot be empty");
            U2CDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(u2cRate.getText() != null ? u2cRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(u2cMin.getText() != null ? u2cMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(u2cMax.getText() != null ? u2cMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Uganda → Congo rate...");

        new Thread(() -> {
            Ugx2CongoRate existing = ugxDb.Ugx2CongoRateDao().getRateByDate(date);
            if (existing != null) {
                existing.ugx2congorate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                ugxDb.Ugx2CongoRateDao().update(existing);
                pushU2C(existing);
            } else {
                Ugx2CongoRate newRate = new Ugx2CongoRate();
                newRate.ugx2congoratedate = date;
                newRate.ugx2congorate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                ugxDb.Ugx2CongoRateDao().insert(newRate);
                pushU2C(newRate);
            }
        }).start();
    }

    private void saveC2U() {
        String date = C2UDate.getText() != null ? C2UDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            C2UDate.setError("Date cannot be empty");
            C2UDate.requestFocus();
            return;
        }

        double rateVal = parseDoubleOrZero(c2uRate.getText() != null ? c2uRate.getText().toString().trim() : "");
        double minVal = parseDoubleOrZero(c2uMin.getText() != null ? c2uMin.getText().toString().trim() : "");
        double maxVal = parseDoubleOrZero(c2uMax.getText() != null ? c2uMax.getText().toString().trim() : "");

        showLoadingDialog("Saving Congo → Uganda rate...");

        new Thread(() -> {
            Congo2UgxRate existing = congoDb.Congo2UgxRateDao().getRateByDate(date);
            if (existing != null) {
                existing.congo2Ugxrate = rateVal;
                existing.min_transactionvalue = minVal;
                existing.max_transactionValue = maxVal;
                congoDb.Congo2UgxRateDao().update(existing);
                pushC2U(existing);
            } else {
                Congo2UgxRate newRate = new Congo2UgxRate();
                newRate.congo2Ugxratedate = date;
                newRate.congo2Ugxrate = rateVal;
                newRate.min_transactionvalue = minVal;
                newRate.max_transactionValue = maxVal;
                congoDb.Congo2UgxRateDao().insert(newRate);
                pushC2U(newRate);
            }
        }).start();
    }

    private void pushU2C(Ugx2CongoRate rate) {
        apiService.addUgx2CongoRate(rate).enqueue(new Callback<Ugx2CongoRate>() {
            @Override
            public void onResponse(Call<Ugx2CongoRate> call, Response<Ugx2CongoRate> response) {
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
            public void onFailure(Call<Ugx2CongoRate> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(), "Server not available, saved locally", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void pushC2U(Congo2UgxRate rate) {
        apiService.addCongo2UgxRate(rate).enqueue(new Callback<Congo2UgxRate>() {
            @Override
            public void onResponse(Call<Congo2UgxRate> call, Response<Congo2UgxRate> response) {
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
            public void onFailure(Call<Congo2UgxRate> call, Throwable t) {
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
