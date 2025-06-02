package com.example.scheduleflow;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class TaskNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // For API level 33 (Android 13) and above, check if POST_NOTIFICATIONS permission is granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, safely exit without posting the notification.
                return;
            }
        }

        String taskTitle = intent.getStringExtra("task_title");

        // Create notification channel for Android O and later.
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TASK_CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(taskTitle != null ? taskTitle : "Task Reminder")
                .setContentText("It's time to complete your task!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Use current time as a unique notification ID
        int notificationId = (int) System.currentTimeMillis();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Wrap the notify call in a try/catch block to handle potential SecurityException.
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
            // Optionally, you could log or handle the error in a user-friendly way.
        }
    }

    private void createNotificationChannel(Context context) {
        // Notification channels are required for API level 26 and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Reminders for scheduled tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("TASK_CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
