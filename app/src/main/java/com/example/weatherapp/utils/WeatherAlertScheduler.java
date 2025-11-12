package com.example.weatherapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weatherapp.notification.SmartWeatherAlertWorker;

import java.util.concurrent.TimeUnit;

/**
 * Scheduler for weather alert checks
 */
public class WeatherAlertScheduler {
    private static final String TAG = "WeatherAlertScheduler";
    private static final String WORK_NAME = "smart_weather_alerts";
    
    /**
     * Schedule periodic weather alert checks
     * @param context Application context
     * @param intervalMinutes Check interval in minutes (default: 30)
     */
    public static void scheduleWeatherAlerts(Context context, int intervalMinutes) {
        Log.d(TAG, "Scheduling weather alerts every " + intervalMinutes + " minutes");
        
        // Constraints: require network connection
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        // Create periodic work request
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                SmartWeatherAlertWorker.class,
                intervalMinutes,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag("weather_alerts")
                .build();
        
        // Schedule work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
        
        Log.d(TAG, "Weather alerts scheduled successfully");
    }
    
    /**
     * Cancel scheduled weather alerts
     */
    public static void cancelWeatherAlerts(Context context) {
        Log.d(TAG, "Canceling weather alerts");
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
    
    /**
     * Check if weather alerts are scheduled
     */
    public static boolean areAlertsScheduled(Context context) {
        // You can implement logic to check work status if needed
        return true;
    }
}
