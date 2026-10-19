package com.example.mobiledbclient.Login.User;

import android.content.Context;
import java.util.List;
import retrofit2.Response;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.Login.LoginRequest;
import com.example.mobiledbclient.Login.PasswordResetRequest;
import com.example.mobiledbclient.Login.LoginResponse;
import com.example.mobiledbclient.Login.SessionManager;

public class UserRepository {
    private final User_syncManager syncManager;
    private final ApiService apiService;
    private final String deviceId;

    public UserRepository(Context context) {
        User_db_instance db = User_db_instance.getInstance(context);
        User_queries userDao = db.userQueries();
        
        // Pass context to getApiService to ensure headers are correctly added
        apiService = ApiClient.getApiService(context);
        this.deviceId = SessionManager.getDeviceId(context);

        // Pass DAO + ApiService to syncManager
        this.syncManager = new User_syncManager(userDao, apiService);
    }

    // Authenticate against backend
    public boolean authenticate(int userId, String password) {
        try {
            User user = syncManager.getAllUsers().stream()
                    .filter(u -> u.getId() == userId)
                    .findFirst()
                    .orElse(null);

            if (user != null) {
                // ✅ Updated: Now passing deviceId to match the new LoginRequest constructor
                LoginRequest request = new LoginRequest(user.getUsername(), password, deviceId);
                // ✅ Pass request as both @Body and @Tag for SecurityInterceptor fallback
                Response<LoginResponse> response = apiService.login(request, request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().getMessage();
                    if ("Login successful".equalsIgnoreCase(msg)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Server offline → fallback to local authentication
        }

        // Offline fallback
        List<User> localUsers = syncManager.getAllUsers();
        for (User user : localUsers) {
            if (user.getId() == userId &&
                    PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                return true;
            }
        }
        return false;
    }

    // Fetch all users from server, fallback to local DB if offline
    public List<User> getAllUsers() {
        try {
            Response<List<User>> response = apiService.getAllUsers().execute();
            if (response.isSuccessful() && response.body() != null) {
                List<User> serverUsers = response.body();
                syncManager.sync();
                return serverUsers;
            }
        } catch (Exception e) {
            // log error
        }
        return syncManager.getAllUsers(); // fallback to local DB
    }

    public void syncFromServer(List<User> serverUsers) {
        syncManager.sync();
    }

    public void setPassword(int userId, String newPassword) {
        try {
            PasswordResetRequest request = new PasswordResetRequest(newPassword);
            // ✅ Updated: Pass request as both @Body and @Tag for SecurityInterceptor fallback
            // This ensures the interceptor can serialize the DTO into the X-Encrypted-Payload header
            apiService.setPassword(userId, request, request).execute();
        } catch (Exception e) {
            // handle offline fallback
        }
    }
}
