package com.example.mobiledbclient.ui.OperationRates.rwanda;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rwanda2UgxRate_syncManager {
    private final ApiService apiService;
    private final Rwanda2UgxRate_db_instance db;

    public Rwanda2UgxRate_syncManager(ApiService apiService, Rwanda2UgxRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncRwanda2UgxRates() {
        apiService.getRwanda2UgxRates().enqueue(new Callback<List<Rwanda2UgxRate>>() {
            @Override
            public void onResponse(Call<List<Rwanda2UgxRate>> call, Response<List<Rwanda2UgxRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Rwanda2UgxRate rate : response.body()) {
                            db.Rwanda2UgxRateDao().insert(rate);
                        }
                    }).start();
                }
            }
            @Override
            public void onFailure(Call<List<Rwanda2UgxRate>> call, Throwable t) {
                // handle error
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncRwanda2UgxRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Rwanda2UgxRate existing = db.Rwanda2UgxRateDao().getRateByDate(today);
            if (existing != null) {
                existing.rwanda2Ugxrate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Rwanda2UgxRateDao().update(existing);

                apiService.addRwanda2UgxRate(existing).enqueue(new Callback<Rwanda2UgxRate>() {
                    @Override public void onResponse(Call<Rwanda2UgxRate> call, Response<Rwanda2UgxRate> response) { }
                    @Override public void onFailure(Call<Rwanda2UgxRate> call, Throwable t) { }
                });
            } else {
                Rwanda2UgxRate newRate = new Rwanda2UgxRate();
                newRate.rwanda2Ugxratedate = today;
                newRate.rwanda2Ugxrate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Rwanda2UgxRateDao().insert(newRate);

                apiService.addRwanda2UgxRate(newRate).enqueue(new Callback<Rwanda2UgxRate>() {
                    @Override public void onResponse(Call<Rwanda2UgxRate> call, Response<Rwanda2UgxRate> response) { }
                    @Override public void onFailure(Call<Rwanda2UgxRate> call, Throwable t) { }
                });
            }
        }).start();
    }
}
