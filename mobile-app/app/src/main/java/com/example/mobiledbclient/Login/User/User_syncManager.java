package com.example.mobiledbclient.Login.User;

import android.util.Log;
import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class User_syncManager {
    private final User_queries dao;
    private final ApiService api;
    private final UserAdapter adapter; // optional, only for UI sync

    // Background sync (no adapter)
    public User_syncManager(User_queries dao, ApiService api) {
        this.dao = dao;
        this.api = api;
        this.adapter = null; // no UI updates in background
    }

    // UI sync (with adapter)
    public User_syncManager(User_queries dao, ApiService api, UserAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    // Sync users from server to local DB
    public void sync() {
        api.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> serverUsers = response.body();
                    new Thread(() -> {
                        dao.clearUsers();
                        dao.insertAll(serverUsers);
                        Log.d("UserSyncManager", "Synced " + serverUsers.size() + " users from server");
                    }).start();

                    // Only update adapter if present (UI context)
                    if (adapter != null) {
                        adapter.updateData(serverUsers);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("UserSyncManager", "Failed to fetch users", t);
            }
        });
    }

    // Local authentication fallback
    public boolean authenticateLocal(String username, String password) {
        User user = dao.getUserByUsername(username);
        if (user != null) {
            return PasswordUtils.verifyPassword(password, user.getPasswordHash());
        }
        return false;
    }

    // Get all users from local DB
    public List<User> getAllUsers() {
        return dao.getAllUsers();
    }
}
