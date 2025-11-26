package com.example.campusexpensemanager;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDescription, etDate;
    private Spinner spinnerCategory;
    private RadioGroup rgPaymentMethod;
    private Button btnSave, btnCancel, btnPickDate;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Khởi tạo
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CampusExpensePrefs", MODE_PRIVATE);
        calendar = Calendar.getInstance();

        // Ánh xạ views
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnBack = findViewById(R.id.btnBack);

        // Setup Spinner cho danh mục
        setupCategorySpinner();

        // Set ngày hiện tại
        updateDateDisplay();

        // Xử lý chọn ngày
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý nút Lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();
            }
        });

        // Xử lý nút Hủy
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

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        // Validate
        if (amountStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Số tiền phải lớn hơn 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Số tiền không hợp lệ");
            etAmount.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Vui lòng nhập mô tả");
            etDescription.requestFocus();
            return;
        }

        // Lấy phương thức thanh toán
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        RadioButton rbSelected = findViewById(selectedId);
        String paymentMethod = rbSelected.getText().toString();

        // Lấy username
        String username = sharedPreferences.getString("username", "");

        // Tạo đối tượng Expense
        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDescription(description);
        expense.setDate(date);
        expense.setPaymentMethod(paymentMethod);
        expense.setUsername(username);

        // Lưu vào database
        long result = databaseHelper.addExpense(expense);

        if (result != -1) {
            Toast.makeText(this, "Đã lưu chi tiêu thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu chi tiêu!", Toast.LENGTH_SHORT).show();
        }
    }
}