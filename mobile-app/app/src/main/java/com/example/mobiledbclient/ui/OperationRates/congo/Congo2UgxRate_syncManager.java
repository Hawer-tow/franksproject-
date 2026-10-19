package com.example.mobiledbclient.ui.OperationRates.congo;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Congo2UgxRate_syncManager {
    private final ApiService apiService;
    private final Congo2UgxRate_db_instance db;

    public Congo2UgxRate_syncManager(ApiService apiService, Congo2UgxRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncCongo2UgxRates() {
        apiService.getCongo2UgxRates().enqueue(new Callback<List<Congo2UgxRate>>() {
            @Override
            public void onResponse(Call<List<Congo2UgxRate>> call, Response<List<Congo2UgxRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Congo2UgxRate rate : response.body()) {
                            db.Congo2UgxRateDao().insert(rate);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<Congo2UgxRate>> call, Throwable t) {
                // handle error
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncCongo2UgxRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Congo2UgxRate existing = db.Congo2UgxRateDao().getRateByDate(today);

            if (existing != null) {
                // Update existing record locally
                existing.congo2Ugxrate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Congo2UgxRateDao().update(existing);

                // Push update to server
                apiService.addCongo2UgxRate(existing).enqueue(new Callback<Congo2UgxRate>() {
                    @Override
                    public void onResponse(Call<Congo2UgxRate> call, Response<Congo2UgxRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Congo2UgxRate> call, Throwable t) {
                        // error feedback
                    }
                });
            } else {
                // Insert new record locally
                Congo2UgxRate newRate = new Congo2UgxRate();
                newRate.congo2Ugxratedate = today;
                newRate.congo2Ugxrate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Congo2UgxRateDao().insert(newRate);

                // Push insert to server
                apiService.addCongo2UgxRate(newRate).enqueue(new Callback<Congo2UgxRate>() {
                    @Override
                    public void onResponse(Call<Congo2UgxRate> call, Response<Congo2UgxRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Congo2UgxRate> call, Throwable t) {
                        // error feedback
                    }
                });
            }
        }).start();
    }
}
