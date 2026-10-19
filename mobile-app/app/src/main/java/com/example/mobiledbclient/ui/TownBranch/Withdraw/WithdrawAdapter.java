package com.example.mobiledbclient.ui.TownBranch.Withdraw;

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

public class WithdrawAdapter extends RecyclerView.Adapter<WithdrawAdapter.ViewHolder> {
    private List<WithdrawEntity> data;
    private OnWithdrawActionListener listener;

    public interface OnWithdrawActionListener {
        void onEdit(WithdrawEntity entity);
        void onDelete(WithdrawEntity entity);
    }

    public WithdrawAdapter(List<WithdrawEntity> data, OnWithdrawActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WithdrawAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.withdraw_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WithdrawAdapter.ViewHolder holder, int position) {
        WithdrawEntity entity = data.get(position);

        holder.tid.setText(nonNull(entity.getTid()));
        holder.tdate.setText(nonNull(entity.getTdate()));
        holder.magent.setText(formatDouble(entity.getMagent()));
        holder.mpersonal.setText(formatDouble(entity.getMpersonal()));
        holder.aagent.setText(formatDouble(entity.getAagent()));
        holder.apersonal.setText(formatDouble(entity.getApersonal()));
        holder.EU.setText(formatDouble(entity.getEU()));
        holder.EUrate.setText(formatDouble(entity.getEUrate()));
        holder.EA.setText(formatDouble(entity.getEA()));
        holder.EArate.setText(formatDouble(entity.getEArate()));
        holder.JA.setText(formatDouble(entity.getJA()));
        holder.JArate.setText(formatDouble(entity.getJArate()));
        holder.MU.setText(formatDouble(entity.getMU()));
        holder.MUrate.setText(formatDouble(entity.getMUrate()));
        holder.MA.setText(formatDouble(entity.getMA()));
        holder.MArate.setText(formatDouble(entity.getMArate()));
        holder.AO.setText(formatDouble(entity.getAO()));
        holder.AOrate.setText(formatDouble(entity.getAOrate()));
        holder.AA.setText(formatDouble(entity.getAA()));
        holder.AArate.setText(formatDouble(entity.getAArate()));
        holder.TZ.setText(formatDouble(entity.getTZ()));
        holder.TZrate.setText(formatDouble(entity.getTZrate()));
        holder.Rwanda.setText(formatDouble(entity.getRwanda()));
        holder.RR.setText(formatDouble(entity.getRR()));

        // Ensure editing disabled initially
        setEditable(holder, false);
        holder.btnEdit.setText("Edit");

        holder.btnEdit.setOnClickListener(v -> {
            if ("Edit".contentEquals(holder.btnEdit.getText())) {
                setEditable(holder, true);
                holder.btnEdit.setText("Save");
            } else {
                // Save: parse values safely and update entity
                entity.setMagent(parseDoubleSafe(holder.magent.getText().toString()));
                entity.setMpersonal(parseDoubleSafe(holder.mpersonal.getText().toString()));
                entity.setAagent(parseDoubleSafe(holder.aagent.getText().toString()));
                entity.setApersonal(parseDoubleSafe(holder.apersonal.getText().toString()));
                entity.setEU(parseDoubleSafe(holder.EU.getText().toString()));
                entity.setEUrate(parseDoubleSafe(holder.EUrate.getText().toString()));
                entity.setEA(parseDoubleSafe(holder.EA.getText().toString()));
                entity.setEArate(parseDoubleSafe(holder.EArate.getText().toString()));
                entity.setJA(parseDoubleSafe(holder.JA.getText().toString()));
                entity.setJArate(parseDoubleSafe(holder.JArate.getText().toString()));
                entity.setMU(parseDoubleSafe(holder.MU.getText().toString()));
                entity.setMUrate(parseDoubleSafe(holder.MUrate.getText().toString()));
                entity.setMA(parseDoubleSafe(holder.MA.getText().toString()));
                entity.setMArate(parseDoubleSafe(holder.MArate.getText().toString()));
                entity.setAO(parseDoubleSafe(holder.AO.getText().toString()));
                entity.setAOrate(parseDoubleSafe(holder.AOrate.getText().toString()));
                entity.setAA(parseDoubleSafe(holder.AA.getText().toString()));
                entity.setAArate(parseDoubleSafe(holder.AArate.getText().toString()));
                entity.setTZ(parseDoubleSafe(holder.TZ.getText().toString()));
                entity.setTZrate(parseDoubleSafe(holder.TZrate.getText().toString()));
                entity.setRwanda(parseDoubleSafe(holder.Rwanda.getText().toString()));
                entity.setRR(parseDoubleSafe(holder.RR.getText().toString()));

                entity.setSynced(false);
                listener.onEdit(entity);

                setEditable(holder, false);
                holder.btnEdit.setText("Edit");
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(entity);
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void updateData(List<WithdrawEntity> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public void updateWithdraw(WithdrawEntity updated) {
        if (data == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getTid().equals(updated.getTid())) {
                data.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
        // if not found, add
        data.add(updated);
        notifyItemInserted(data.size() - 1);
    }

    public void removeWithdraw(WithdrawEntity removed) {
        if (data == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getTid().equals(removed.getTid())) {
                data.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    private void setEditable(ViewHolder holder, boolean enabled) {
        // enable/disable all editable fields; keep primary key and date disabled
        holder.magent.setEnabled(enabled);
        holder.mpersonal.setEnabled(enabled);
        holder.aagent.setEnabled(enabled);
        holder.apersonal.setEnabled(enabled);
        holder.EU.setEnabled(enabled);
        holder.EUrate.setEnabled(enabled);
        holder.EA.setEnabled(enabled);
        holder.EArate.setEnabled(enabled);
        holder.JA.setEnabled(enabled);
        holder.JArate.setEnabled(enabled);
        holder.MU.setEnabled(enabled);
        holder.MUrate.setEnabled(enabled);
        holder.MA.setEnabled(enabled);
        holder.MArate.setEnabled(enabled);
        holder.AO.setEnabled(enabled);
        holder.AOrate.setEnabled(enabled);
        holder.AA.setEnabled(enabled);
        holder.AArate.setEnabled(enabled);
        holder.TZ.setEnabled(enabled);
        holder.TZrate.setEnabled(enabled);
        holder.Rwanda.setEnabled(enabled);
        holder.RR.setEnabled(enabled);

        holder.tid.setEnabled(false);
        holder.tdate.setEnabled(false);
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    private static String formatDouble(Double d) {
        return d == null ? "" : String.valueOf(d);
    }

    private static Double parseDoubleSafe(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText tid, tdate;
        EditText magent, mpersonal, aagent, apersonal;
        EditText EU, EUrate, EA, EArate;
        EditText JA, JArate, MU, MUrate;
        EditText MA, MArate, AO, AOrate;
        EditText AA, AArate, TZ, TZrate;
        EditText Rwanda, RR;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tid = itemView.findViewById(R.id.withdrawTid);
            tdate = itemView.findViewById(R.id.withdrawDate);

            magent = itemView.findViewById(R.id.withdrawMagent);
            mpersonal = itemView.findViewById(R.id.withdrawMpersonal);
            aagent = itemView.findViewById(R.id.withdrawAagent);
            apersonal = itemView.findViewById(R.id.withdrawApersonal);

            EU = itemView.findViewById(R.id.withdrawEU);
            EUrate = itemView.findViewById(R.id.withdrawEUrate);
            EA = itemView.findViewById(R.id.withdrawEA);
            EArate = itemView.findViewById(R.id.withdrawEArate);

            JA = itemView.findViewById(R.id.withdrawJA);
            JArate = itemView.findViewById(R.id.withdrawJArate);
            MU = itemView.findViewById(R.id.withdrawMU);
            MUrate = itemView.findViewById(R.id.withdrawMUrate);

            MA = itemView.findViewById(R.id.withdrawMA);
            MArate = itemView.findViewById(R.id.withdrawMArate);
            AO = itemView.findViewById(R.id.withdrawAO);
            AOrate = itemView.findViewById(R.id.withdrawAOrate);

            AA = itemView.findViewById(R.id.withdrawAA);
            AArate = itemView.findViewById(R.id.withdrawAArate);
            TZ = itemView.findViewById(R.id.withdrawTZ);
            TZrate = itemView.findViewById(R.id.withdrawTZrate);

            Rwanda = itemView.findViewById(R.id.withdrawRwanda);
            RR = itemView.findViewById(R.id.withdrawRR);

            btnEdit = itemView.findViewById(R.id.withdrawEdit);
            btnDelete = itemView.findViewById(R.id.withdrawDelete);
        }
    }
}
