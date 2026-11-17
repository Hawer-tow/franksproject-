package com.example.mobiledbclient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobiledbclient.Login.ProfileSelectorFragment;
import com.example.mobiledbclient.Login.SessionManager;
import com.example.mobiledbclient.databinding.ActivityMainBinding;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String OFFLINE_CHANNEL_ID = "offline_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int activeUserId = SessionManager.getActiveUser(this);

        // Apply theme before super.onCreate so the window and system bars use it too
        String themeKey = "selectedTheme_" + activeUserId;
        String themeName = prefs.getString(themeKey, "AppTheme_Light");
        if ("AppTheme_Dark".equals(themeName)) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets();

        setSupportActionBar(binding.appBarMain.toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_operation, R.id.nav_customers, R.id.nav_ShopMajorTransactions)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        // Decide flow based on active user AFTER layout is ready
        if (activeUserId != -1) {
            unlockAndShowNavigation();
        } else {
            lockAndHideNavigation();
        }

        removeLegacyOfflineNotification();

        // ✅ Fetch Server Public Key for encryption if missing
        fetchServerPublicKey();
    }

    // Draw edge-to-edge the same way on every Android version (15+ forces it anyway), then keep
    // all content clear of the status bar, navigation bar and keyboard by padding the root once.
    // The insets are consumed here, so no screen below needs its own keyboard/system-bar handling.
    private void applyWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void fetchServerPublicKey() {
        if (SessionManager.getServerPublicKey(this) == null) {
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getPublicKey().enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String publicKey = response.body().get("publicKey");
                        if (publicKey != null) {
                            SessionManager.setServerPublicKey(MainActivity.this, publicKey);
                            Log.d(TAG, "Server public key stored successfully.");
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e(TAG, "Failed to fetch server public key", t);
                }
            });
        }
    }

    public void unlockAndShowNavigation() {
        // Hide auth flow
        findViewById(R.id.auth_nav_host).setVisibility(View.GONE);

        // Show main app
        binding.appBarMain.getRoot().setVisibility(View.VISIBLE);
        binding.navView.setVisibility(View.VISIBLE);
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        binding.appBarMain.toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(binding.appBarMain.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        setupNavigation();
        applySystemBarColors();
    }

    public void lockAndHideNavigation() {
        // Hide main app
        binding.appBarMain.getRoot().setVisibility(View.GONE);
        binding.navView.setVisibility(View.GONE);
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Show auth flow
        findViewById(R.id.auth_nav_host).setVisibility(View.VISIBLE);
    }

    private void setupNavigation() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // ---- Offline indicator: a small banner inside the app, only while the app is on screen ----

    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;
        setOfflineBannerVisible(!isOnline(cm));
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> setOfflineBannerVisible(false));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> setOfflineBannerVisible(!isOnline(cm)));
            }
        };
        cm.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    protected void onStop() {
        // Stop watching the network when the app is not on screen: nothing runs or shows in the background
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            try {
                cm.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException ignored) {
                // already unregistered
            }
        }
        networkCallback = null;
        super.onStop();
    }

    private static boolean isOnline(ConnectivityManager cm) {
        Network active = cm.getActiveNetwork();
        NetworkCapabilities caps = active != null ? cm.getNetworkCapabilities(active) : null;
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void setOfflineBannerVisible(boolean visible) {
        View banner = findViewById(R.id.offline_banner);
        if (banner == null) return;
        if (visible && banner.getVisibility() != View.VISIBLE) {
            banner.setAlpha(0f);
            banner.setVisibility(View.VISIBLE);
            banner.animate().alpha(1f).setDuration(200).start();
        } else if (!visible && banner.getVisibility() == View.VISIBLE) {
            banner.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> banner.setVisibility(View.GONE)).start();
        }
    }

    /** Older versions posted a sticky "offline" notification; clear it and its channel once. */
    private void removeLegacyOfflineNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        nm.cancel(1001);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.deleteNotificationChannel(OFFLINE_CHANNEL_ID);
        }
    }

    private void applySystemBarColors() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int activeUserId = SessionManager.getActiveUser(this);
        String themeKey = "selectedTheme_" + activeUserId;
        boolean dark = "AppTheme_Dark".equals(prefs.getString(themeKey, "AppTheme_Light"));

        int statusColor = ContextCompat.getColor(this, dark ? R.color.status_bar_dark : R.color.status_bar_light);
        int navColor = ContextCompat.getColor(this, dark ? R.color.navigation_bar_dark : R.color.navigation_bar_light);
        getWindow().setStatusBarColor(statusColor);
        getWindow().setNavigationBarColor(navColor);
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat wic = new WindowInsetsControllerCompat(getWindow(), decorView);
        // Status bar sits on the green (or dark) toolbar colour, so its icons are always white
        wic.setAppearanceLightStatusBars(false);
        wic.setAppearanceLightNavigationBars(!dark);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        if (item.getItemId() == R.id.nav_gallery
                || item.getItemId() == R.id.nav_home
                || item.getItemId() == R.id.nav_slideshow) {
            navController.navigate(item.getItemId());
            return true;
        }
        else if (item.getItemId() == R.id.customer_www) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost/customers.html"));
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.Helpcenter_www) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://hawer-tow.github.io/FrankM-pesa/"));
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.Logoutoption) {
            SessionManager.clearActiveUser(this);
            lockAndHideNavigation();
            Toast.makeText(this, "You have logged out", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
