package com.example.mobiledbclient.ui.OperationRates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;

import java.util.Arrays;
import java.util.List;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


public class OperationRates extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.operation_rates_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_rates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<RateItems> rates = Arrays.asList(
                new RateItems(1, R.drawable.kenya_rates, "Kenya Rates"),
                new RateItems(2, R.drawable.congo_rates, "Congo Rates"),
                new RateItems(3, R.drawable.tanzania_rates, "Tanzania Rates"),
                new RateItems(4, R.drawable.rwanda_rates, "Rwanda Rates")
        );

      RateItemsAdapter adapter = new RateItemsAdapter(rates, item -> {
            // Handle click: navigate or open detail fragment
            Bundle bundle = new Bundle();
            bundle.putInt("rateId", item.getId());
            // Example navigation:
            // NavController navController = Navigation.findNavController(view);
            // navController.navigate(R.id.nav_rate_detail, bundle);
            NavController navController = Navigation.findNavController(view);
            switch (item.getId()) {
                case 1:
                    navController.navigate(R.id.nav_kenya_rate_details);
                    break;
                case 2:
                    navController.navigate(R.id.nav_congo_rate_details);
                    break;
                case 3:
                    navController.navigate(R.id.nav_tanzania_rate_details);
                    break;
                case 4:
                    navController.navigate(R.id.nav_rwanda_rate_details);
                    break;
            }
        });


        recyclerView.setAdapter(adapter);

        return view;
    }
}
