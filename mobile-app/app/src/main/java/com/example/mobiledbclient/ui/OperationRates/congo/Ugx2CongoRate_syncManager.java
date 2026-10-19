package com.example.mobiledbclient.ui.OperationRates.congo;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ugx2CongoRate_syncManager {
    private final ApiService apiService;
    private final Ugx2CongoRate_db_instance db;

    public Ugx2CongoRate_syncManager(ApiService apiService, Ugx2CongoRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncUgx2CongoRates() {
        apiService.getUgx2CongoRates().enqueue(new Callback<List<Ugx2CongoRate>>() {
            @Override
            public void onResponse(Call<List<Ugx2CongoRate>> call, Response<List<Ugx2CongoRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Ugx2CongoRate rate : response.body()) {
                            db.Ugx2CongoRateDao().insert(rate);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<Ugx2CongoRate>> call, Throwable t) {
                // handle error
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncUgx2CongoRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Ugx2CongoRate existing = db.Ugx2CongoRateDao().getRateByDate(today);

            if (existing != null) {
                // Update existing record locally
                existing.ugx2congorate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Ugx2CongoRateDao().update(existing);

                // Push update to server
                apiService.addUgx2CongoRate(existing).enqueue(new Callback<Ugx2CongoRate>() {
                    @Override
                    public void onResponse(Call<Ugx2CongoRate> call, Response<Ugx2CongoRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Ugx2CongoRate> call, Throwable t) {
                        // error feedback
                    }
                });
            } else {
                // Insert new record locally
                Ugx2CongoRate newRate = new Ugx2CongoRate();
                newRate.ugx2congoratedate = today;
                newRate.ugx2congorate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Ugx2CongoRateDao().insert(newRate);

                // Push insert to server
                apiService.addUgx2CongoRate(newRate).enqueue(new Callback<Ugx2CongoRate>() {
                    @Override
                    public void onResponse(Call<Ugx2CongoRate> call, Response<Ugx2CongoRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Ugx2CongoRate> call, Throwable t) {
                        // error feedback
                    }
                });
            }
        }).start();
    }
}
