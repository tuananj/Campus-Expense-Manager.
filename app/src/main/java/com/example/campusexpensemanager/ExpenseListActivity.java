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
import java.util.List;
import java.util.Locale;

public class ExpenseListActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {

    private RecyclerView recyclerViewExpenses;
    private TextView tvTotalAmount, tvExpenseCount;
    private LinearLayout layoutEmpty;
    private ImageButton btnBack;
    private Spinner spinnerFilterCategory;
    private Button btnFilter;

    private ExpenseAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private List<Expense> expenseList;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        // Khởi tạo
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CampusExpensePrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        // Ánh xạ views
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvExpenseCount = findViewById(R.id.tvExpenseCount);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnBack = findViewById(R.id.btnBack);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        btnFilter = findViewById(R.id.btnFilter);

        // Setup RecyclerView
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(this, expenseList, this);
        recyclerViewExpenses.setAdapter(adapter);

        // Setup filter spinner
        setupFilterSpinner();

        // Load data
        loadExpenses();

        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý filter
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterExpenses();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(); // Reload khi quay lại từ màn hình khác
    }

    private void setupFilterSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Tất cả");
        categories.add("Ăn uống");
        categories.add("Học tập");
        categories.add("Đi lại");
        categories.add("Giải trí");
        categories.add("Mua sắm");
        categories.add("Sức khỏe");
        categories.add("Nhà ở");
        categories.add("Điện thoại/Internet");
        categories.add("Khác");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(adapter);
    }

    private void loadExpenses() {
        expenseList = databaseHelper.getAllExpenses(username);
        adapter.updateList(expenseList);
        updateSummary();

        if (expenseList.isEmpty()) {
            recyclerViewExpenses.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewExpenses.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void filterExpenses() {
        String selectedCategory = spinnerFilterCategory.getSelectedItem().toString();

        if (selectedCategory.equals("Tất cả")) {
            loadExpenses();
        } else {
            expenseList = databaseHelper.getExpensesByCategory(username, selectedCategory);
            adapter.updateList(expenseList);
            updateSummary();

            if (expenseList.isEmpty()) {
                Toast.makeText(this, "Không có chi tiêu nào trong danh mục này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSummary() {
        double total = 0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(formatter.format(total) + " VNĐ");
        tvExpenseCount.setText(expenseList.size() + " giao dịch");
    }

    @Override
    public void onEditClick(Expense expense) {
        Intent intent = new Intent(this, EditExpenseActivity.class);
        intent.putExtra("expense", expense);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa chi tiêu")
                .setMessage("Bạn có chắc muốn xóa chi tiêu này?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseHelper.deleteExpense(expense.getId());
                        Toast.makeText(ExpenseListActivity.this, "Đã xóa chi tiêu", Toast.LENGTH_SHORT).show();
                        loadExpenses();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}