package com.example.mobiledbclient.ui.Kisenyi.expenses;

import android.util.Log;

import com.example.mobiledbclient.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class expense_syncManager {
    private final expense_queries dao;
    private final ApiService api;
    private final ExpenseAdapter adapter;

    // Background sync (no adapter)
    public expense_syncManager(expense_queries dao, ApiService api) {
        this.dao = dao;
        this.api = api;
        this.adapter = null;
    }

    // UI sync (with adapter)
    public expense_syncManager(expense_queries dao, ApiService api, ExpenseAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        List<ExpenseEntity> unsynced = dao.getUnsynced();
        Log.d("SyncManager", "Unsynced count=" + unsynced.size());

        for (ExpenseEntity entity : unsynced) {
            // Normalize date before syncing
            entity.setDate(normalizeDate(entity.getDate()));

            if (entity.isMarkedForDelete()) {
                if (entity.getServerId() != null) {
                    api.deleteExpense(entity.getServerId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                dao.delete(entity);
                                if (adapter != null) adapter.removeExpense(entity);
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("SyncManager", "Delete error", t);
                        }
                    });
                } else {
                    dao.delete(entity);
                    if (adapter != null) adapter.removeExpense(entity);
                }

            } else if (entity.getServerId() == null) {
                Expense networkExpense = new Expense(
                        entity.getAmount(),
                        normalizeDate(entity.getDate()), // normalize before sending
                        entity.getDescription()
                );

                api.addExpense(networkExpense).enqueue(new Callback<Expense>() {
                    @Override
                    public void onResponse(Call<Expense> call, Response<Expense> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            entity.setServerId(response.body().getId());
                            // Normalize backend date before saving
                            entity.setDate(normalizeDate(response.body().getDate()));
                            entity.setSynced(true);
                            dao.update(entity);
                            if (adapter != null) adapter.updateExpense(entity);
                        }
                    }
                    @Override
                    public void onFailure(Call<Expense> call, Throwable t) {
                        Log.e("SyncManager", "Post error", t);
                    }
                });

            } else {
                Expense networkExpense = new Expense(
                        entity.getServerId(),
                        entity.getAmount(),
                        normalizeDate(entity.getDate()), // normalize before sending
                        entity.getDescription()
                );

                api.updateExpense(entity.getServerId(), networkExpense).enqueue(new Callback<Expense>() {
                    @Override
                    public void onResponse(Call<Expense> call, Response<Expense> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Normalize backend date before saving
                            entity.setDate(normalizeDate(response.body().getDate()));
                            entity.setSynced(true);
                            dao.update(entity);
                            if (adapter != null) adapter.updateExpense(entity);
                        }
                    }
                    @Override
                    public void onFailure(Call<Expense> call, Throwable t) {
                        Log.e("SyncManager", "Update error", t);
                    }
                });
            }
        }
    }

    /**
     * Normalize any incoming or outgoing date string to yyyy-MM-dd
     */
    private String normalizeDate(String rawDate) {
        if (rawDate == null) return null;
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }
}
