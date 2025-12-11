package com.example.campusexpensemanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private Context context;
    private List<Budget> budgetList;
    private DatabaseHelper databaseHelper;
    private String username;
    private OnBudgetClickListener listener;

    public interface OnBudgetClickListener {
        void onEditClick(Budget budget);
        void onDeleteClick(Budget budget);
    }

    public BudgetAdapter(Context context, List<Budget> budgetList, String username, OnBudgetClickListener listener) {
        this.context = context;
        this.budgetList = budgetList;
        this.username = username;
        this.listener = listener;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Category name
        holder.tvBudgetCategory.setText(budget.getCategory());

        // Budget amount
        holder.tvBudgetAmount.setText(formatter.format(budget.getBudgetAmount()) + " VNĐ");

        // Get total spent for this category
        double spent = databaseHelper.getTotalExpenseByCategoryAndMonth(
                username,
                budget.getCategory(),
                budget.getMonth(),
                budget.getYear()
        );

        double remaining = budget.getBudgetAmount() - spent;
        double percentage = (spent / budget.getBudgetAmount()) * 100;

        // Spent amount
        holder.tvBudgetSpent.setText("Đã chi: " + formatter.format(spent) + " VNĐ");

        // Remaining amount
        holder.tvBudgetRemaining.setText("Còn: " + formatter.format(remaining) + " VNĐ");

        // Percentage
        holder.tvBudgetPercentage.setText(String.format("Đã sử dụng %.1f%%", percentage));

        // Progress bar
        holder.progressBudget.setMax(100);
        holder.progressBudget.setProgress((int) percentage);

        // Change color based on percentage
        if (percentage >= 100) {
            holder.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
            holder.tvBudgetPercentage.setTextColor(Color.parseColor("#F44336"));
        } else if (percentage >= 80) {
            holder.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
            holder.tvBudgetPercentage.setTextColor(Color.parseColor("#FF9800"));
        } else {
            holder.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
            holder.tvBudgetPercentage.setTextColor(Color.parseColor("#4CAF50"));
        }

        // Edit button
        holder.btnEditBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEditClick(budget);
                }
            }
        });

        // Delete button
        holder.btnDeleteBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(budget);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void updateList(List<Budget> newList) {
        this.budgetList = newList;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvBudgetCategory, tvBudgetAmount, tvBudgetSpent, tvBudgetRemaining, tvBudgetPercentage;
        ProgressBar progressBudget;
        ImageButton btnEditBudget, btnDeleteBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvBudgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvBudgetSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvBudgetRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            tvBudgetPercentage = itemView.findViewById(R.id.tvBudgetPercentage);
            progressBudget = itemView.findViewById(R.id.progressBudget);
            btnEditBudget = itemView.findViewById(R.id.btnEditBudget);
            btnDeleteBudget = itemView.findViewById(R.id.btnDeleteBudget);
        }
    }
}