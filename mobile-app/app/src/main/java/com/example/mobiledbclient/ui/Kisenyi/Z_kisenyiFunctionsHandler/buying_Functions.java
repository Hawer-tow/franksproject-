package com.example.mobiledbclient.ui.Kisenyi.Z_kisenyiFunctionsHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying;
import com.example.mobiledbclient.ui.Kisenyi.buys.BuyingAdapter;
import com.example.mobiledbclient.ui.Kisenyi.buys.BuyingEntity;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_queries;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_syncManager;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class buying_Functions {

    private BuyingAdapter buyingAdapter;
    private Buying_syncManager buyingSyncManager;

    public void fetchBuying(Context context,
                            FragmentActivity activity,
                            View loadingOverlay,
                            TextView loadingMessage,
                            RecyclerView recyclerView,
                            BuyingAdapter adapter) {

        loadingOverlay.setVisibility(View.VISIBLE);
        loadingMessage.setText("Fetching Buys...");
        recyclerView.setVisibility(View.GONE);

        Buying_db_instance db = Buying_db_instance.getInstance(context);
        Buying_queries dao = db.buying_Queries();
        // ✅ Updated to use context-safe API service
        ApiService api = ApiClient.getApiService(context);

        SharedPreferences prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String range = prefs.getString("data_range", "all");

        // Use background thread for local DB query to prevent UI lag
        Executors.newSingleThreadExecutor().execute(() -> {
            List<BuyingEntity> localBuying;
            switch (range) {
                case "3days": localBuying = dao.getBuyingInRange(3); break;
                case "7days": localBuying = dao.getBuyingInRange(7); break;
                case "monthly": localBuying = dao.getBuyingMonthly(); break;
                default: localBuying = dao.getAll();
            }

            activity.runOnUiThread(() -> {
                if (adapter == null) {
                    buyingAdapter = new BuyingAdapter(localBuying, new BuyingAdapter.OnBuyingActionListener() {
                        @Override
                        public void onEdit(BuyingEntity buying) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Confirm Save")
                                    .setMessage("Do you want to permanently save these changes?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        buying.setSynced(false);
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            dao.update(buying);
                                            activity.runOnUiThread(() -> buyingAdapter.updateBuying(buying));
                                            buyingSyncManager.sync();
                                        });
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }

                        @Override
                        public void onDelete(BuyingEntity buying) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Confirm Delete")
                                    .setMessage("Are you sure you want to permanently delete this row?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        buying.setMarkedForDelete(true);
                                        buying.setSynced(false);
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            dao.update(buying);
                                            activity.runOnUiThread(() -> buyingAdapter.removeBuying(buying));
                                            buyingSyncManager.sync();
                                        });
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    });
                    recyclerView.setAdapter(buyingAdapter);
                } else {
                    adapter.updateData(localBuying);
                    buyingAdapter = adapter;
                }

                // Initialize sync manager
                buyingSyncManager = new Buying_syncManager(dao, api, buyingAdapter);
                buyingSyncManager.sync();

                // Trigger network call
                triggerServerFetch(context, activity, api, dao, range, loadingOverlay, recyclerView);
            });
        });
    }

    private void triggerServerFetch(Context context, FragmentActivity activity, ApiService api, Buying_queries dao, String range, View loadingOverlay, RecyclerView recyclerView) {
        Call<List<Buying>> call;
        switch (range) {
            case "3days": call = api.getBuyingInRange(3); break;
            case "7days": call = api.getBuyingInRange(7); break;
            case "monthly": call = api.getMonthlyBuying(); break;
            default: call = api.getBuying();
        }

        call.enqueue(new Callback<List<Buying>>() {
            @Override
            public void onResponse(Call<List<Buying>> call, Response<List<Buying>> response) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (Buying b : response.body()) {
                            if (b.getBuyingId() == null) continue;
                            BuyingEntity existing = dao.findByServerId(b.getBuyingId());
                            if (existing == null) {
                                BuyingEntity entity = new BuyingEntity();
                                entity.setServerId(b.getBuyingId());
                                entity.setBuyingDate(b.getBuyingDate());
                                entity.setBuyingAmount(b.getBuyingAmount());
                                entity.setBuyingRate(b.getBuyingRate());
                                entity.setTransactingCountry(b.getTransactingCountry());
                                entity.setBuyerName(b.getBuyerName());
                                entity.setBuyDescription(b.getBuyDescription());
                                entity.setSynced(true);
                                dao.insert(entity);
                            } else {
                                existing.setBuyingDate(b.getBuyingDate());
                                existing.setBuyingAmount(b.getBuyingAmount());
                                existing.setBuyingRate(b.getBuyingRate());
                                existing.setTransactingCountry(b.getTransactingCountry());
                                existing.setBuyerName(b.getBuyerName());
                                existing.setBuyDescription(b.getBuyDescription());
                                existing.setSynced(true);
                                dao.update(existing);
                            }
                        }
                        
                        // Final local refresh
                        List<BuyingEntity> updatedList;
                        switch (range) {
                            case "3days": updatedList = dao.getBuyingInRange(3); break;
                            case "7days": updatedList = dao.getBuyingInRange(7); break;
                            case "monthly": updatedList = dao.getBuyingMonthly(); break;
                            default: updatedList = dao.getAll();
                        }
                        activity.runOnUiThread(() -> buyingAdapter.updateData(updatedList));
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Buying>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.e("buying_Functions", "Fetch Buying failed", t);
            }
        });
    }
}
