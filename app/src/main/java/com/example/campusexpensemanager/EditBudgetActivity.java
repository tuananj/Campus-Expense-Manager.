package com.example.campusexpensemanager;

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

public class EditBudgetActivity extends AppCompatActivity {

    private EditText etBudgetAmount;
    private Spinner spinnerCategory, spinnerMonth, spinnerYear;
    private Button btnSaveBudget, btnCancel;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;
    private Budget budget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        // Khởi tạo
        databaseHelper = new DatabaseHelper(this);

        // Get budget from intent
        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ngân sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        // Load budget data
        loadBudgetData();

        // Change button text
        btnSaveBudget.setText("💾 Cập nhật ngân sách");

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
                updateBudget();
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

        // Year spinner
        List<String> years = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int i = currentYear - 1; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void loadBudgetData() {
        // Set amount
        etBudgetAmount.setText(String.valueOf(budget.getBudgetAmount()));

        // Set category
        ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            if (categoryAdapter.getItem(i).equals(budget.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Set month
        spinnerMonth.setSelection(Integer.parseInt(budget.getMonth()) - 1);

        // Set year
        ArrayAdapter<String> yearAdapter = (ArrayAdapter<String>) spinnerYear.getAdapter();
        for (int i = 0; i < yearAdapter.getCount(); i++) {
            if (yearAdapter.getItem(i).equals(budget.getYear())) {
                spinnerYear.setSelection(i);
                break;
            }
        }
    }

    private void updateBudget() {
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

        // Update budget object
        budget.setCategory(category);
        budget.setBudgetAmount(amount);
        budget.setMonth(month);
        budget.setYear(year);

        // Update in database
        int result = databaseHelper.updateBudget(budget);

        if (result > 0) {
            Toast.makeText(this, "Đã cập nhật ngân sách!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi cập nhật ngân sách!", Toast.LENGTH_SHORT).show();
        }
    }
}