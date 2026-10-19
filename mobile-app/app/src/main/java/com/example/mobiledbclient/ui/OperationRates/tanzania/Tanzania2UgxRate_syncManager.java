package com.example.mobiledbclient.ui.OperationRates.tanzania;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Tanzania2UgxRate_syncManager {
    private final ApiService apiService;
    private final Tanzania2UgxRate_db_instance db;

    public Tanzania2UgxRate_syncManager(ApiService apiService, Tanzania2UgxRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncTanzania2UgxRates() {
        apiService.getTanzania2UgxRates().enqueue(new Callback<List<Tanzania2UgxRate>>() {
            @Override
            public void onResponse(Call<List<Tanzania2UgxRate>> call, Response<List<Tanzania2UgxRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Tanzania2UgxRate rate : response.body()) {
                            db.Tanzania2UgxRateDao().insert(rate);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<Tanzania2UgxRate>> call, Throwable t) {
                // handle error (log, toast, etc.)
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncTanzania2UgxRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Tanzania2UgxRate existing = db.Tanzania2UgxRateDao().getRateByDate(today);

            if (existing != null) {
                // Update existing record locally
                existing.tanzania2Ugxrate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Tanzania2UgxRateDao().update(existing);

                // Push update to server
                apiService.addTanzania2UgxRate(existing).enqueue(new Callback<Tanzania2UgxRate>() {
                    @Override
                    public void onResponse(Call<Tanzania2UgxRate> call, Response<Tanzania2UgxRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Tanzania2UgxRate> call, Throwable t) {
                        // error feedback
                    }
                });
            } else {
                // Insert new record locally
                Tanzania2UgxRate newRate = new Tanzania2UgxRate();
                newRate.tanzania2Ugxratedate = today;
                newRate.tanzania2Ugxrate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Tanzania2UgxRateDao().insert(newRate);

                // Push insert to server
                apiService.addTanzania2UgxRate(newRate).enqueue(new Callback<Tanzania2UgxRate>() {
                    @Override
                    public void onResponse(Call<Tanzania2UgxRate> call, Response<Tanzania2UgxRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Tanzania2UgxRate> call, Throwable t) {
                        // error feedback
                    }
                });
            }
        }).start();
    }
}
