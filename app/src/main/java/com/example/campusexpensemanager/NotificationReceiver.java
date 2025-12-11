package com.example.campusexpensemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Khởi động lại alarm sau khi reboot
                scheduleNotifications(context);
            } else if (action.equals("CHECK_BUDGET")) {
                // Kiểm tra ngân sách
                checkBudgetAndNotify(context);
            } else if (action.equals("DAILY_SUMMARY")) {
                // Gửi tóm tắt hàng ngày
                sendDailySummary(context);
            } else if (action.equals("REMINDER")) {
                // Gửi nhắc nhở
                sendReminder(context);
            }
        }
    }

    private void checkBudgetAndNotify(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("CampusExpensePrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        if (username.isEmpty()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        NotificationHelper notificationHelper = new NotificationHelper(context);

        Calendar calendar = Calendar.getInstance();
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        List<Budget> budgets = dbHelper.getAllBudgets(username);

        for (Budget budget : budgets) {
            if (budget.getMonth().equals(month) && budget.getYear().equals(year)) {
                double spent = dbHelper.getTotalExpenseByCategoryAndMonth(
                        username,
                        budget.getCategory(),
                        month,
                        year
                );

                double percentage = (spent / budget.getBudgetAmount()) * 100;

                // Gửi thông báo nếu >= 80%
                if (percentage >= 80) {
                    notificationHelper.sendBudgetWarningNotification(
                            budget.getCategory(),
                            percentage,
                            spent,
                            budget.getBudgetAmount()
                    );
                }
            }
        }
    }

    private void sendDailySummary(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("CampusExpensePrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        if (username.isEmpty()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        NotificationHelper notificationHelper = new NotificationHelper(context);

        Calendar calendar = Calendar.getInstance();
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        double totalSpent = dbHelper.getTotalExpenseByMonth(username, month, year);
        List<Expense> expenses = dbHelper.getAllExpenses(username);

        notificationHelper.sendDailySummaryNotification(totalSpent, expenses.size());
    }

    private void sendReminder(Context context) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.sendReminderNotification();
    }

    private void scheduleNotifications(Context context) {
        // Có thể thêm logic để lên lịch lại các notification sau khi reboot
    }
}