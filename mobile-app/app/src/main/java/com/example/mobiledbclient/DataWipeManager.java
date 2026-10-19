package com.example.mobiledbclient;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.mobiledbclient.Login.SessionManager;
import com.example.mobiledbclient.Login.User.User_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_db_instance;

import java.util.concurrent.Executors;

public class DataWipeManager {
    private static final String TAG = "DataWipeManager";

    /**
     * Wipes all local data and returns the user to the Profile Selector.
     */
    public static void wipeAndLogout(Context context) {
        if (context == null) return;

        Context appContext = context.getApplicationContext();
        Log.e(TAG, "Security Action: Wiping all local data and logging out.");

        // 1. Clear session immediately in SharedPreferences
        SessionManager.clearActiveUser(appContext);

        // 2. Wipe all local databases in background
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Clear all Room databases
                expense_db_instance.getInstance(appContext).expense_Queries().clearAll();
                Buying_db_instance.getInstance(appContext).buying_Queries().clearAll();
                Selling_db_instance.getInstance(appContext).selling_Queries().clearAll();
                OpenAndClose_db_instance.getInstance(appContext).openAndClose_Queries().clearAll();
                User_db_instance.getInstance(appContext).userQueries().clearAll();

                Log.d(TAG, "All local databases cleared successfully.");

                // 3. Force navigation back to Profile Selector on the UI thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(appContext, "Access Denied: Your account is inactive. Local data wiped for security.", Toast.LENGTH_LONG).show();
                    
                    // Restart MainActivity which will now see no active user and show Profile Selector
                    Intent intent = new Intent(appContext, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    appContext.startActivity(intent);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error during data wipe", e);
            }
        });
    }
}
