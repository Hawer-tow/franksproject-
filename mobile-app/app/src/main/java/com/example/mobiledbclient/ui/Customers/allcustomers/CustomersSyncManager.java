package com.example.mobiledbclient.ui.Customers.allcustomers;

import android.content.Context;
import android.widget.Toast;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomersSyncManager {

    private final customers_db_instance db;
    private final ApiService apiService;
    private final Context context;

    public CustomersSyncManager(Context context) {
        this.context = context;
        this.db = customers_db_instance.getInstance(context);
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void pushCustomer(Customers customer) {
        apiService.addCustomer(customer).enqueue(new Callback<Customers>() {
            @Override
            public void onResponse(Call<Customers> call, Response<Customers> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Customer synced to server", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Server error, saved locally", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Customers> call, Throwable t) {
                Toast.makeText(context, "Server not available, saved locally", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pullCustomers() {
        apiService.getAllCustomers().enqueue(new Callback<List<Customers>>() {
            @Override
            public void onResponse(Call<List<Customers>> call, Response<List<Customers>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.customers_Queries().clearAll();
                        for (Customers c : response.body()) {
                            db.customers_Queries().insert(c);
                        }
                    });
                    Toast.makeText(context, "Customers synced locally", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Customers>> call, Throwable t) {
                Toast.makeText(context, "Failed to fetch customers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
