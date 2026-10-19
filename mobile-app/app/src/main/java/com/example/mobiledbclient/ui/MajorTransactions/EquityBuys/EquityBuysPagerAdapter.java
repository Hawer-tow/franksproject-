package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EquityBuysPagerAdapter extends FragmentStateAdapter {

    public EquityBuysPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new EquityBuysPhoneFragment();
        } else {
            return new EquityBuysAccountFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
