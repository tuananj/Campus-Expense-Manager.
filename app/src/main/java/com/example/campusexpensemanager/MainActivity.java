package com.example.campusexpensemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalExpense;
    private Button btnLogout, btnAddExpense, btnViewExpenses, btnStatistics, btnSettings;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CampusExpensePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Ánh xạ views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        btnLogout = findViewById(R.id.btnLogout);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnViewExpenses = findViewById(R.id.btnViewExpenses);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnSettings = findViewById(R.id.btnSettings);

        // Hiển thị tên user
        String username = sharedPreferences.getString(KEY_USERNAME, "User");
        tvWelcome.setText("Xin chào, " + username);

        // Xử lý sự kiện Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        // Xử lý sự kiện các nút chức năng
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            }
        });

        btnViewExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpenseListActivity.class);
                startActivity(intent);
            }
        });

        btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển sang màn hình thống kê
                Toast.makeText(MainActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển sang màn hình cài đặt
                Toast.makeText(MainActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTotalExpense();
    }

    private void updateTotalExpense() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        // Lấy tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        double total = databaseHelper.getTotalExpenseByMonth(username, month, year);

        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        tvTotalExpense.setText(formatter.format(total) + " VNĐ");
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleLogout();
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void handleLogout() {
        // Xóa trạng thái đăng nhập
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USERNAME);
        editor.apply();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Quay về màn hình Login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}