package com.example.weatherapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weatherapp.domain.model.WeatherAlarm;
import com.example.weatherapp.notification.WeatherAlarmReceiver;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Alarm Scheduler
 * Handles scheduling and canceling of weather alarms using AlarmManager
 */
public class AlarmScheduler {
    private static final String TAG = "AlarmScheduler";
    private static final String WORK_NAME_PREFIX = "weather_alarm_check_";
    
    private final Context context;
    private final AlarmManager alarmManager;
    
    public AlarmScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    /**
     * Check if app can schedule exact alarms (Android 12+)
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true; // Older versions don't need permission
    }
    
    /**
     * Schedule an alarm
     */
    public void scheduleAlarm(WeatherAlarm alarm) {
        if (!alarm.isEnabled()) {
            Log.d(TAG, "Alarm is disabled, not scheduling: " + alarm.getTitle());
            return;
        }
        
        // Check permission for Android 12+
        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule exact alarms - permission not granted");
            return;
        }
        
        // Create intent for alarm receiver
        Intent intent = new Intent(context, WeatherAlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("alarm_title", alarm.getTitle());
        intent.putExtra("alarm_type", alarm.getType().name());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Calculate alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHourOfDay());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // If alarm time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        // Schedule alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use setExactAndAllowWhileIdle for API 23+
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            // Use setExact for older versions
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
        
        Log.d(TAG, "Alarm scheduled: " + alarm.getTitle() + 
                " at " + alarm.getFormattedTime() + 
                " (timestamp: " + calendar.getTimeInMillis() + ")");
        
        // Also schedule background work to check weather conditions
        scheduleWeatherCheck(alarm);
    }
    
    /**
     * Cancel an alarm
     */
    public void cancelAlarm(WeatherAlarm alarm) {
        Intent intent = new Intent(context, WeatherAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId().hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Alarm cancelled: " + alarm.getTitle());
        }
        
        // Also cancel background work
        cancelWeatherCheck(alarm);
    }
    
    /**
     * Reschedule an alarm (useful after device reboot)
     */
    public void rescheduleAlarm(WeatherAlarm alarm) {
        cancelAlarm(alarm);
        scheduleAlarm(alarm);
    }
    
    /**
     * Schedule background work to check weather conditions periodically
     * This runs hourly to check if weather conditions match alarm criteria
     */
    private void scheduleWeatherCheck(WeatherAlarm alarm) {
        String workName = WORK_NAME_PREFIX + alarm.getId();
        
        // Create constraints - require network for weather API
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        // Create periodic work request - check every hour
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeatherAlarmWorker.class,
                1, TimeUnit.HOURS, // Repeat every 1 hour
                15, TimeUnit.MINUTES // Flex period
        )
                .setConstraints(constraints)
                .addTag(alarm.getId())
                .build();
        
        // Schedule work
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        workName,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequest
                );
        
        Log.d(TAG, "Background weather check scheduled for: " + alarm.getTitle());
    }
    
    /**
     * Cancel background weather check
     */
    private void cancelWeatherCheck(WeatherAlarm alarm) {
        String workName = WORK_NAME_PREFIX + alarm.getId();
        WorkManager.getInstance(context).cancelUniqueWork(workName);
        Log.d(TAG, "Background weather check cancelled for: " + alarm.getTitle());
    }
    
    /**
     * Reschedule all enabled alarms (e.g., after device reboot)
     */
    public void rescheduleAllAlarms(java.util.List<WeatherAlarm> alarms) {
        Log.d(TAG, "Rescheduling " + alarms.size() + " alarms");
        for (WeatherAlarm alarm : alarms) {
            if (alarm.isEnabled()) {
                scheduleAlarm(alarm);
            }
        }
    }
    
    /**
     * Cancel all alarms
     */
    public void cancelAllAlarms(java.util.List<WeatherAlarm> alarms) {
        Log.d(TAG, "Cancelling " + alarms.size() + " alarms");
        for (WeatherAlarm alarm : alarms) {
            cancelAlarm(alarm);
        }
    }
}
