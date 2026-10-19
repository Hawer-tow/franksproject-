package com.example.mobiledbclient.ui.Customers.allcustomers;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class allcustomersFragment extends Fragment {

    private RecyclerView recyclerView;
    private CustomersAdapter adapter;
    private FrameLayout headerFrame;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.all_customers_detail, container, false);

        // Inflate header
        headerFrame = view.findViewById(R.id.customersframework);
        View headerView = inflater.inflate(R.layout.allcustomer_item_header, headerFrame, false);
        headerFrame.addView(headerView);

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.customersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CustomersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load cached data first
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Customers> cached = customers_db_instance.getInstance(requireContext())
                    .customers_Queries().getAllCustomers();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> adapter.updateData(cached));
            }
        });

        // Merge server data
        loadCustomersFromServer();

        // Modify button logic
        Button modifyButton = view.findViewById(R.id.button);
        modifyButton.setOnClickListener(v -> {
            if (adapter.isEditMode()) {
                // Apply edits back into model for all visible rows
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(child);
                    if (vh instanceof CustomersAdapter.CustomerViewHolder) {
                        CustomersAdapter.CustomerViewHolder holder = (CustomersAdapter.CustomerViewHolder) vh;
                        int pos = holder.getBindingAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) continue;
                        Customers customer = adapter.getCustomersList().get(pos);
                        holder.applyEdits(customer);
                    }
                }

                adapter.setEditMode(false);
                modifyButton.setText("Modify the Customers");

                if (adapter.hasChanges()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Changes")
                            .setMessage("Do you want to save the changes?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    customers_db_instance db = customers_db_instance.getInstance(requireContext());
                                    ApiService apiService = ApiClient.getClient().create(ApiService.class);

                                    for (Customers c : adapter.getCustomersList()) {
                                        db.customers_Queries().update(c);
                                        apiService.updateCustomer(c.getCustomer_ID(), c).enqueue(new Callback<Customers>() {
                                            @Override
                                            public void onResponse(Call<Customers> call, Response<Customers> response) {
                                                // success
                                            }

                                            @Override
                                            public void onFailure(Call<Customers> call, Throwable t) {
                                                // handle error
                                            }
                                        });
                                    }
                                });
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            } else {
                adapter.setEditMode(true);
                modifyButton.setText("Save Changes");
            }
        });

        // Attach swipe helper
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof CustomersAdapter.CustomerViewHolder) {
                    CustomersAdapter.CustomerViewHolder holder = (CustomersAdapter.CustomerViewHolder) viewHolder;

                    // If expanded, disable swipe gestures
                    if (holder.fullScroll.getVisibility() == View.VISIBLE) {
                        return 0;
                    }
                }
                return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                adapter.toggleExpanded(pos);
                recyclerView.getAdapter().notifyItemChanged(pos);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.15f;
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        return view;
    }

    private void loadCustomersFromServer() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllCustomers().enqueue(new Callback<List<Customers>>() {
            @Override
            public void onResponse(Call<List<Customers>> call, Response<List<Customers>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Customers> serverList = response.body();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (!isAdded()) return;
                        customers_db_instance db = customers_db_instance.getInstance(requireContext());
                        List<Customers> localList = db.customers_Queries().getAllCustomers();

                        for (Customers serverCustomer : serverList) {
                            boolean exists = false;
                            for (Customers localCustomer : localList) {
                                if (localCustomer.getCustomer_ID() == serverCustomer.getCustomer_ID()) {
                                    exists = true;
                                    db.customers_Queries().update(serverCustomer);
                                    break;
                                }
                            }
                            if (!exists) {
                                db.customers_Queries().insert(serverCustomer);
                            }
                        }

                        List<Customers> merged = db.customers_Queries().getAllCustomers();
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> adapter.updateData(merged));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Customers>> call, Throwable t) {
                // Do nothing: cached data is already loaded
            }
        });
    }
}
