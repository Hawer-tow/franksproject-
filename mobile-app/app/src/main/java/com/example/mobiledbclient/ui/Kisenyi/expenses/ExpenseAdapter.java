package com.example.mobiledbclient.ui.Kisenyi.expenses;

import android.app.DatePickerDialog;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onEdit(ExpenseEntity expense);
        void onDelete(ExpenseEntity expense);
    }

    private List<ExpenseEntity> expenses;
    private final OnExpenseActionListener listener;

    public ExpenseAdapter(List<ExpenseEntity> expenses, OnExpenseActionListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenses.get(position);

        // Compact values
        holder.txtDate.setText(expense.getDate());
        holder.txtAmount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));

        // Expanded values
        holder.editDate.setText(expense.getDate());
        holder.editAmount.setText(String.valueOf(expense.getAmount()));
        holder.editDescription.setText(expense.getDescription());

        boolean expanded = expense.isEditing();
        holder.compactContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        holder.fullScroll.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // Expand chevron
        holder.expandChevron.setOnClickListener(v -> {
            expense.setEditing(true);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            notifyItemChanged(position);
        });

        // Collapse chevron
        holder.collapseChevron.setOnClickListener(v -> {
            expense.setEditing(false);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            notifyItemChanged(position);
        });

        // Date picker
        holder.editDate.setOnClickListener(v -> {
            if (holder.editDate.isEnabled()) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(
                        v.getContext(),
                        (view1, year, month, dayOfMonth) -> {
                            String selectedDate = String.format(Locale.getDefault(),
                                    "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            holder.editDate.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            }
        });

        // Edit/Save button
        holder.btnEditExpanded.setOnClickListener(v -> {
            if (holder.btnEditExpanded.getText().equals("Edit")) {
                holder.btnEditExpanded.setText("Save");
                holder.editDate.setEnabled(true);
                holder.editAmount.setEnabled(true);
                holder.editDescription.setEnabled(true);
                TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            } else {
                // Save edits
                expense.setDate(holder.editDate.getText().toString());
                try {
                    expense.setAmount(Double.parseDouble(holder.editAmount.getText().toString()));
                } catch (NumberFormatException e) {
                    expense.setAmount(0);
                }
                expense.setDescription(holder.editDescription.getText().toString());

                expense.setEditing(false);
                expense.setSynced(false);

                listener.onEdit(expense);

                holder.btnEditExpanded.setText("Edit");
                holder.editDate.setEnabled(false);
                holder.editAmount.setEnabled(false);
                holder.editDescription.setEnabled(false);

                TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
                notifyItemChanged(position);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(expense));

        // Disable fields until Edit is clicked
        holder.editDate.setEnabled(false);
        holder.editAmount.setEnabled(false);
        holder.editDescription.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateData(List<ExpenseEntity> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    public void removeExpense(ExpenseEntity expense) {
        int index = expenses.indexOf(expense);
        if (index >= 0) {
            expenses.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void updateExpense(ExpenseEntity updated) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getServerId() != null &&
                    expenses.get(i).getServerId().equals(updated.getServerId())) {
                expenses.set(i, updated);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        View compactContainer;
        HorizontalScrollView fullScroll;
        TextView txtDate, txtAmount;
        TextInputEditText editDate, editAmount, editDescription;
        MaterialButton btnEditExpanded, btnDelete;
        ImageView expandChevron, collapseChevron;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            compactContainer = itemView.findViewById(R.id.compact_container);
            fullScroll = itemView.findViewById(R.id.full_scroll);

            txtDate = itemView.findViewById(R.id.txtDate);
            txtAmount = itemView.findViewById(R.id.txtAmount);

            editDate = itemView.findViewById(R.id.editDate);
            editAmount = itemView.findViewById(R.id.editAmount);
            editDescription = itemView.findViewById(R.id.editDescription);

            btnEditExpanded = itemView.findViewById(R.id.btnEditExpanded);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            expandChevron = itemView.findViewById(R.id.expand_chevron);
            collapseChevron = itemView.findViewById(R.id.collapse_chevron);
        }
    }
}
