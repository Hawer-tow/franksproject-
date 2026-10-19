package com.example.mobiledbclient.ui.OperationRates.kenya;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ugx2KenyaRate_syncManager {
    private final ApiService apiService;
    private final Ugx2KenyaRate_db_instance db;

    public Ugx2KenyaRate_syncManager(ApiService apiService, Ugx2KenyaRate_db_instance db) {
        this.apiService = apiService;
        this.db = db;
    }

    // Pull all rates from server and save locally
    public void syncUgx2KenyaRates() {
        apiService.getUgx2KenyaRates().enqueue(new Callback<List<Ugx2KenyaRate>>() {
            @Override
            public void onResponse(Call<List<Ugx2KenyaRate>> call, Response<List<Ugx2KenyaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        for (Ugx2KenyaRate rate : response.body()) {
                            db.ugx2KenyaRateDao().insert(rate);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<Ugx2KenyaRate>> call, Throwable t) {
                // handle error (e.g. log or toast)
            }
        });
    }

    // Update or insert today's rate locally and push to server
    public void syncUgx2KenyaRates(double rateValue, double minValue, double maxValue) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            Ugx2KenyaRate existing = db.ugx2KenyaRateDao().getRateByDate(today);

            if (existing != null) {
                // Update existing record locally
                existing.ugx2kenyarate = rateValue;
                existing.min_transactionvalue = minValue;
                existing.max_transactionValue = maxValue;
                db.ugx2KenyaRateDao().update(existing);

                // Push update to server
                apiService.addUgx2KenyaRate(existing).enqueue(new Callback<Ugx2KenyaRate>() {
                    @Override
                    public void onResponse(Call<Ugx2KenyaRate> call, Response<Ugx2KenyaRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Ugx2KenyaRate> call, Throwable t) {
                        // error feedback
                    }
                });
            } else {
                // Insert new record locally
                Ugx2KenyaRate newRate = new Ugx2KenyaRate();
                newRate.ugx2kenyaratedate = today;
                newRate.ugx2kenyarate = rateValue;
                newRate.min_transactionvalue = minValue;
                newRate.max_transactionValue = maxValue;
                db.ugx2KenyaRateDao().insert(newRate);

                // Push insert to server
                apiService.addUgx2KenyaRate(newRate).enqueue(new Callback<Ugx2KenyaRate>() {
                    @Override
                    public void onResponse(Call<Ugx2KenyaRate> call, Response<Ugx2KenyaRate> response) {
                        // success feedback
                    }
                    @Override
                    public void onFailure(Call<Ugx2KenyaRate> call, Throwable t) {
                        // error feedback
                    }
                });
            }
        }).start();
    }
}
