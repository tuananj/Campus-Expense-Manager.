package com.example.campusexpensemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> expenseList;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onEditClick(Expense expense);
        void onDeleteClick(Expense expense);
    }

    public ExpenseAdapter(Context context, List<Expense> expenseList, OnExpenseClickListener listener) {
        this.context = context;
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvCategory.setText(expense.getCategory());
        holder.tvDate.setText(expense.getDate());
        holder.tvDescription.setText(expense.getDescription());

        // Format amount
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvAmount.setText(formatter.format(expense.getAmount()) + " VNĐ");

        // Payment method icon
        String paymentIcon = "💳";
        if (expense.getPaymentMethod().equals("Tiền mặt")) {
            paymentIcon = "💵";
        } else if (expense.getPaymentMethod().equals("Chuyển khoản")) {
            paymentIcon = "🏦";
        }
        holder.tvPaymentMethod.setText(paymentIcon + " " + expense.getPaymentMethod());

        // Handle edit button
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEditClick(expense);
                }
            }
        });

        // Handle delete button
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(expense);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateList(List<Expense> newList) {
        this.expenseList = newList;
        notifyDataSetChanged();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAmount, tvDescription, tvPaymentMethod;
        ImageButton btnEdit, btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}