package com.example.mobiledbclient.ui.Kisenyi.openandclosings;

import android.app.DatePickerDialog;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OpenAndCloseAdapter extends RecyclerView.Adapter<OpenAndCloseAdapter.ViewHolder> {

    private List<OpenAndCloseEntity> data;
    private OnOpenAndCloseActionListener listener;

    public interface OnOpenAndCloseActionListener {
        void onEdit(OpenAndCloseEntity entity);
        void onDelete(OpenAndCloseEntity entity);
    }

    public OpenAndCloseAdapter(List<OpenAndCloseEntity> data, OnOpenAndCloseActionListener listener) {
        this.data = data != null ? new ArrayList<>(data) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public OpenAndCloseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.open_and_close_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpenAndCloseAdapter.ViewHolder holder, int position) {
        OpenAndCloseEntity entity = data.get(position);

        // Compact values
        holder.dateCompact.setText(entity.getDate());
        holder.descriptionCompact.setText(entity.getDescription());

        // Expanded values
        holder.date.setText(entity.getDate());
        holder.ugandaOpening.setText(formatDouble(entity.getUgandaOpening()));
        holder.kenyaOpening.setText(formatDouble(entity.getKenyaOpening()));
        holder.ugandaCashIn.setText(formatDouble(entity.getUgandaCashIn()));
        holder.kenyaCashIn.setText(formatDouble(entity.getKenyaCashIn()));
        holder.ugandaClosing.setText(formatDouble(entity.getUgandaClosing()));
        holder.kenyaClosing.setText(formatDouble(entity.getKenyaClosing()));
        holder.description.setText(entity.getDescription() != null ? entity.getDescription() : "");

        boolean expanded = entity.isEditing();
        holder.compactContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        holder.fullScroll.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // Expand chevron
        holder.expandChevron.setOnClickListener(v -> {
            entity.setEditing(true);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            notifyItemChanged(position);
        });

        // Collapse chevron
        holder.collapseChevron.setOnClickListener(v -> {
            entity.setEditing(false);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            notifyItemChanged(position);
        });

        // DatePicker integration
        holder.date.setOnClickListener(v -> {
            if (holder.date.isEnabled()) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(
                        v.getContext(),
                        (view1, year, month, dayOfMonth) -> {
                            String selectedDate = String.format(Locale.getDefault(),
                                    "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            holder.date.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            }
        });

        // Edit/Save button
        holder.btnEdit.setOnClickListener(v -> {
            if ("Edit".contentEquals(holder.btnEdit.getText())) {
                setEditable(holder, true);
                holder.btnEdit.setText("Save");
            } else {
                // Save changes
                entity.setDate(holder.date.getText().toString());
                entity.setUgandaOpening(parseDoubleSafe(holder.ugandaOpening.getText().toString()));
                entity.setKenyaOpening(parseDoubleSafe(holder.kenyaOpening.getText().toString()));
                entity.setUgandaCashIn(parseDoubleSafe(holder.ugandaCashIn.getText().toString()));
                entity.setKenyaCashIn(parseDoubleSafe(holder.kenyaCashIn.getText().toString()));
                entity.setUgandaClosing(parseDoubleSafe(holder.ugandaClosing.getText().toString()));
                entity.setKenyaClosing(parseDoubleSafe(holder.kenyaClosing.getText().toString()));
                entity.setDescription(holder.description.getText().toString());

                entity.setSynced(false);
                if (listener != null) listener.onEdit(entity);

                setEditable(holder, false);
                holder.btnEdit.setText("Edit");
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(entity);
        });

        // Initially disable editing
        setEditable(holder, false);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void updateData(List<OpenAndCloseEntity> newData) {
        this.data = newData != null ? new ArrayList<>(newData) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeOpenAndClose(OpenAndCloseEntity item) {
        if (item == null || data == null || data.isEmpty()) return;
        String targetDate = item.getDate();
        if (targetDate == null) return;
        for (int i = 0; i < data.size(); i++) {
            OpenAndCloseEntity e = data.get(i);
            if (targetDate.equals(e.getDate())) {
                data.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    private void setEditable(ViewHolder holder, boolean enabled) {
        holder.date.setEnabled(enabled);
        holder.ugandaOpening.setEnabled(enabled);
        holder.kenyaOpening.setEnabled(enabled);
        holder.ugandaCashIn.setEnabled(enabled);
        holder.kenyaCashIn.setEnabled(enabled);
        holder.ugandaClosing.setEnabled(enabled);
        holder.kenyaClosing.setEnabled(enabled);
        holder.description.setEnabled(enabled);
    }

    private double parseDoubleSafe(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return 0.0;
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String formatDouble(Double d) {
        if (d == null) return "0.00";
        return String.format(Locale.getDefault(), "%.2f", d);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Compact
        View compactContainer;
        TextView dateCompact, descriptionCompact;
        ImageView expandChevron, collapseChevron;

        // Expanded
        HorizontalScrollView fullScroll;
        EditText date, ugandaOpening, kenyaOpening, ugandaCashIn, kenyaCashIn,
                ugandaClosing, kenyaClosing, description;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            compactContainer = itemView.findViewById(R.id.openclose_compact_container);
            dateCompact = itemView.findViewById(R.id.opencloseDateCompact);
            descriptionCompact = itemView.findViewById(R.id.opencloseDescriptionCompact);
            expandChevron = itemView.findViewById(R.id.openclose_expand_chevron);
            collapseChevron = itemView.findViewById(R.id.openclose_collapse_chevron);

            fullScroll = itemView.findViewById(R.id.openclose_full_scroll);

            date = itemView.findViewById(R.id.openclosedate);
            ugandaOpening = itemView.findViewById(R.id.ugandaOpening);
            kenyaOpening = itemView.findViewById(R.id.kenyaOpening);
            ugandaCashIn = itemView.findViewById(R.id.ugandaCashIn);
            kenyaCashIn = itemView.findViewById(R.id.kenyaCashIn);
            ugandaClosing = itemView.findViewById(R.id.ugandaClosing);
            kenyaClosing = itemView.findViewById(R.id.kenyaClosing);
            description = itemView.findViewById(R.id.openclosedescription);

            btnEdit = itemView.findViewById(R.id.opencloseEdit);
            btnDelete = itemView.findViewById(R.id.opencloseDelete);
        }
    }
}
