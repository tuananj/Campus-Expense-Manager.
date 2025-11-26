package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegEmail, etRegPassword, etRegConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ views
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Xử lý sự kiện đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        // Xử lý quay lại đăng nhập
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng RegisterActivity và quay về LoginActivity
            }
        });
    }

    private void handleRegister() {
        String username = etRegUsername.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // Validate input
        if (username.isEmpty()) {
            etRegUsername.setError("Vui lòng nhập tên đăng nhập");
            etRegUsername.requestFocus();
            return;
        }

        if (username.length() < 4) {
            etRegUsername.setError("Tên đăng nhập phải có ít nhất 4 ký tự");
            etRegUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etRegEmail.setError("Vui lòng nhập email");
            etRegEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegEmail.setError("Email không hợp lệ");
            etRegEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegPassword.setError("Vui lòng nhập mật khẩu");
            etRegPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etRegPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etRegConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etRegConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etRegConfirmPassword.setError("Mật khẩu không khớp");
            etRegConfirmPassword.requestFocus();
            return;
        }

        // Đăng ký thành công (Demo)
        // Trong thực tế, lưu vào database hoặc gọi API
        Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();

        // Quay về màn hình đăng nhập
        finish();
    }
}