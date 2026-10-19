package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

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

public class EquityBuysMainFragment extends Fragment {

    private EquityBuysViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equity_buys_main_fragment, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(EquityBuysViewModel.class);

        ViewPager2 pager = view.findViewById(R.id.equityBuysPager);
        EquityBuysPagerAdapter adapter = new EquityBuysPagerAdapter(this);
        pager.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.equityBuysTabs);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            if (position == 0) tab.setText("Phone Payment");
            else tab.setText("Account Payment");
        }).attach();

        return view;
    }
}

