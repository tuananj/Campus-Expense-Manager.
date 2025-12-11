package com.example.campusexpensemanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "expense_notifications";
    private static final String CHANNEL_NAME = "Thông báo Chi tiêu";
    private static final String CHANNEL_DESCRIPTION = "Thông báo về ngân sách và chi tiêu";

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    // Tạo Notification Channel (bắt buộc từ Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Gửi thông báo cảnh báo ngân sách
    public void sendBudgetWarningNotification(String category, double percentage, double spent, double budget) {
        String title;
        String message;
        int notificationId = category.hashCode();

        if (percentage >= 100) {
            title = "⚠️ VƯỢT NGÂN SÁCH!";
            message = "Bạn đã VƯỢT ngân sách cho \"" + category + "\"!\n" +
                    "Chi tiêu: " + formatMoney(spent) + " / " + formatMoney(budget);
        } else if (percentage >= 90) {
            title = "🚨 Sắp vượt ngân sách!";
            message = "Bạn đã sử dụng " + String.format("%.0f%%", percentage) + " ngân sách cho \"" + category + "\"!\n" +
                    "Còn lại: " + formatMoney(budget - spent);
        } else if (percentage >= 80) {
            title = "⚠️ Cảnh báo ngân sách";
            message = "Bạn đã sử dụng " + String.format("%.0f%%", percentage) + " ngân sách cho \"" + category + "\".";
        } else {
            return; // Không gửi thông báo nếu dưới 80%
        }

        // Intent khi click vào notification
        Intent intent = new Intent(context, BudgetSettingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{100, 200, 300, 400, 500})
                .setLights(Color.RED, 1000, 1000);

        // Gửi notification
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Gửi thông báo tổng hợp hàng ngày
    public void sendDailySummaryNotification(double totalSpent, int expenseCount) {
        String title = "📊 Tóm tắt chi tiêu hôm nay";
        String message = "Bạn đã chi " + formatMoney(totalSpent) + " cho " + expenseCount + " giao dịch hôm nay.";

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            notificationManager.notify(1001, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Gửi thông báo nhắc nhở thêm chi tiêu
    public void sendReminderNotification() {
        String title = "💭 Nhắc nhở";
        String message = "Đừng quên ghi lại các chi tiêu hôm nay nhé!";

        Intent intent = new Intent(context, AddExpenseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            notificationManager.notify(1002, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Format tiền
    private String formatMoney(double amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(amount) + " VNĐ";
    }

    // Hủy tất cả thông báo
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    // Hủy thông báo theo ID
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
}