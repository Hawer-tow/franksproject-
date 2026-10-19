package com.example.mobiledbclient.ui.Kisenyi.buys;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;

public class BuyingAdapter extends RecyclerView.Adapter<BuyingAdapter.BuyingViewHolder> {
    private List<BuyingEntity> buyingList;
    private OnBuyingActionListener listener;

    public interface OnBuyingActionListener {
        void onEdit(BuyingEntity buying);
        void onDelete(BuyingEntity buying);
    }

    public BuyingAdapter(List<BuyingEntity> buyingList, OnBuyingActionListener listener) {
        this.buyingList = buyingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BuyingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.buying_item, parent, false);
        return new BuyingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuyingViewHolder holder, int position) {
        BuyingEntity buying = buyingList.get(position);

        // Compact values
        holder.buyDateCompact.setText(buying.getBuyingDate());
        holder.buyAmountCompact.setText(String.valueOf(buying.getBuyingAmount()));
        holder.buyRateCompact.setText(String.valueOf(buying.getBuyingRate()));

        // Expanded values
        holder.buyDate.setText(buying.getBuyingDate());
        holder.buyAmount.setText(String.valueOf(buying.getBuyingAmount()));
        holder.buyRate.setText(String.valueOf(buying.getBuyingRate()));
        holder.buyCountry.setText(buying.getTransactingCountry());
        holder.buyName.setText(buying.getBuyerName());
        holder.buyDescription.setText(buying.getBuyDescription());

        boolean expanded = buying.isEditing();
        holder.compactContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        holder.fullScroll.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // Expand chevron
        holder.expandChevron.setOnClickListener(v -> {
            buying.setEditing(true);
            notifyItemChanged(position);
        });

        // Collapse chevron
        holder.collapseChevron.setOnClickListener(v -> {
            buying.setEditing(false);
            notifyItemChanged(position);
        });

        // Date picker for buyDate
        holder.buyDate.setOnClickListener(v -> {
            if (holder.buyDate.isEnabled()) { // only when editing
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(
                        v.getContext(),
                        (view1, year, month, dayOfMonth) -> {
                            String selectedDate = String.format(Locale.getDefault(),
                                    "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            holder.buyDate.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            }
        });

        // Edit button in expanded row
        holder.btnEdit.setOnClickListener(v -> {
            if (holder.btnEdit.getText().equals("Edit")) {
                holder.btnEdit.setText("Save");
                holder.setEditable(true);
            } else {
                // Save edits
                buying.setBuyingDate(holder.buyDate.getText().toString());
                try {
                    buying.setBuyingAmount(Double.parseDouble(holder.buyAmount.getText().toString()));
                } catch (NumberFormatException e) {
                    buying.setBuyingAmount(0);
                }
                try {
                    buying.setBuyingRate(Double.parseDouble(holder.buyRate.getText().toString()));
                } catch (NumberFormatException e) {
                    buying.setBuyingRate(0);
                }
                buying.setTransactingCountry(holder.buyCountry.getText().toString());
                buying.setBuyerName(holder.buyName.getText().toString());
                buying.setBuyDescription(holder.buyDescription.getText().toString());

                buying.setEditing(false);
                buying.setSynced(false);

                listener.onEdit(buying);

                holder.btnEdit.setText("Edit");
                holder.setEditable(false);

                notifyItemChanged(position);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(buying));

        // Disable fields until Edit is clicked
        holder.setEditable(false);
    }

    @Override
    public int getItemCount() {
        return buyingList.size();
    }

    public void updateData(List<BuyingEntity> newBuying) {
        this.buyingList = newBuying;
        notifyDataSetChanged();
    }

    public void removeBuying(BuyingEntity buying) {
        int index = buyingList.indexOf(buying);
        if (index >= 0) {
            buyingList.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void updateBuying(BuyingEntity updated) {
        for (int i = 0; i < buyingList.size(); i++) {
            if (buyingList.get(i).getServerId() != null &&
                    buyingList.get(i).getServerId().equals(updated.getServerId())) {
                buyingList.set(i, updated);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class BuyingViewHolder extends RecyclerView.ViewHolder {
        // Compact
        LinearLayout compactContainer;
        TextView buyDateCompact, buyAmountCompact, buyRateCompact;
        ImageView expandChevron;

        // Expanded
        HorizontalScrollView fullScroll;
        LinearLayout fullContainer;
        TextInputEditText buyDate, buyAmount, buyRate, buyCountry, buyName, buyDescription;
        MaterialButton btnEdit, btnDelete;
        ImageView collapseChevron;

        BuyingViewHolder(View itemView) {
            super(itemView);
            compactContainer = itemView.findViewById(R.id.buy_compact_container);
            buyDateCompact = itemView.findViewById(R.id.buyDateCompact);
            buyAmountCompact = itemView.findViewById(R.id.buyAmountCompact);
            buyRateCompact = itemView.findViewById(R.id.buyRateCompact);
            expandChevron = itemView.findViewById(R.id.buy_expand_chevron);

            fullScroll = itemView.findViewById(R.id.buy_full_scroll);
            fullContainer = itemView.findViewById(R.id.buy_full_container);
            collapseChevron = itemView.findViewById(R.id.buy_collapse_chevron);

            buyDate = itemView.findViewById(R.id.buyDate);
            buyAmount = itemView.findViewById(R.id.buyAmount);
            buyRate = itemView.findViewById(R.id.buyRate);
            buyCountry = itemView.findViewById(R.id.buyCountry);
            buyName = itemView.findViewById(R.id.buyName);
            buyDescription = itemView.findViewById(R.id.buyDescription);

            btnEdit = itemView.findViewById(R.id.buyEdit);
            btnDelete = itemView.findViewById(R.id.buyDelete);
        }

        void setEditable(boolean editable) {
            buyDate.setEnabled(editable);
            buyAmount.setEnabled(editable);
            buyRate.setEnabled(editable);
            buyCountry.setEnabled(editable);
            buyName.setEnabled(editable);
            buyDescription.setEnabled(editable);
        }
    }
}
