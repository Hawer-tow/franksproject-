package com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MpesaBuysPagerAdapter extends FragmentStateAdapter {

    public MpesaBuysPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new MpesaBuysPhoneFragment();
        } else {
            return new MpesaBuysPendingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
