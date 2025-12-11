package com.example.campusexpensemanager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseOverviewActivity extends AppCompatActivity {

    private TextView tvTotalExpense, tvTotalBudget, tvRemainingBudget, tvProgressPercentage;
    private TextView tvNoPieData, tvNoCategoryData, tvNoTrendData;
    private ProgressBar progressOverall;
    private LinearLayout layoutPieChart, layoutCategoryBreakdown, layoutTrend;
    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String username;
    private String selectedMonth;
    private String selectedYear;

    private int[] categoryColors = {
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#795548")  // Brown
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_overview);

        // Khởi tạo
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CampusExpensePrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        // Ánh xạ views
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        tvNoPieData = findViewById(R.id.tvNoPieData);
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData);
        tvNoTrendData = findViewById(R.id.tvNoTrendData);
        progressOverall = findViewById(R.id.progressOverall);
        layoutPieChart = findViewById(R.id.layoutPieChart);
        layoutCategoryBreakdown = findViewById(R.id.layoutCategoryBreakdown);
        layoutTrend = findViewById(R.id.layoutTrend);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnLoad = findViewById(R.id.btnLoad);
        btnBack = findViewById(R.id.btnBack);

        // Setup Spinners
        setupMonthYearSpinners();

        // Set current month/year
        Calendar calendar = Calendar.getInstance();
        selectedMonth = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        selectedYear = String.valueOf(calendar.get(Calendar.YEAR));
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));

        // Load data
        loadOverviewData();

        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý nút Load
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth = String.format("%02d", spinnerMonth.getSelectedItemPosition() + 1);
                selectedYear = spinnerYear.getSelectedItem().toString();
                loadOverviewData();
            }
        });
    }

    private void setupMonthYearSpinners() {
        // Month spinner
        String[] months = {
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Year spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 2; i <= currentYear + 1; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(2); // Select current year
    }

    private void loadOverviewData() {
        // Calculate total expense
        double totalExpense = databaseHelper.getTotalExpenseByMonth(username, selectedMonth, selectedYear);

        // Calculate total budget
        List<Budget> budgets = databaseHelper.getAllBudgets(username);
        double totalBudget = 0;
        for (Budget budget : budgets) {
            if (budget.getMonth().equals(selectedMonth) && budget.getYear().equals(selectedYear)) {
                totalBudget += budget.getBudgetAmount();
            }
        }

        double remainingBudget = totalBudget - totalExpense;
        double percentage = totalBudget > 0 ? (totalExpense / totalBudget) * 100 : 0;

        // Update summary
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalExpense.setText(formatter.format(totalExpense) + " VNĐ");
        tvTotalBudget.setText(formatter.format(totalBudget) + " VNĐ");
        tvRemainingBudget.setText(formatter.format(remainingBudget) + " VNĐ");

        if (totalBudget > 0) {
            tvProgressPercentage.setText(String.format("Đã sử dụng %.1f%%", percentage));
            progressOverall.setProgress((int) percentage);
        } else {
            tvProgressPercentage.setText("Chưa thiết lập ngân sách");
            progressOverall.setProgress(0);
        }

        // Change color based on percentage
        if (percentage >= 100) {
            progressOverall.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
            tvRemainingBudget.setTextColor(Color.parseColor("#F44336"));
        } else if (percentage >= 80) {
            progressOverall.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
            tvRemainingBudget.setTextColor(Color.parseColor("#FF9800"));
        } else {
            progressOverall.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Blue
            tvRemainingBudget.setTextColor(Color.parseColor("#2196F3"));
        }

        // Load category breakdown
        loadCategoryBreakdown(totalExpense);

        // Load pie chart
        loadPieChart(totalExpense);

        // Load trend
        loadTrendData();
    }

    private void loadCategoryBreakdown(double totalExpense) {
        layoutCategoryBreakdown.removeAllViews();

        if (totalExpense == 0) {
            tvNoCategoryData.setVisibility(View.VISIBLE);
            return;
        }

        tvNoCategoryData.setVisibility(View.GONE);

        // Get expenses by category
        Map<String, Double> categoryMap = new HashMap<>();
        List<Expense> expenses = databaseHelper.getAllExpenses(username);

        for (Expense expense : expenses) {
            String[] dateParts = expense.getDate().split("/");
            if (dateParts.length == 3) {
                String expenseMonth = dateParts[1];
                String expenseYear = dateParts[2];

                if (expenseMonth.equals(selectedMonth) && expenseYear.equals(selectedYear)) {
                    String category = expense.getCategory();
                    double currentAmount = categoryMap.getOrDefault(category, 0.0);
                    categoryMap.put(category, currentAmount + expense.getAmount());
                }
            }
        }

        // Create category expense list
        List<CategoryExpense> categoryExpenses = new ArrayList<>();
        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            double percentage = (entry.getValue() / totalExpense) * 100;
            categoryExpenses.add(new CategoryExpense(
                    entry.getKey(),
                    entry.getValue(),
                    percentage,
                    categoryColors[colorIndex % categoryColors.length]
            ));
            colorIndex++;
        }

        // Sort by amount (descending)
        categoryExpenses.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        // Add views
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CategoryExpense catExp : categoryExpenses) {
            View itemView = inflater.inflate(R.layout.item_category_breakdown, layoutCategoryBreakdown, false);

            View colorIndicator = itemView.findViewById(R.id.viewColorIndicator);
            TextView tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            TextView tvCategoryAmount = itemView.findViewById(R.id.tvCategoryAmount);
            TextView tvCategoryPercentage = itemView.findViewById(R.id.tvCategoryPercentage);
            ProgressBar progressCategory = itemView.findViewById(R.id.progressCategory);

            colorIndicator.setBackgroundColor(catExp.getColor());
            tvCategoryName.setText(catExp.getCategory());
            tvCategoryAmount.setText(formatter.format(catExp.getAmount()) + " VNĐ");
            tvCategoryPercentage.setText(String.format("%.1f%% của tổng chi tiêu", catExp.getPercentage()));
            progressCategory.setProgress((int) catExp.getPercentage());
            progressCategory.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(catExp.getColor()));

            layoutCategoryBreakdown.addView(itemView);
        }
    }

    private void loadPieChart(double totalExpense) {
        layoutPieChart.removeAllViews();

        if (totalExpense == 0) {
            TextView tvNoData = new TextView(this);
            tvNoData.setText("Chưa có dữ liệu chi tiêu");
            tvNoData.setTextSize(16);
            tvNoData.setTextColor(Color.parseColor("#999999"));
            tvNoData.setGravity(android.view.Gravity.CENTER);
            layoutPieChart.addView(tvNoData);
            return;
        }

        // Get expenses by category
        Map<String, Double> categoryMap = new HashMap<>();
        List<Expense> expenses = databaseHelper.getAllExpenses(username);

        for (Expense expense : expenses) {
            String[] dateParts = expense.getDate().split("/");
            if (dateParts.length == 3) {
                String expenseMonth = dateParts[1];
                String expenseYear = dateParts[2];

                if (expenseMonth.equals(selectedMonth) && expenseYear.equals(selectedYear)) {
                    String category = expense.getCategory();
                    double currentAmount = categoryMap.getOrDefault(category, 0.0);
                    categoryMap.put(category, currentAmount + expense.getAmount());
                }
            }
        }

        // Create simple bar chart representation
        LinearLayout chartLayout = new LinearLayout(this);
        chartLayout.setOrientation(LinearLayout.VERTICAL);
        chartLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        chartLayout.setPadding(16, 16, 16, 16);

        // Sort categories by amount
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryMap.entrySet());
        sortedCategories.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int colorIndex = 0;
        double maxAmount = sortedCategories.isEmpty() ? 1 : sortedCategories.get(0).getValue();

        for (Map.Entry<String, Double> entry : sortedCategories) {
            double percentage = (entry.getValue() / totalExpense) * 100;
            double barWidth = (entry.getValue() / maxAmount) * 100;

            // Category label with percentage
            LinearLayout labelLayout = new LinearLayout(this);
            labelLayout.setOrientation(LinearLayout.HORIZONTAL);
            labelLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView label = new TextView(this);
            label.setText(entry.getKey());
            label.setTextSize(14);
            label.setTextColor(Color.parseColor("#333333"));
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            labelLayout.addView(label);

            TextView percentageLabel = new TextView(this);
            percentageLabel.setText(String.format("%.1f%%", percentage));
            percentageLabel.setTextSize(14);
            percentageLabel.setTextColor(Color.parseColor("#666666"));
            percentageLabel.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            labelLayout.addView(percentageLabel);

            chartLayout.addView(labelLayout);

            // Bar
            View bar = new View(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    (int) (barWidth * 2.5), // Scale for better visibility
                    40
            );
            barParams.setMargins(0, 8, 0, 16);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(categoryColors[colorIndex % categoryColors.length]);
            chartLayout.addView(bar);

            colorIndex++;
        }

        layoutPieChart.addView(chartLayout);
    }

    private void loadTrendData() {
        layoutTrend.removeAllViews();

        // Get last 3 months including current
        Calendar calendar = Calendar.getInstance();
        int currentMonth = Integer.parseInt(selectedMonth);
        int currentYear = Integer.parseInt(selectedYear);

        calendar.set(Calendar.MONTH, currentMonth - 1);
        calendar.set(Calendar.YEAR, currentYear);

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        boolean hasData = false;
        double maxAmount = 0;

        // First pass: find max amount for scaling
        List<MonthData> monthDataList = new ArrayList<>();
        for (int i = 2; i >= 0; i--) {
            calendar.add(Calendar.MONTH, -1);
            String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
            String year = String.valueOf(calendar.get(Calendar.YEAR));

            double total = databaseHelper.getTotalExpenseByMonth(username, month, year);

            String[] monthNames = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
            String monthName = monthNames[calendar.get(Calendar.MONTH)];

            monthDataList.add(new MonthData(monthName, year, total));

            if (total > maxAmount) maxAmount = total;
            if (total > 0) hasData = true;

            calendar.add(Calendar.MONTH, 1); // Reset for next iteration
        }

        // Reverse to show oldest to newest
        for (int i = monthDataList.size() - 1; i >= 0; i--) {
            MonthData data = monthDataList.get(i);

            // Month label
            LinearLayout labelLayout = new LinearLayout(this);
            labelLayout.setOrientation(LinearLayout.HORIZONTAL);
            labelLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            labelLayout.setPadding(0, 8, 0, 4);

            TextView monthLabel = new TextView(this);
            monthLabel.setText(data.monthName + " " + data.year);
            monthLabel.setTextSize(14);
            monthLabel.setTextColor(Color.parseColor("#333333"));
            monthLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            labelLayout.addView(monthLabel);

            TextView amountLabel = new TextView(this);
            amountLabel.setText(formatter.format(data.amount) + " VNĐ");
            amountLabel.setTextSize(14);
            amountLabel.setTextColor(Color.parseColor("#F44336"));
            amountLabel.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            amountLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            labelLayout.addView(amountLabel);

            layoutTrend.addView(labelLayout);

            // Progress bar for visual
            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    24
            );
            barParams.setMargins(0, 4, 0, 16);
            bar.setLayoutParams(barParams);
            bar.setMax(100);

            int progress = maxAmount > 0 ? (int) ((data.amount / maxAmount) * 100) : 0;
            bar.setProgress(progress);
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3")));
            layoutTrend.addView(bar);
        }

        tvNoTrendData.setVisibility(hasData ? View.GONE : View.VISIBLE);
    }

    // Helper class for month data
    private static class MonthData {
        String monthName;
        String year;
        double amount;

        MonthData(String monthName, String year, double amount) {
            this.monthName = monthName;
            this.year = year;
            this.amount = amount;
        }
    }
}
