package com.example.mobiledbclient.ui.Customers.allcustomers;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomersAdapter extends RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder> {

    private List<Customers> customersList;
    private List<Customers> originalList;
    private boolean isEditMode = false;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public CustomersAdapter(List<Customers> customersList) {
        this.customersList = customersList;
    }

    public void updateData(List<Customers> newList) {
        this.customersList = new ArrayList<>(newList);
        this.originalList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public List<Customers> getCustomersList() {
        return customersList;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    public boolean hasChanges() {
        if (originalList == null || customersList == null) return false;
        if (originalList.size() != customersList.size()) return true;

        for (int i = 0; i < customersList.size(); i++) {
            Customers current = customersList.get(i);
            Customers original = originalList.get(i);

            if (!Objects.equals(current.getCustomer_name(), original.getCustomer_name()) ||
                    !Objects.equals(current.getCustomer_Phone_No(), original.getCustomer_Phone_No()) ||
                    !Objects.equals(current.getCustomer_email(), original.getCustomer_email()) ||
                    !Objects.equals(current.getCustomer_primary_country(), original.getCustomer_primary_country()) ||
                    !Objects.equals(current.getCustomer_secondary_country(), original.getCustomer_secondary_country()) ||
                    !Objects.equals(current.getCustomer_major_transactions(), original.getCustomer_major_transactions()) ||
                    !Objects.equals(current.getCustomer_description(), original.getCustomer_description())) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.allcustomer_item, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customers customer = customersList.get(position);

        // Compact summary
        holder.summaryName.setText(customer.getCustomer_name());
        holder.summaryPhone.setText(customer.getCustomer_Phone_No());

        // Full details
        holder.name.setText(customer.getCustomer_name());
        holder.phone.setText(customer.getCustomer_Phone_No());
        holder.email.setText(customer.getCustomer_email());
        holder.primary_country.setText(customer.getCustomer_primary_country());
        holder.secondary_country.setText(customer.getCustomer_secondary_country());
        holder.major_transactions.setText(customer.getCustomer_major_transactions());
        holder.description.setText(customer.getCustomer_description());

        holder.setEditMode(isEditMode);

        boolean expanded = expandedPositions.contains(position) || isEditMode;
        TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView, new AutoTransition());
        holder.fullScroll.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.compactContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);

        // Expand chevron click
        holder.expandChevron.setOnClickListener(v -> {
            expandedPositions.add(position);
            notifyItemChanged(position);
        });

        // Collapse chevron click
        holder.collapseChevron.setOnClickListener(v -> {
            expandedPositions.remove(position);
            notifyItemChanged(position);
        });

        // Delete button
        holder.deleteButton.setOnClickListener(v -> {
            customersList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, customersList.size());

            new Thread(() -> {
                customers_db_instance db = customers_db_instance.getInstance(holder.itemView.getContext());
                db.customers_Queries().delete(customer);
            }).start();

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.deleteCustomer(customer.getCustomer_ID(), "Tenet").enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) { }
                @Override
                public void onFailure(Call<Void> call, Throwable t) { }
            });
        });
    }

    @Override
    public int getItemCount() {
        return customersList != null ? customersList.size() : 0;
    }

    public void toggleExpanded(int position) {
        if (expandedPositions.contains(position)) {
            expandedPositions.remove(position);
        } else {
            expandedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout compactContainer;
        TextView summaryName, summaryPhone;
        HorizontalScrollView fullScroll;
        ImageView expandChevron, collapseChevron;
        TextInputEditText name, phone, email, primary_country, secondary_country, major_transactions, description;
        MaterialButton deleteButton;

        CustomerViewHolder(View itemView) {
            super(itemView);
            compactContainer = itemView.findViewById(R.id.compact_container);
            summaryName = itemView.findViewById(R.id.customer_summary_name);
            summaryPhone = itemView.findViewById(R.id.customer_summary_phone);

            fullScroll = itemView.findViewById(R.id.full_scroll);
            expandChevron = itemView.findViewById(R.id.customer_expand_chevron);
            collapseChevron = itemView.findViewById(R.id.customer_collapse_chevron);

            name = itemView.findViewById(R.id.customerName);
            phone = itemView.findViewById(R.id.customerPhone);
            email = itemView.findViewById(R.id.customerEmail);
            primary_country = itemView.findViewById(R.id.customer_primary_country);
            secondary_country = itemView.findViewById(R.id.customer_secondary_country);
            major_transactions = itemView.findViewById(R.id.customer_major_transactions);
            description = itemView.findViewById(R.id.customer_description);
            deleteButton = itemView.findViewById(R.id.customerDelete);
        }

        void setEditMode(boolean editable) {
            name.setEnabled(editable);
            phone.setEnabled(editable);
            email.setEnabled(editable);
            primary_country.setEnabled(editable);
            secondary_country.setEnabled(editable);
            major_transactions.setEnabled(editable);
            description.setEnabled(editable);
            deleteButton.setVisibility(editable ? View.VISIBLE : View.GONE);
        }

        public void applyEdits(Customers customer) {
            customer.setCustomer_name(name.getText().toString());
            customer.setCustomer_Phone_No(phone.getText().toString());
            customer.setCustomer_email(email.getText().toString());
            customer.setCustomer_primary_country(primary_country.getText().toString());
            customer.setCustomer_secondary_country(secondary_country.getText().toString());
            customer.setCustomer_major_transactions(major_transactions.getText().toString());
            customer.setCustomer_description(description.getText().toString());
        }
    }
}
