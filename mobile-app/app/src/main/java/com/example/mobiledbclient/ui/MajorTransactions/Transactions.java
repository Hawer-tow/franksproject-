package com.example.mobiledbclient.ui.MajorTransactions;

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

public class Transactions extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shoptransactions_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.transaction_options_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Data with section headers
        List<TransactionsItems> rates = Arrays.asList(
                // Sales section
                new TransactionsItems("Sales"),
                new TransactionsItems(1, R.drawable.equity_bank_logo2, "Equity (Send Money to customer)"),
                new TransactionsItems(2, R.drawable.mpesa_image2, "M-pesa (Send Money to customer)"),

                // Buys section
                new TransactionsItems("Buys"),
                new TransactionsItems(3, R.drawable.equity_bank_logo2, "Equity (Prompt customers)"),
                new TransactionsItems(4, R.drawable.mpesa_image2, "M-pesa (Prompt customers)")
        );

        TransactionsItemsAdapter adapter = new TransactionsItemsAdapter(rates, item -> {
            if (item.isHeader()) {
                // Do nothing on header click
                return;
            }

            // Handle click: navigate or open detail fragment
            Bundle bundle = new Bundle();
            bundle.putInt("rateId", item.getId());

            NavController navController = Navigation.findNavController(view);
            switch (item.getId()) {
                case 1: // Equity Sales
                    navController.navigate(R.id.equityTransactions);
                    break;
                case 2: // Mpesa Sales
                    navController.navigate(R.id.mpesaTransactions);
                    break;
                case 3: // Equity Buys
                    navController.navigate(R.id.equityBuysTransactions);
                    break;
                case 4: // Mpesa Buys
                    navController.navigate(R.id.mpesaBuysTransactions);
                    break;
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}
