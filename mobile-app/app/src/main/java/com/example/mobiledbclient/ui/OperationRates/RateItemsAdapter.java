package com.example.mobiledbclient.ui.OperationRates;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.mobiledbclient.R;

public class RateItemsAdapter extends RecyclerView.Adapter<RateItemsAdapter.RateViewHolder> {

    private final List<RateItems> rateList;
    private final OnRateClickListener listener;

    public interface OnRateClickListener {
        void onRateClick(RateItems item);
    }

    public RateItemsAdapter(List<RateItems> rateList, OnRateClickListener listener) {
        this.rateList = rateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout. Make sure your item layout file is named rate_item.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rate_fragment_items, parent, false);
        return new RateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateViewHolder holder, int position) {
        RateItems item = rateList.get(position);

        // Set description
        holder.description.setText(item.getDescription());

        // Set icon resource (or use an image loader if you have URLs)
        holder.icon.setImageResource(item.getIconRes());

        // Example: if you later switch to remote images, use Glide or Picasso:
        // Glide.with(holder.icon.getContext()).load(item.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.icon);

        // Click handling
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRateClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return rateList == null ? 0 : rateList.size();
    }

    static class RateViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final ImageView chevron;
        final TextView description;

        RateViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.rate_icon);
            description = itemView.findViewById(R.id.rate_description);
            chevron = itemView.findViewById(R.id.rate_chevron);
        }
    }
}
