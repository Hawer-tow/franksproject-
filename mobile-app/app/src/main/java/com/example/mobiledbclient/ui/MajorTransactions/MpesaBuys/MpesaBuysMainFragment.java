package com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobiledbclient.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MpesaBuysMainFragment extends Fragment {

    private MpesaBuysViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mpesa_buys_main_fragment, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MpesaBuysViewModel.class);

        ViewPager2 pager = view.findViewById(R.id.mpesaBuysPager);
        MpesaBuysPagerAdapter adapter = new MpesaBuysPagerAdapter(this);
        pager.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.mpesaBuysTabs);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            if (position == 0) tab.setText("Phone Payment");
            else tab.setText("Pending Feature");
        }).attach();

        return view;
    }
}
