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
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndCloseAdapter;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndCloseEntity;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_queries;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_syncManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenAndClose_Functions {

    private OpenAndCloseAdapter openAndCloseAdapter;
    private OpenAndClose_syncManager openAndCloseSyncManager;

    public void fetchOpenAndClose(Context context,
                                  FragmentActivity activity,
                                  View loadingOverlay,
                                  TextView loadingMessage,
                                  RecyclerView recyclerView,
                                  OpenAndCloseAdapter adapter) {

        loadingOverlay.setVisibility(View.VISIBLE);
        loadingMessage.setText("Fetching Opening and Closing information...");
        recyclerView.setVisibility(View.GONE);

        OpenAndClose_db_instance db = OpenAndClose_db_instance.getInstance(context);
        OpenAndClose_queries dao = db.openAndClose_Queries();
        // ✅ Updated to pass context for header support
        ApiService api = ApiClient.getApiService(context);

        SharedPreferences prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String range = prefs.getString("data_range", "all");

        // Local data based on range
        List<OpenAndCloseEntity> local;
        switch (range) {
            case "3days": local = dao.getInRange(3); break;
            case "7days": local = dao.getInRange(7); break;
            case "monthly": local = dao.getMonthly(); break;
            default: local = dao.getAll();
        }

        if (adapter == null) {
            openAndCloseAdapter = new OpenAndCloseAdapter(local, new OpenAndCloseAdapter.OnOpenAndCloseActionListener() {
                @Override
                public void onEdit(OpenAndCloseEntity entity) {
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm Save")
                            .setMessage("Do you want to permanently save these changes?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                entity.setSynced(false);
                                dao.update(entity);
                                openAndCloseAdapter.updateData(dao.getAll());
                                openAndCloseSyncManager.sync();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                }

                @Override
                public void onDelete(OpenAndCloseEntity entity) {
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to permanently delete this row?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                entity.setMarkedForDelete(true);
                                entity.setSynced(false);
                                dao.update(entity);
                                openAndCloseAdapter.updateData(dao.getAll());
                                openAndCloseSyncManager.sync();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
            recyclerView.setAdapter(openAndCloseAdapter);
        } else {
            adapter.updateData(local);
            openAndCloseAdapter = adapter;
        }

        openAndCloseSyncManager = new OpenAndClose_syncManager(dao, api, openAndCloseAdapter);
        openAndCloseSyncManager.sync();

        // Choose correct server endpoint
        Call<List<OpenAndClose>> call;
        switch (range) {
            case "3days": call = api.getOpenAndCloseInRange(3); break;
            case "7days": call = api.getOpenAndCloseInRange(7); break;
            case "monthly": call = api.getMonthlyOpenAndClose(); break;
            default: call = api.getOpenAndClose();
        }

        call.enqueue(new Callback<List<OpenAndClose>>() {
            @Override
            public void onResponse(Call<List<OpenAndClose>> call, Response<List<OpenAndClose>> response) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    dao.clearAll();
                    for (OpenAndClose dto : response.body()) {
                        OpenAndCloseEntity existing = dao.findByDate(dto.getDate());
                        if (existing == null) {
                            dao.insert(openAndCloseSyncManager.fromDto(dto));
                        } else {
                            existing.setUgandaOpening(dto.getUgandaOpening());
                            existing.setKenyaOpening(dto.getKenyaOpening());
                            existing.setUgandaCashIn(dto.getUgandaCashIn());
                            existing.setKenyaCashIn(dto.getKenyaCashIn());
                            existing.setUgandaClosing(dto.getUgandaClosing());
                            existing.setKenyaClosing(dto.getKenyaClosing());
                            existing.setDescription(dto.getDescription());
                            existing.setSynced(true);
                            dao.update(existing);
                        }
                    }

                    // Refresh adapter with filtered local data
                    List<OpenAndCloseEntity> filtered;
                    switch (range) {
                        case "3days": filtered = dao.getInRange(3); break;
                        case "7days": filtered = dao.getInRange(7); break;
                        case "monthly": filtered = dao.getMonthly(); break;
                        default: filtered = dao.getAll();
                    }
                    openAndCloseAdapter.updateData(filtered);

                    if (filtered.isEmpty()) {
                        new AlertDialog.Builder(context)
                                .setTitle("No Data Found")
                                .setMessage("No open/close records exist in the selected range.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<OpenAndClose>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Server offline, showing local data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
