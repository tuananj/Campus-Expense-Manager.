package com.example.campusexpensemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetSettingActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetClickListener {

    private RecyclerView recyclerViewBudgets;
    private TextView tvTotalBudget, tvTotalSpent, tvRemaining;
    private LinearLayout layoutEmpty;
    private ImageButton btnBack, btnAddBudget;
    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoadBudget;

    private BudgetAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private List<Budget> budgetList;
    private String username;
    private String selectedMonth;
    private String selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setting);

        // Khởi tạo
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CampusExpensePrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        // Ánh xạ views
        recyclerViewBudgets = findViewById(R.id.recyclerViewBudgets);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvRemaining = findViewById(R.id.tvRemaining);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnBack = findViewById(R.id.btnBack);
        btnAddBudget = findViewById(R.id.btnAddBudget);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnLoadBudget = findViewById(R.id.btnLoadBudget);

        // Setup RecyclerView
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        budgetList = new ArrayList<>();
        adapter = new BudgetAdapter(this, budgetList, username, this);
        recyclerViewBudgets.setAdapter(adapter);

        // Setup Spinners
        setupMonthYearSpinners();

        // Set current month/year
        Calendar calendar = Calendar.getInstance();
        selectedMonth = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        selectedYear = String.valueOf(calendar.get(Calendar.YEAR));
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));

        // Load data
        loadBudgets();

        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý nút Add Budget
        btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BudgetSettingActivity.this, AddBudgetActivity.class);
                intent.putExtra("month", selectedMonth);
                intent.putExtra("year", selectedYear);
                startActivity(intent);
            }
        });

        // Xử lý nút Load
        btnLoadBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth = String.format("%02d", spinnerMonth.getSelectedItemPosition() + 1);
                selectedYear = spinnerYear.getSelectedItem().toString();
                loadBudgets();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgets();
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
        for (int i = currentYear - 1; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(1); // Select current year
    }

    private void loadBudgets() {
        budgetList = databaseHelper.getAllBudgets(username);

        // Filter by month and year
        List<Budget> filteredList = new ArrayList<>();
        for (Budget budget : budgetList) {
            if (budget.getMonth().equals(selectedMonth) && budget.getYear().equals(selectedYear)) {
                filteredList.add(budget);
            }
        }

        adapter.updateList(filteredList);
        updateSummary(filteredList);

        if (filteredList.isEmpty()) {
            recyclerViewBudgets.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewBudgets.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void updateSummary(List<Budget> filteredList) {
        double totalBudget = 0;
        double totalSpent = 0;

        for (Budget budget : filteredList) {
            totalBudget += budget.getBudgetAmount();
            double spent = databaseHelper.getTotalExpenseByCategoryAndMonth(
                    username,
                    budget.getCategory(),
                    budget.getMonth(),
                    budget.getYear()
            );
            totalSpent += spent;
        }

        double remaining = totalBudget - totalSpent;

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalBudget.setText(formatter.format(totalBudget) + " VNĐ");
        tvTotalSpent.setText(formatter.format(totalSpent) + " VNĐ");
        tvRemaining.setText(formatter.format(remaining) + " VNĐ");

        // Change color based on status
        if (remaining < 0) {
            tvRemaining.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (totalBudget > 0 && (totalSpent / totalBudget) >= 0.8) {
            tvRemaining.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvRemaining.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }

        // Check for over-budget warning
        checkBudgetWarnings(filteredList);
    }

    private void checkBudgetWarnings(List<Budget> filteredList) {
        for (Budget budget : filteredList) {
            double spent = databaseHelper.getTotalExpenseByCategoryAndMonth(
                    username,
                    budget.getCategory(),
                    budget.getMonth(),
                    budget.getYear()
            );

            double percentage = (spent / budget.getBudgetAmount()) * 100;

            if (percentage >= 100) {
                showWarningDialog("⚠️ Vượt ngân sách!",
                        "Bạn đã vượt ngân sách cho danh mục \"" + budget.getCategory() + "\"!");
                break;
            } else if (percentage >= 80) {
                showWarningDialog("⚠️ Cảnh báo!",
                        "Bạn đã sử dụng " + String.format("%.0f%%", percentage) +
                                " ngân sách cho danh mục \"" + budget.getCategory() + "\"!");
                break;
            }
        }
    }

    private void showWarningDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onEditClick(Budget budget) {
        Intent intent = new Intent(this, EditBudgetActivity.class);
        intent.putExtra("budget", budget);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final Budget budget) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ngân sách")
                .setMessage("Bạn có chắc muốn xóa ngân sách này?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseHelper.deleteBudget(budget.getId());
                        Toast.makeText(BudgetSettingActivity.this, "Đã xóa ngân sách", Toast.LENGTH_SHORT).show();
                        loadBudgets();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}