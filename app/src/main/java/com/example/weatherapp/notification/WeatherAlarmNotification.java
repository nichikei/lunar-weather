package com.example.weatherapp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.AlarmType;
import com.example.weatherapp.ui.activities.MainActivity;

/**
 * Weather Alarm Notification
 * Handles creating and showing alarm notifications
 */
public class WeatherAlarmNotification {
    private static final String CHANNEL_ID = "weather_alarms";
    private static final String CHANNEL_NAME = "Weather Alarms";
    private static final String CHANNEL_DESCRIPTION = "Notifications for weather-based alarms";
    
    /**
     * Show alarm notification
     */
    public static void showNotification(Context context, int notificationId, 
                                       String title, String message, AlarmType alarmType) {
        // Create notification channel (API 26+)
        createNotificationChannel(context);
        
        // Create intent to open app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE
        );
        
        // Get alarm sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(getIconForAlarmType(alarmType))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 500, 200, 500}) // Vibration pattern
                .setLights(0xFF3B82F6, 1000, 1000); // Blue light
        
        // Add action buttons
        builder.addAction(R.drawable.ic_check, "OK", pendingIntent);
        
        // Show notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.enableLights(true);
            channel.setLightColor(0xFF3B82F6); // Blue
            channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
            );
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Get icon resource for alarm type
     */
    private static int getIconForAlarmType(AlarmType alarmType) {
        switch (alarmType) {
            case WAKE_UP_EARLY:
                return R.drawable.ic_alarm;
            case UMBRELLA_REMINDER:
                return R.drawable.ic_umbrella;
            case UV_ALERT:
                return R.drawable.ic_sun;
            case AIR_QUALITY_ALERT:
                return R.drawable.ic_wind;
            case ICY_ROADS_ALERT:
                return R.drawable.ic_car;
            case TEMPERATURE_ALERT:
                return R.drawable.ic_thermometer;
            default:
                return R.drawable.ic_alarm; // Default to alarm icon
        }
    }
    
    /**
     * Cancel notification
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }
}
