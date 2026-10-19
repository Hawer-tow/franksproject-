package com.example.mobiledbclient.ui.OperationRates.rwanda;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ugx2RwandaRate_syncManager {
    private final ApiService apiService;
    private final Ugx2RwandaRate_db_instance db;

    public Ugx2RwandaRate_syncManager(ApiService apiService, Ugx2RwandaRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncUgx2RwandaRates() {
        apiService.getUgx2RwandaRates().enqueue(new Callback<List<Ugx2RwandaRate>>() {
            @Override
            public void onResponse(Call<List<Ugx2RwandaRate>> call, Response<List<Ugx2RwandaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Ugx2RwandaRate rate : response.body()) {
                            db.Ugx2RwandaRateDao().insert(rate);
                        }
                    }).start();
                }
            }
            @Override
            public void onFailure(Call<List<Ugx2RwandaRate>> call, Throwable t) {
                // handle error
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncUgx2RwandaRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Ugx2RwandaRate existing = db.Ugx2RwandaRateDao().getRateByDate(today);
            if (existing != null) {
                existing.ugx2rwandarate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Ugx2RwandaRateDao().update(existing);

                apiService.addUgx2RwandaRate(existing).enqueue(new Callback<Ugx2RwandaRate>() {
                    @Override public void onResponse(Call<Ugx2RwandaRate> call, Response<Ugx2RwandaRate> response) { }
                    @Override public void onFailure(Call<Ugx2RwandaRate> call, Throwable t) { }
                });
            } else {
                Ugx2RwandaRate newRate = new Ugx2RwandaRate();
                newRate.ugx2rwandaratedate = today;
                newRate.ugx2rwandarate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Ugx2RwandaRateDao().insert(newRate);

                apiService.addUgx2RwandaRate(newRate).enqueue(new Callback<Ugx2RwandaRate>() {
                    @Override public void onResponse(Call<Ugx2RwandaRate> call, Response<Ugx2RwandaRate> response) { }
                    @Override public void onFailure(Call<Ugx2RwandaRate> call, Throwable t) { }
                });
            }
        }).start();
    }
}
