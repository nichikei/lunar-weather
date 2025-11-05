package com.example.weatherapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class WeatherNotificationManager {

    private static final String CHANNEL_ID = "weather_alerts";
    private static final String CHANNEL_NAME = "Weather Alerts";
    private static final String CHANNEL_DESC = "Notifications for weather updates and alerts";
    private static final int NOTIFICATION_ID = 1001;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public WeatherNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showWeatherNotification(String cityName, String temperature, String condition, String weatherIcon) {
        // Intent to open MainActivity when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Refresh action
        Intent refreshIntent = new Intent(context, NotificationReceiver.class);
        refreshIntent.setAction("com.example.weatherapp.ACTION_REFRESH_WEATHER");
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(cityName + " Weather")
                .setContentText(temperature + " • " + condition)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Current temperature: " + temperature + "\n" +
                                "Condition: " + condition + "\n" +
                                "Tap to see more details"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher_foreground, "Refresh", refreshPendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500});

        // Show notification
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void showWeatherAlert(String title, String message, String alertType) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500, 200, 500});

        try {
            notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }
}

