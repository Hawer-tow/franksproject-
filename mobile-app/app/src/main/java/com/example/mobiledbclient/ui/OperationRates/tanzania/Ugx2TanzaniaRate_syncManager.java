package com.example.mobiledbclient.ui.OperationRates.tanzania;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ugx2TanzaniaRate_syncManager {
    private final ApiService apiService;
    private final Ugx2TanzaniaRate_db_instance db;

    public Ugx2TanzaniaRate_syncManager(ApiService apiService, Ugx2TanzaniaRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    public void syncUgx2TanzaniaRates() {
        apiService.getUgx2TanzaniaRates().enqueue(new Callback<List<Ugx2TanzaniaRate>>() {
            @Override
            public void onResponse(Call<List<Ugx2TanzaniaRate>> call, Response<List<Ugx2TanzaniaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Ugx2TanzaniaRate rate : response.body()) {
                            db.Ugx2TanzaniaRateDao().insert(rate);
                        }
                    }).start();
                }
            }
            @Override
            public void onFailure(Call<List<Ugx2TanzaniaRate>> call, Throwable t) { }
        });
    }

    public void syncUgx2TanzaniaRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        new Thread(() -> {
            Ugx2TanzaniaRate existing = db.Ugx2TanzaniaRateDao().getRateByDate(today);
            if (existing != null) {
                existing.ugx2tanzaniarate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.Ugx2TanzaniaRateDao().update(existing);
                apiService.addUgx2TanzaniaRate(existing).enqueue(new Callback<Ugx2TanzaniaRate>() {
                    @Override public void onResponse(Call<Ugx2TanzaniaRate> call, Response<Ugx2TanzaniaRate> response) { }
                    @Override public void onFailure(Call<Ugx2TanzaniaRate> call, Throwable t) { }
                });
            } else {
                Ugx2TanzaniaRate newRate = new Ugx2TanzaniaRate();
                newRate.ugx2tanzaniaratedate = today;
                newRate.ugx2tanzaniarate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.Ugx2TanzaniaRateDao().insert(newRate);
                apiService.addUgx2TanzaniaRate(newRate).enqueue(new Callback<Ugx2TanzaniaRate>() {
                    @Override public void onResponse(Call<Ugx2TanzaniaRate> call, Response<Ugx2TanzaniaRate> response) { }
                    @Override public void onFailure(Call<Ugx2TanzaniaRate> call, Throwable t) { }
                });
            }
        }).start();
    }
}
