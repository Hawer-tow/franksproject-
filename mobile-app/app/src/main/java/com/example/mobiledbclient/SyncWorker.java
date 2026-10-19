package com.example.mobiledbclient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;

import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_queries;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_syncManager;

import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_queries;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_syncManager;

import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_queries;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_syncManager;

import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_queries;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_syncManager;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // ✅ Updated to use context-safe getApiService for encryption support
        ApiService api = ApiClient.getApiService(getApplicationContext());

        // Expenses
        expense_db_instance expenseDb = expense_db_instance.getInstance(getApplicationContext());
        expense_queries expenseDao = expenseDb.expense_Queries();
        new expense_syncManager(expenseDao, api).sync();

        // Buying
        Buying_db_instance buyingDb = Buying_db_instance.getInstance(getApplicationContext());
        Buying_queries buyingDao = buyingDb.buying_Queries();
        new Buying_syncManager(buyingDao, api).sync();

        // Selling
        Selling_db_instance sellingDb = Selling_db_instance.getInstance(getApplicationContext());
        Selling_queries sellingDao = sellingDb.selling_Queries();
        new Selling_syncManager(sellingDao, api).sync();

        // Open & Close
        OpenAndClose_db_instance ocDb = OpenAndClose_db_instance.getInstance(getApplicationContext());
        OpenAndClose_queries ocDao = ocDb.openAndClose_Queries();
        new OpenAndClose_syncManager(ocDao, api).sync();

        return Result.success();
    }
}
