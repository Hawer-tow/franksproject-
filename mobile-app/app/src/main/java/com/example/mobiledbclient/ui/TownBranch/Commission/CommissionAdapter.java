package com.example.mobiledbclient.ui.TownBranch.Commission;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;

import java.util.List;

public class CommissionAdapter extends RecyclerView.Adapter<CommissionAdapter.ViewHolder> {
    private List<CommissionEntity> data;
    private OnCommissionActionListener listener;

    public interface OnCommissionActionListener {
        void onEdit(CommissionEntity entity);
        void onDelete(CommissionEntity entity);
    }

    public CommissionAdapter(List<CommissionEntity> data, OnCommissionActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommissionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.commission_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommissionAdapter.ViewHolder holder, int position) {
        CommissionEntity entity = data.get(position);

        holder.cid.setText(nonNull(entity.getCid()));
        holder.cdate.setText(nonNull(entity.getCdate()));
        holder.ci.setText(formatDouble(entity.getCi()));
        holder.co.setText(formatDouble(entity.getCo()));

        setEditable(holder, false);
        holder.btnEdit.setText("Edit");

        holder.btnEdit.setOnClickListener(v -> {
            if ("Edit".contentEquals(holder.btnEdit.getText())) {
                setEditable(holder, true);
                holder.btnEdit.setText("Save");
            } else {
                entity.setCi(parseDoubleSafe(holder.ci.getText().toString()));
                entity.setCo(parseDoubleSafe(holder.co.getText().toString()));

                entity.setSynced(false);
                listener.onEdit(entity);

                setEditable(holder, false);
                holder.btnEdit.setText("Edit");
            }
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(entity));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    public void updateData(List<CommissionEntity> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public void updateCommission(CommissionEntity updated) {
        if (data == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getCid().equals(updated.getCid())) {
                data.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
        data.add(updated);
        notifyItemInserted(data.size() - 1);
    }

    public void removeCommission(CommissionEntity removed) {
        if (data == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getCid().equals(removed.getCid())) {
                data.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    private void setEditable(ViewHolder holder, boolean enabled) {
        holder.ci.setEnabled(enabled);
        holder.co.setEnabled(enabled);

        holder.cid.setEnabled(false);
        holder.cdate.setEnabled(false);
    }

    private static String nonNull(String s) { return s == null ? "" : s; }
    private static String formatDouble(Double d) { return d == null ? "" : String.valueOf(d); }
    private static Double parseDoubleSafe(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try { return Double.valueOf(s); } catch (NumberFormatException e) { return null; }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText cid, cdate;
        EditText ci, co;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            cid = itemView.findViewById(R.id.commissionCid);
            cdate = itemView.findViewById(R.id.commissionDate);
            ci = itemView.findViewById(R.id.commissionCI);
            co = itemView.findViewById(R.id.commissionCO);
            btnEdit = itemView.findViewById(R.id.commissionEdit);
            btnDelete = itemView.findViewById(R.id.commissionDelete);
        }
    }
}
