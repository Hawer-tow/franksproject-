package com.example.mobiledbclient.ui.Kisenyi.Z_kisenyiFunctionsHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.ui.Kisenyi.expenses.Expense;
import com.example.mobiledbclient.ui.Kisenyi.expenses.ExpenseAdapter;
import com.example.mobiledbclient.ui.Kisenyi.expenses.ExpenseEntity;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_queries;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_syncManager;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class expenses_Functions {

    private ExpenseAdapter expenseAdapter;
    private expense_syncManager syncManager;

    public void fetchExpenses(Context context,
                              FragmentActivity activity,
                              View loadingOverlay,
                              TextView loadingMessage,
                              RecyclerView recyclerView,
                              ExpenseAdapter adapter) {

        loadingOverlay.setVisibility(View.VISIBLE);
        loadingMessage.setText("Fetching expenses...");
        recyclerView.setVisibility(View.GONE);

        expense_db_instance db = expense_db_instance.getInstance(context);
        expense_queries dao = db.expense_Queries();
        // ✅ Updated to pass context for header support
        ApiService api = ApiClient.getApiService(context);

        SharedPreferences prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String range = prefs.getString("data_range", "all");

        // Background thread for local DB query
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExpenseEntity> localExpenses = getLocalExpensesByRange(dao, range);

            activity.runOnUiThread(() -> {
                if (adapter == null) {
                    expenseAdapter = new ExpenseAdapter(localExpenses, new ExpenseAdapter.OnExpenseActionListener() {
                        @Override
                        public void onEdit(ExpenseEntity expense) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Confirm Save")
                                    .setMessage("Do you want to permanently save these changes?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        expense.setSynced(false);
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            dao.update(expense);
                                            activity.runOnUiThread(() -> expenseAdapter.updateExpense(expense));
                                            syncManager.sync();
                                        });
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }

                        @Override
                        public void onDelete(ExpenseEntity expense) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Confirm Delete")
                                    .setMessage("Are you sure you want to permanently delete this row?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        expense.setMarkedForDelete(true);
                                        expense.setSynced(false);
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            dao.update(expense);
                                            activity.runOnUiThread(() -> expenseAdapter.removeExpense(expense));
                                            syncManager.sync();
                                        });
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    });
                    recyclerView.setAdapter(expenseAdapter);
                } else {
                    adapter.updateData(localExpenses);
                    expenseAdapter = adapter;
                }

                // Initialize sync manager
                syncManager = new expense_syncManager(dao, api, expenseAdapter);
                syncManager.sync();

                // Trigger network fetch
                triggerServerFetch(activity, api, dao, range, loadingOverlay, recyclerView);
            });
        });
    }

    private void triggerServerFetch(FragmentActivity activity, ApiService api, expense_queries dao, String range, View loadingOverlay, RecyclerView recyclerView) {
        Call<List<Expense>> call = getServerCallByRange(api, range);

        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (Expense e : response.body()) {
                            if (e.getId() == null) continue;
                            ExpenseEntity existing = dao.findByServerId(e.getId());
                            if (existing == null) {
                                ExpenseEntity entity = new ExpenseEntity();
                                entity.setServerId(e.getId());
                                entity.setDate(e.getDate());
                                entity.setAmount(e.getAmount());
                                entity.setDescription(e.getDescription());
                                entity.setSynced(true);
                                dao.insert(entity);
                            } else {
                                existing.setDate(e.getDate());
                                existing.setAmount(e.getAmount());
                                existing.setDescription(e.getDescription());
                                existing.setSynced(true);
                                dao.update(existing);
                            }
                        }
                        
                        List<ExpenseEntity> updatedList = getLocalExpensesByRange(dao, range);
                        activity.runOnUiThread(() -> expenseAdapter.updateData(updatedList));
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                loadingOverlay.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.e("expenses_Functions", "Fetch Expenses failed", t);
            }
        });
    }

    private List<ExpenseEntity> getLocalExpensesByRange(expense_queries dao, String range) {
        switch (range) {
            case "3days": return dao.getExpensesInRange(3);
            case "7days": return dao.getExpensesInRange(7);
            case "monthly": return dao.getExpensesMonthly();
            default: return dao.getAll();
        }
    }

    private Call<List<Expense>> getServerCallByRange(ApiService api, String range) {
        switch (range) {
            case "3days": return api.getExpensesInRange(3);
            case "7days": return api.getExpensesInRange(7);
            case "monthly": return api.getMonthlyExpenses();
            default: return api.getExpenses();
        }
    }
}
