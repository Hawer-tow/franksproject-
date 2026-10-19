package com.example.mobiledbclient.ui.MajorTransactions.MpesaSales;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MpesaPagerAdapter extends FragmentStateAdapter {

    public MpesaPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new mpesaPaybill2Phonefragment1();
        } else {
            return new mpesaPaybill2Paybillfragment2();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
