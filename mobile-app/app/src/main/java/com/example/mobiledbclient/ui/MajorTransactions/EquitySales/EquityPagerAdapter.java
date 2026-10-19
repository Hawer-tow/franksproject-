package com.example.mobiledbclient.ui.MajorTransactions.EquitySales;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EquityPagerAdapter extends FragmentStateAdapter {

    public EquityPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new equityAccountFragment();
        } else {
            return new equityPhoneFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
