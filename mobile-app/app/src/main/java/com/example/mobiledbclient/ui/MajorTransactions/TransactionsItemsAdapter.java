package com.example.mobiledbclient.ui.MajorTransactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.example.mobiledbclient.R;

public class TransactionsItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TransactionsItems> rateList;
    private OnRateClickListener listener;

    public interface OnRateClickListener {
        void onRateClick(TransactionsItems item);
    }

    public TransactionsItemsAdapter(List<TransactionsItems> rateList, OnRateClickListener listener) {
        this.rateList = rateList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return rateList.get(position).isHeader() ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            // Header layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shoptransactions_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            // Normal item layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shoptransactions_fragment_items, parent, false);
            return new RateViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TransactionsItems item = rateList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerText.setText(item.getDescription());
        } else if (holder instanceof RateViewHolder) {
            RateViewHolder rateHolder = (RateViewHolder) holder;
            rateHolder.icon.setImageResource(item.getIconRes());
            rateHolder.description.setText(item.getDescription());
            rateHolder.itemView.setOnClickListener(v -> listener.onRateClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return rateList.size();
    }

    static class RateViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, chevron;
        TextView description;

        RateViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.trade_icon);
            description = itemView.findViewById(R.id.trade_description);
            chevron = itemView.findViewById(R.id.trade_chevron);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.section_header);
        }
    }
}
