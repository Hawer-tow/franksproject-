package com.example.mobiledbclient.ui.Kisenyi.sales;

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

public class SellingAdapter extends RecyclerView.Adapter<SellingAdapter.SellingViewHolder> {
    private List<SellingEntity> sellingList;
    private OnSellingActionListener listener;

    public interface OnSellingActionListener {
        void onEdit(SellingEntity selling);
        void onDelete(SellingEntity selling);
    }

    public SellingAdapter(List<SellingEntity> sellingList, OnSellingActionListener listener) {
        this.sellingList = sellingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SellingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selling_item, parent, false);
        return new SellingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellingViewHolder holder, int position) {
        SellingEntity selling = sellingList.get(position);

        // Compact values
        holder.sellDateCompact.setText(selling.getSellingDate());
        holder.sellAmountCompact.setText(String.valueOf(selling.getSellingAmount()));
        holder.sellRateCompact.setText(String.valueOf(selling.getSellingRate()));

        // Expanded values
        holder.selldate.setText(selling.getSellingDate());
        holder.sellamount.setText(String.valueOf(selling.getSellingAmount()));
        holder.sellrate.setText(String.valueOf(selling.getSellingRate()));
        holder.sellcountry.setText(selling.getReceivingCountry());
        holder.sellphoneNo.setText(selling.getPhoneNo());
        holder.sellrecipientname.setText(selling.getRecipientName());
        holder.sellselldescription.setText(selling.getSellDescription());
        holder.sellsenderaccount.setText(selling.getSenderAccount());
        holder.sellreceiveraccount.setText(selling.getReceiverAccount());
        holder.selltransactionid.setText(selling.getTransactionId());
        holder.sellstatus.setText(selling.getStatus());

        boolean expanded = selling.isEditing();
        holder.compactContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        holder.fullScroll.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // Expand chevron
        holder.expandChevron.setOnClickListener(v -> {
            selling.setEditing(true);
            notifyItemChanged(position);
        });

        // Collapse chevron
        holder.collapseChevron.setOnClickListener(v -> {
            selling.setEditing(false);
            notifyItemChanged(position);
        });

        // Date picker for selldate
        holder.selldate.setOnClickListener(v -> {
            if (holder.selldate.isEnabled()) { // only when editing
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(
                        v.getContext(),
                        (view1, year, month, dayOfMonth) -> {
                            String selectedDate = String.format(Locale.getDefault(),
                                    "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            holder.selldate.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            }
        });

        // Edit button in expanded row
        holder.sellEdit.setOnClickListener(v -> {
            if (holder.sellEdit.getText().equals("Edit")) {
                holder.sellEdit.setText("Save");
                holder.setEditable(true, selling.getSaleType());
            } else {
                // Save changes depending on saleType
                selling.setSellingDate(holder.selldate.getText().toString());
                try {
                    selling.setSellingAmount(Double.parseDouble(holder.sellamount.getText().toString()));
                } catch (NumberFormatException e) {
                    selling.setSellingAmount(0);
                }
                try {
                    selling.setSellingRate(Double.parseDouble(holder.sellrate.getText().toString()));
                } catch (NumberFormatException e) {
                    selling.setSellingRate(0);
                }
                selling.setReceivingCountry(holder.sellcountry.getText().toString());
                selling.setRecipientName(holder.sellrecipientname.getText().toString());
                selling.setSellDescription(holder.sellselldescription.getText().toString());
                selling.setStatus(holder.sellstatus.getText().toString());

                if ("USER_INITIATED_BANK".equalsIgnoreCase(selling.getSaleType())) {
                    selling.setSenderAccount(holder.sellsenderaccount.getText().toString());
                    selling.setReceiverAccount(holder.sellreceiveraccount.getText().toString());
                } else if ("USER_INITIATED_MPESA".equalsIgnoreCase(selling.getSaleType())) {
                    selling.setPhoneNo(holder.sellphoneNo.getText().toString());
                    selling.setTransactionId(holder.selltransactionid.getText().toString());
                }

                selling.setEditing(false);
                selling.setSynced(false);
                listener.onEdit(selling);

                holder.sellEdit.setText("Edit");
                holder.setEditable(false, selling.getSaleType());

                notifyItemChanged(position);
            }
        });

        // Delete button
        holder.sellDelete.setOnClickListener(v -> listener.onDelete(selling));

        // Disable fields until Edit is clicked
        holder.setEditable(false, selling.getSaleType());
    }

    @Override
    public int getItemCount() {
        return sellingList.size();
    }

    public void updateData(List<SellingEntity> newSelling) {
        this.sellingList = newSelling;
        notifyDataSetChanged();
    }

    public void removeSelling(SellingEntity selling) {
        int index = sellingList.indexOf(selling);
        if (index >= 0) {
            sellingList.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void updateSelling(SellingEntity updated) {
        for (int i = 0; i < sellingList.size(); i++) {
            if (sellingList.get(i).getServerId() != null &&
                    sellingList.get(i).getServerId().equals(updated.getServerId())) {
                sellingList.set(i, updated);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class SellingViewHolder extends RecyclerView.ViewHolder {
        // Compact
        LinearLayout compactContainer;
        TextView sellDateCompact, sellAmountCompact, sellRateCompact;
        ImageView expandChevron;

        // Expanded
        HorizontalScrollView fullScroll;
        LinearLayout fullContainer;
        TextInputEditText selldate, sellamount, sellrate, sellcountry, sellphoneNo,
                sellrecipientname, sellselldescription, selltransactionid, sellstatus,
                sellsenderaccount, sellreceiveraccount;
        MaterialButton sellEdit, sellDelete;
        ImageView collapseChevron;

        SellingViewHolder(View itemView) {
            super(itemView);
            compactContainer = itemView.findViewById(R.id.sell_compact_container);
            sellDateCompact = itemView.findViewById(R.id.sellDateCompact);
            sellAmountCompact = itemView.findViewById(R.id.sellAmountCompact);
            sellRateCompact = itemView.findViewById(R.id.sellRateCompact);
            expandChevron = itemView.findViewById(R.id.sell_expand_chevron);

            fullScroll = itemView.findViewById(R.id.sell_full_scroll);
            fullContainer = itemView.findViewById(R.id.sell_full_container);
            collapseChevron = itemView.findViewById(R.id.sell_collapse_chevron);

            selldate = itemView.findViewById(R.id.selldate);
            sellamount = itemView.findViewById(R.id.sellamount);
            sellrate = itemView.findViewById(R.id.sellrate);
            sellcountry = itemView.findViewById(R.id.sellcountry);
            sellphoneNo = itemView.findViewById(R.id.sellphoneNo);
            sellrecipientname = itemView.findViewById(R.id.sellrecipientname);
            sellselldescription = itemView.findViewById(R.id.sellselldescription);
            selltransactionid = itemView.findViewById(R.id.selltransactionid);
            sellstatus = itemView.findViewById(R.id.sellstatus);
            sellsenderaccount = itemView.findViewById(R.id.sellsenderaccount);
            sellreceiveraccount = itemView.findViewById(R.id.sellreceiveraccount);

            sellEdit = itemView.findViewById(R.id.sellEdit);
            sellDelete = itemView.findViewById(R.id.sellDelete);
        }

        void setEditable(boolean editable, String saleType) {
            if ("USER_INITIATED_BANK".equalsIgnoreCase(saleType)) {
                selldate.setEnabled(editable);
                sellamount.setEnabled(editable);
                sellrate.setEnabled(editable);
                sellcountry.setEnabled(editable);
                sellrecipientname.setEnabled(editable);
                sellselldescription.setEnabled(editable);
                sellstatus.setEnabled(editable);
                sellsenderaccount.setEnabled(editable);
                sellreceiveraccount.setEnabled(editable);
            } else if ("USER_INITIATED_MPESA".equalsIgnoreCase(saleType)) {
                selldate.setEnabled(editable);
                sellamount.setEnabled(editable);
                sellrate.setEnabled(editable);
                sellcountry.setEnabled(editable);
                sellphoneNo.setEnabled(editable);
                sellrecipientname.setEnabled(editable);
                sellselldescription.setEnabled(editable);
                selltransactionid.setEnabled(editable);
                sellstatus.setEnabled(editable);
            } else {
                // System transactions: lock all except rate + description + status
                selldate.setEnabled(false);
                sellamount.setEnabled(false);
                sellcountry.setEnabled(false);
                sellphoneNo.setEnabled(false);
                sellrecipientname.setEnabled(false);
                selltransactionid.setEnabled(false);
                sellsenderaccount.setEnabled(false);
                sellreceiveraccount.setEnabled(false);
                sellrate.setEnabled(editable);
                sellselldescription.setEnabled(editable);
                sellstatus.setEnabled(editable);
            }
        }
    }
}
