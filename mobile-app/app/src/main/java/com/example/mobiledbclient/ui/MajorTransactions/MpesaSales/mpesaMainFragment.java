package com.example.mobiledbclient.ui.MajorTransactions.MpesaSales;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobiledbclient.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class mpesaMainFragment extends Fragment {

    private static final String TAG = "MpesaMain";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_mpesa_transactions, container, false);

        ViewPager2 pager = view.findViewById(R.id.transactionPager);
        TabLayout tabLayout = view.findViewById(R.id.transactionTabs);

        MpesaPagerAdapter adapter = new MpesaPagerAdapter(this);
        pager.setAdapter(adapter);

        pager.setUserInputEnabled(true);
        pager.setOffscreenPageLimit(2);

        final int sidePadding = getResources().getDimensionPixelSize(R.dimen.pager_side_padding);
        pager.setPadding(sidePadding, 0, sidePadding, 0);
        pager.setClipToPadding(false);
        pager.setClipChildren(false);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(
                getResources().getDimensionPixelSize(R.dimen.pager_page_margin)));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            float scale = 0.90f + (r * 0.10f);
            page.setScaleY(scale);
            page.setScaleX(scale);
            page.setAlpha(0.6f + (r * 0.4f));
        });
        pager.setPageTransformer(transformer);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i(TAG, "onPageSelected: " + position);
            }
        });

        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.bringToFront();

        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            tab.setText(position == 0 ? "Paybill to Phone" : "Paybill to Paybill");
        }).attach();

        // MainActivity already shrinks the screen above the keyboard; hide the decorative
        // picture while typing so the form gets that space too
        View backgroundImage = view.findViewById(R.id.backgroundImage);
        view.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(v);
            int visibility = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime())
                    ? View.GONE : View.VISIBLE;
            if (backgroundImage.getVisibility() != visibility) {
                v.post(() -> backgroundImage.setVisibility(visibility));
            }
        });

        // Ensure correct default tab/card is shown
        view.post(() -> {
            Log.i(TAG, "Setting default tab to Phone to Phone");
            //pager.setCurrentItem(0, false); // now applied after layout pass
            Log.i(TAG, "After setCurrentItem: current=" + pager.getCurrentItem());
        });

        return view;
    }
}
