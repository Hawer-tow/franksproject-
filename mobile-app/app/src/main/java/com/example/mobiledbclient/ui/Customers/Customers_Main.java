package com.example.mobiledbclient.ui.Customers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;

import java.util.Arrays;
import java.util.List;

public class Customers_Main extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customers_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_customers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<CustomerTypes> rates = Arrays.asList(
                new CustomerTypes(1, R.drawable.customers_image, "All Customers"),
                new CustomerTypes(2, R.drawable.topcustomer_image, "Top Customers")

        );

        CustomerTypesAdapter adapter = new CustomerTypesAdapter(rates, item -> {
            // Handle click: navigate or open detail fragment
            Bundle bundle = new Bundle();
            bundle.putInt("rateId", item.getId());
            // Example navigation:
            // NavController navController = Navigation.findNavController(view);
            // navController.navigate(R.id.nav_rate_detail, bundle);
            NavController navController = Navigation.findNavController(view);
            switch (item.getId()) {
                case 1:
                    navController.navigate(R.id.nav_all_customers);
                    break;
                case 2:
                    navController.navigate(R.id.nav_top_customers);
                    break;

            }
        });


        recyclerView.setAdapter(adapter);

        return view;
    }
}
