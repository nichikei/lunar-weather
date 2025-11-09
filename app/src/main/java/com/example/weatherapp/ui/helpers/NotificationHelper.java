package com.example.weatherapp.ui.helpers;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.weatherapp.notification.WeatherNotificationWorker;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for managing notifications and permissions
 * Handles notification permissions and scheduling weather notifications
 */
public class NotificationHelper {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final ActivityResultLauncher<String> permissionLauncher;
    
    public NotificationHelper(Context context, SharedPreferences sharedPreferences,
                            ActivityResultLauncher<String> permissionLauncher) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.permissionLauncher = permissionLauncher;
    }
    
    /**
     * Check and request notification permission if needed
     */
    public void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    
    /**
     * Schedule weather notifications based on user preferences
     */
    public void scheduleWeatherNotifications() {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true);

        if (notificationsEnabled) {
            // Test: One-time notification after 2 minutes
            OneTimeWorkRequest weatherWorkRequest = new OneTimeWorkRequest.Builder(
                    WeatherNotificationWorker.class)
                    .setInitialDelay(2, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(context).enqueueUniqueWork(
                    "WeatherNotification",
                    ExistingWorkPolicy.REPLACE,
                    weatherWorkRequest
            );

            android.util.Log.d("NotificationHelper", "Weather notification scheduled for 2 minutes");
        } else {
            cancelWeatherNotifications();
        }
    }
    
    /**
     * Cancel all scheduled weather notifications
     */
    public void cancelWeatherNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork("WeatherNotification");
    }
    
    /**
     * Handle notification permission result
     */
    public void onPermissionGranted() {
        scheduleWeatherNotifications();
        Toast.makeText(context, "Weather notifications enabled", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handle notification permission denial
     */
    public void onPermissionDenied() {
        Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show();
    }
}
