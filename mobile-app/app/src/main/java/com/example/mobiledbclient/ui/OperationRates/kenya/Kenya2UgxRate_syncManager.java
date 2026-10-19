package com.example.mobiledbclient.ui.OperationRates.kenya;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Kenya2UgxRate_syncManager {
    private final ApiService apiService;
    private final Kenya2UgxRate_db_instance db;

    public Kenya2UgxRate_syncManager(ApiService apiService, Kenya2UgxRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncKenya2UgxRates() {
        apiService.getKenya2UgxRates().enqueue(new Callback<List<Kenya2UgxRate>>() {
            @Override
            public void onResponse(Call<List<Kenya2UgxRate>> call, Response<List<Kenya2UgxRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Kenya2UgxRate rate : response.body()) {
                            db.Kenya2UgxRate_queries().insert(rate);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<Kenya2UgxRate>> call, Throwable t) {
                // handle error (e.g. log or toast)
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncKenya2UgxRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Kenya2UgxRate existing = db.Kenya2UgxRate_queries().getRateByDate(today);

            if (existing != null) {
                // Update existing record locally
                existing.kenya2Ugxrate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Kenya2UgxRate_queries().update(existing);

                // Push update to server
                apiService.addKenya2UgxRate(existing).enqueue(new Callback<Kenya2UgxRate>() {
                    @Override
                    public void onResponse(Call<Kenya2UgxRate> call, Response<Kenya2UgxRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Kenya2UgxRate> call, Throwable t) {
                        // error feedback
                    }
                });
            } else {
                // Insert new record locally
                Kenya2UgxRate newRate = new Kenya2UgxRate();
                newRate.kenya2Ugxratedate = today;
                newRate.kenya2Ugxrate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Kenya2UgxRate_queries().insert(newRate);

                // Push insert to server
                apiService.addKenya2UgxRate(newRate).enqueue(new Callback<Kenya2UgxRate>() {
                    @Override
                    public void onResponse(Call<Kenya2UgxRate> call, Response<Kenya2UgxRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Kenya2UgxRate> call, Throwable t) {
                        // error feedback
                    }
                });
            }
        }).start();
    }
}
