package com.example.mobiledbclient.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.color.MaterialColors;

/**
 * Pull-to-refresh for lists that load from the server. Pulling down re-runs the screen's own load
 * (which pushes local changes and fetches what other users changed). The spinner stops when the
 * screen's loading overlay goes away, when the list receives new data, or after 10 seconds.
 */
public final class RefreshHelper {

    private static final long MIN_SPIN_MS = 600;
    private static final long MAX_SPIN_MS = 10_000;

    private RefreshHelper() {}

    public static void setUp(SwipeRefreshLayout refresh, @Nullable View loadingOverlay,
                             @Nullable RecyclerView list, Runnable reload) {
        refresh.setColorSchemeColors(MaterialColors.getColor(refresh, androidx.appcompat.R.attr.colorPrimary));
        refresh.setProgressBackgroundColorSchemeColor(
                MaterialColors.getColor(refresh, com.google.android.material.R.attr.colorSurfaceContainerHigh));

        refresh.setOnRefreshListener(() -> {
            final boolean[] dataArrived = {false};
            RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    dataArrived[0] = true;
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    dataArrived[0] = true;
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    dataArrived[0] = true;
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    dataArrived[0] = true;
                }
            };
            RecyclerView.Adapter<?> watched = list != null ? list.getAdapter() : null;
            if (watched != null) watched.registerAdapterDataObserver(observer);

            reload.run();

            Handler handler = new Handler(Looper.getMainLooper());
            long started = SystemClock.uptimeMillis();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - started;
                    boolean overlayBusy = loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE;
                    boolean adapterSwapped = list != null && list.getAdapter() != watched;
                    boolean done = elapsed >= MIN_SPIN_MS
                            && ((loadingOverlay != null && !overlayBusy) || dataArrived[0] || adapterSwapped);
                    if (done || elapsed >= MAX_SPIN_MS || !refresh.isAttachedToWindow()) {
                        refresh.setRefreshing(false);
                        if (watched != null) {
                            try {
                                watched.unregisterAdapterDataObserver(observer);
                            } catch (IllegalStateException ignored) {
                                // already removed
                            }
                        }
                    } else {
                        handler.postDelayed(this, 250);
                    }
                }
            }, 300);
        });
    }
}
