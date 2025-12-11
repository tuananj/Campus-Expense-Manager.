package com.example.campusexpensemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddBudgetActivity extends AppCompatActivity {

    private EditText etBudgetAmount;
    private Spinner spinnerCategory, spinnerMonth, spinnerYear;
    private Button btnSaveBudget, btnCancel;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        // Khởi tạo
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CampusExpensePrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        // Ánh xạ views
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        // Setup Spinners
        setupCategorySpinner();
        setupMonthYearSpinners();

        // Get month/year from intent (if passed)
        String month = getIntent().getStringExtra("month");
        String year = getIntent().getStringExtra("year");
        if (month != null && year != null) {
            spinnerMonth.setSelection(Integer.parseInt(month) - 1);
            // Set year selection
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerYear.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(year)) {
                    spinnerYear.setSelection(i);
                    break;
                }
            }
        }

        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý nút Save
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBudget();
            }
        });

        // Xử lý nút Cancel
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Ăn uống",
                "Học tập",
                "Đi lại",
                "Giải trí",
                "Mua sắm",
                "Sức khỏe",
                "Nhà ở",
                "Điện thoại/Internet",
                "Khác"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
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

        // Set current month
        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));

        // Year spinner
        List<String> years = new ArrayList<>();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int i = currentYear - 1; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(1); // Select current year
    }

    private void saveBudget() {
        String amountStr = etBudgetAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String month = String.format("%02d", spinnerMonth.getSelectedItemPosition() + 1);
        String year = spinnerYear.getSelectedItem().toString();

        // Validate
        if (amountStr.isEmpty()) {
            etBudgetAmount.setError("Vui lòng nhập số tiền");
            etBudgetAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etBudgetAmount.setError("Số tiền phải lớn hơn 0");
                etBudgetAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Số tiền không hợp lệ");
            etBudgetAmount.requestFocus();
            return;
        }

        // Check if budget already exists for this category and month
        Budget existingBudget = databaseHelper.getBudgetByCategoryAndMonth(username, category, month, year);
        if (existingBudget != null) {
            Toast.makeText(this, "Ngân sách cho danh mục này đã tồn tại!\nVui lòng chỉnh sửa thay vì tạo mới.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create Budget object
        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setBudgetAmount(amount);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setUsername(username);

        // Save to database
        long result = databaseHelper.addBudget(budget);

        if (result != -1) {
            Toast.makeText(this, "Đã lưu ngân sách thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu ngân sách!", Toast.LENGTH_SHORT).show();
        }
    }
}