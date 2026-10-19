package com.example.mobiledbclient.ui.Kisenyi.Z_kisenyiFunctionsHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling;
import com.example.mobiledbclient.ui.Kisenyi.sales.SellingAdapter;
import com.example.mobiledbclient.ui.Kisenyi.sales.SellingEntity;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_queries;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_syncManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class selling_Functions {

    private SellingAdapter sellingAdapter;
    private Selling_syncManager sellingSyncManager;

    public void fetchSelling(Context context,
                             FragmentActivity activity,
                             View loadingOverlay,
                             TextView loadingMessage,
                             RecyclerView recyclerView,
                             SellingAdapter adapter) {

        loadingOverlay.setVisibility(View.VISIBLE);
        loadingMessage.setText("Fetching Sales...");
        recyclerView.setVisibility(View.GONE);

        Selling_db_instance db = Selling_db_instance.getInstance(context);
        Selling_queries dao = db.selling_Queries();
        // ✅ Updated to pass context for header support
        ApiService api = ApiClient.getApiService(context);

        SharedPreferences prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String range = prefs.getString("data_range", "all");

        // Local data based on range
        List<SellingEntity> localSelling;
        switch (range) {
            case "3days": localSelling = dao.getSellingInRange(3); break;
            case "7days": localSelling = dao.getSellingInRange(7); break;
            case "monthly": localSelling = dao.getSellingMonthly(); break;
            default: localSelling = dao.getAll();
        }

        if (adapter == null) {
            sellingAdapter = new SellingAdapter(localSelling, new SellingAdapter.OnSellingActionListener() {
                @Override
                public void onEdit(SellingEntity selling) {
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm Save")
                            .setMessage("Do you want to permanently save these changes?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                selling.setSynced(false);
                                dao.update(selling);
                                sellingAdapter.updateSelling(selling);
                                sellingSyncManager.sync();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                }

                @Override
                public void onDelete(SellingEntity selling) {
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to permanently delete this row?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                selling.setMarkedForDelete(true);
                                selling.setSynced(false);
                                dao.update(selling);
                                sellingAdapter.removeSelling(selling);
                                sellingSyncManager.sync();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
            recyclerView.setAdapter(sellingAdapter);
        } else {
            adapter.updateData(localSelling);
            sellingAdapter = adapter;
        }

        // Initialize sync manager
        sellingSyncManager = new Selling_syncManager(dao, api, sellingAdapter);
        sellingSyncManager.sync();

        // Choose correct server endpoint
        Call<List<Selling>> call;
        switch (range) {
            case "3days": call = api.getSellingInRange(3); break;
            case "7days": call = api.getSellingInRange(7); break;
            case "monthly": call = api.getMonthlySelling(); break;
            default: call = api.getSelling();
        }

        call.enqueue(new Callback<List<Selling>>() {
            @Override
            public void onResponse(Call<List<Selling>> call, Response<List<Selling>> response) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    dao.clearAll();
                    for (Selling s : response.body()) {
                        if (s.getSellingId() == null) continue;

                        SellingEntity existing = dao.findByServerId(s.getSellingId());
                        if (existing == null) {
                            SellingEntity entity = new SellingEntity();
                            entity.setServerId(s.getSellingId());
                            entity.setSellingDate(normalizeDate(s.getSellingDate()));
                            entity.setSellingAmount(s.getSellingAmount());
                            entity.setSellingRate(s.getSellingRate());
                            entity.setReceivingCountry(s.getReceivingCountry());
                            entity.setPhoneNo(s.getPhoneNo());
                            entity.setRecipientName(s.getRecipientName());
                            entity.setSellDescription(s.getSellDescription());
                            entity.setSynced(true);
                            dao.insert(entity);
                        } else {
                            existing.setSellingDate(normalizeDate(s.getSellingDate()));
                            existing.setSellingAmount(s.getSellingAmount());
                            existing.setSellingRate(s.getSellingRate());
                            existing.setReceivingCountry(s.getReceivingCountry());
                            existing.setPhoneNo(s.getPhoneNo());
                            existing.setRecipientName(s.getRecipientName());
                            existing.setSellDescription(s.getSellDescription());
                            existing.setSynced(true);
                            dao.update(existing);
                        }
                    }

                    // Refresh adapter with filtered local data
                    List<SellingEntity> filtered;
                    switch (range) {
                        case "3days": filtered = dao.getSellingInRange(3); break;
                        case "7days": filtered = dao.getSellingInRange(7); break;
                        case "monthly": filtered = dao.getSellingMonthly(); break;
                        default: filtered = dao.getAll();
                    }

                    sellingAdapter.updateData(filtered);

                    if (filtered.isEmpty()) {
                        new AlertDialog.Builder(context)
                                .setTitle("No Data Found")
                                .setMessage("No selling records exist in the selected range.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Selling>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Failed to fetch selling data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String normalizeDate(String rawDate) {
        if (rawDate == null) return null;
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }
}
