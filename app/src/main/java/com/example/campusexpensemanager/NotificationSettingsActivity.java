package com.example.campusexpensemanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchCompat switchBudgetWarning, switchBudgetExceed, switchDailySummary, switchReminder;
    private Spinner spinnerThreshold;
    private Button btnSetTime, btnTestNotification, btnSave;
    private ImageButton btnBack;

    private SharedPreferences prefs;
    private int selectedHour = 20;
    private int selectedMinute = 0;

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Khởi tạo
        prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        // Ánh xạ views
        switchBudgetWarning = findViewById(R.id.switchBudgetWarning);
        switchBudgetExceed = findViewById(R.id.switchBudgetExceed);
        switchDailySummary = findViewById(R.id.switchDailySummary);
        switchReminder = findViewById(R.id.switchReminder);
        spinnerThreshold = findViewById(R.id.spinnerThreshold);
        btnSetTime = findViewById(R.id.btnSetTime);
        btnTestNotification = findViewById(R.id.btnTestNotification);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Setup threshold spinner
        setupThresholdSpinner();

        // Load saved settings
        loadSettings();

        // Request notification permission (Android 13+)
        requestNotificationPermission();

        // Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý chọn thời gian
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Xử lý test notification
        btnTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testNotification();
            }
        });

        // Xử lý lưu cài đặt
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void setupThresholdSpinner() {
        String[] thresholds = {"70%", "75%", "80%", "85%", "90%", "95%"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, thresholds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThreshold.setAdapter(adapter);
        spinnerThreshold.setSelection(2); // Default 80%
    }

    private void loadSettings() {
        // Load saved preferences
        switchBudgetWarning.setChecked(prefs.getBoolean("budget_warning", true));
        switchBudgetExceed.setChecked(prefs.getBoolean("budget_exceed", true));
        switchDailySummary.setChecked(prefs.getBoolean("daily_summary", false));
        switchReminder.setChecked(prefs.getBoolean("reminder", false));

        int threshold = prefs.getInt("threshold", 80);
        spinnerThreshold.setSelection((threshold - 70) / 5); // Map to spinner position

        selectedHour = prefs.getInt("summary_hour", 20);
        selectedMinute = prefs.getInt("summary_minute", 0);
        btnSetTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();

        // Save switch states
        editor.putBoolean("budget_warning", switchBudgetWarning.isChecked());
        editor.putBoolean("budget_exceed", switchBudgetExceed.isChecked());
        editor.putBoolean("daily_summary", switchDailySummary.isChecked());
        editor.putBoolean("reminder", switchReminder.isChecked());

        // Save threshold
        String thresholdStr = spinnerThreshold.getSelectedItem().toString().replace("%", "");
        editor.putInt("threshold", Integer.parseInt(thresholdStr));

        // Save time
        editor.putInt("summary_hour", selectedHour);
        editor.putInt("summary_minute", selectedMinute);

        editor.apply();

        // Schedule notifications
        scheduleDailyNotifications();

        Toast.makeText(this, "Đã lưu cài đặt thông báo!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        btnSetTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    }
                },
                selectedHour,
                selectedMinute,
                true
        );
        timePickerDialog.show();
    }

    private void scheduleDailyNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        // Schedule daily summary
        if (switchDailySummary.isChecked()) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.setAction("DAILY_SUMMARY");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    1001,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            calendar.set(Calendar.SECOND, 0);

            // If time has passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Schedule repeating alarm
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        } else {
            // Cancel daily summary
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.setAction("DAILY_SUMMARY");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    1001,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
        }

        // Schedule reminder
        if (switchReminder.isChecked()) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.setAction("REMINDER");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    1002,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 18); // 6 PM
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        } else {
            // Cancel reminder
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.setAction("REMINDER");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    1002,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
        }
    }

    private void testNotification() {
        NotificationHelper notificationHelper = new NotificationHelper(this);

        // Test budget warning notification
        notificationHelper.sendBudgetWarningNotification("Ăn uống", 85.5, 1710000, 2000000);

        Toast.makeText(this, "Đã gửi thông báo thử nghiệm!", Toast.LENGTH_SHORT).show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã bật thông báo!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền thông báo để sử dụng tính năng này", Toast.LENGTH_LONG).show();
            }
        }
    }
}