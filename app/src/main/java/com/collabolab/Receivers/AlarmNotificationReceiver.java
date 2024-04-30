package com.collabolab.Receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.collabolab.R;

public class AlarmNotificationReceiver extends BroadcastReceiver {
    public static final String notificationChannelId = "taskReminder";
    public static final String notificationChannelName = "Task Reminder";
    public static final String notificationChannelDescription = "Task Reminder Notification Channel To Remind You To Do Your Tasks";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmNotificationReceiver", "onReceive: " + intent.getAction());
        if (intent.getAction().startsWith("taskReminderAction")) {
            Log.d("AlarmNotificationReceiver", "taskReminderAction");
            Bundle extras = intent.getExtras();
            String taskName = extras.getString("taskName");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannelId)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("Task Reminder")
                    .setContentText("Don't forget to do your task: " + taskName)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.notify(0, builder.build());
            Log.d("AlarmNotificationReceiver", "notification created");
        }
    }
}