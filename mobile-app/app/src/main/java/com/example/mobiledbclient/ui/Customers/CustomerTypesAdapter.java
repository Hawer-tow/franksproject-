package com.example.mobiledbclient.ui.Customers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;

import java.util.List;

public class CustomerTypesAdapter extends RecyclerView.Adapter<CustomerTypesAdapter.RateViewHolder> {

    private final List<CustomerTypes> customerList;
    private final OnRateClickListener listener;

    public interface OnRateClickListener {
        void onRateClick(CustomerTypes item);
    }

    public CustomerTypesAdapter(List<CustomerTypes> customerList, OnRateClickListener listener) {
        this.customerList = customerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout. Ensure this filename matches your layout resource.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customers_fragment_items, parent, false);
        return new RateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateViewHolder holder, int position) {
        CustomerTypes item = customerList.get(position);

        // Defensive checks to avoid NPEs if layout IDs differ
        if (holder.description != null) {
            holder.description.setText(item.getDescription());
        }

        if (holder.icon != null) {
            holder.icon.setImageResource(item.getIconRes());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRateClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return customerList == null ? 0 : customerList.size();
    }

    static class RateViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final ImageView chevron;
        final TextView description;

        RateViewHolder(View itemView) {
            super(itemView);
            // Use the IDs that exist in your item layout. If your layout uses customer_name, use that.
            ImageView tmpIcon = itemView.findViewById(R.id.customer_icon);
            TextView tmpDescription = itemView.findViewById(R.id.customer_description);
            if (tmpDescription == null) {
                // fallback to alternate id used in the improved layout
                tmpDescription = itemView.findViewById(R.id.customer_name);
            }
            ImageView tmpChevron = itemView.findViewById(R.id.customer_chevron);

            icon = tmpIcon;
            description = tmpDescription;
            chevron = tmpChevron;
        }
    }
}
