package com.example.mobiledbclient.ui.TownBranch.Deposit;

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

public class DepositAdapter extends RecyclerView.Adapter<DepositAdapter.ViewHolder> {
    private List<DepositEntity> data;
    private OnDepositActionListener listener;

    public interface OnDepositActionListener {
        void onEdit(DepositEntity entity);
        void onDelete(DepositEntity entity);
    }

    public DepositAdapter(List<DepositEntity> data, OnDepositActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DepositAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deposit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepositAdapter.ViewHolder holder, int position) {
        DepositEntity entity = data.get(position);

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

        setEditable(holder, false);
        holder.btnEdit.setText("Edit");

        holder.btnEdit.setOnClickListener(v -> {
            if ("Edit".contentEquals(holder.btnEdit.getText())) {
                setEditable(holder, true);
                holder.btnEdit.setText("Save");
            } else {
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

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(entity));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    public void updateData(List<DepositEntity> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public void updateDeposit(DepositEntity updated) {
        if (data == null) return;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getTid().equals(updated.getTid())) {
                data.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
        data.add(updated);
        notifyItemInserted(data.size() - 1);
    }

    public void removeDeposit(DepositEntity removed) {
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

    private static String nonNull(String s) { return s == null ? "" : s; }
    private static String formatDouble(Double d) { return d == null ? "" : String.valueOf(d); }
    private static Double parseDoubleSafe(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try { return Double.valueOf(s); } catch (NumberFormatException e) { return null; }
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
            tid = itemView.findViewById(R.id.depositTid);
            tdate = itemView.findViewById(R.id.depositDate);

            magent = itemView.findViewById(R.id.depositMagent);
            mpersonal = itemView.findViewById(R.id.depositMpersonal);
            aagent = itemView.findViewById(R.id.depositAagent);
            apersonal = itemView.findViewById(R.id.depositApersonal);

            EU = itemView.findViewById(R.id.depositEU);
            EUrate = itemView.findViewById(R.id.depositEUrate);
            EA = itemView.findViewById(R.id.depositEA);
            EArate = itemView.findViewById(R.id.depositEArate);

            JA = itemView.findViewById(R.id.depositJA);
            JArate = itemView.findViewById(R.id.depositJArate);
            MU = itemView.findViewById(R.id.depositMU);
            MUrate = itemView.findViewById(R.id.depositMUrate);

            MA = itemView.findViewById(R.id.depositMA);
            MArate = itemView.findViewById(R.id.depositMArate);
            AO = itemView.findViewById(R.id.depositAO);
            AOrate = itemView.findViewById(R.id.depositAOrate);

            AA = itemView.findViewById(R.id.depositAA);
            AArate = itemView.findViewById(R.id.depositAArate);
            TZ = itemView.findViewById(R.id.depositTZ);
            TZrate = itemView.findViewById(R.id.depositTZrate);

            Rwanda = itemView.findViewById(R.id.depositRwanda);
            RR = itemView.findViewById(R.id.depositRR);

            btnEdit = itemView.findViewById(R.id.depositEdit);
            btnDelete = itemView.findViewById(R.id.depositDelete);
        }
    }
}
