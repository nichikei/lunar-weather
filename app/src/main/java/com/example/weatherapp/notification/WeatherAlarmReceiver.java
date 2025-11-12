package com.example.weatherapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.weatherapp.domain.model.AlarmType;
import com.example.weatherapp.domain.model.WeatherAlarm;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.services.WeatherAlarmService;

/**
 * Weather Alarm Receiver
 * Receives alarm broadcasts and triggers notifications
 */
public class WeatherAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "WeatherAlarmReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received");
        
        String action = intent.getAction();
        
        // Handle boot completed - reschedule alarms
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "Device rebooted - rescheduling alarms");
            rescheduleAlarms(context);
            return;
        }
        
        // Handle alarm trigger
        String alarmId = intent.getStringExtra("alarm_id");
        String alarmTitle = intent.getStringExtra("alarm_title");
        String alarmTypeName = intent.getStringExtra("alarm_type");
        
        if (alarmId == null) {
            Log.w(TAG, "No alarm ID provided");
            return;
        }
        
        // Get alarm service
        WeatherAlarmService alarmService = new WeatherAlarmService(context);
        WeatherAlarm alarm = alarmService.getAlarmById(alarmId);
        
        if (alarm == null) {
            Log.w(TAG, "Alarm not found: " + alarmId);
            return;
        }
        
        if (!alarm.isEnabled()) {
            Log.d(TAG, "Alarm is disabled: " + alarmTitle);
            return;
        }
        
        // Get current weather data
        WeatherData weatherData = getCurrentWeatherData(context);
        
        if (weatherData == null) {
            Log.w(TAG, "No weather data available");
            // Show notification anyway but with generic message
            WeatherAlarmNotification.showNotification(
                    context,
                    alarm.getId().hashCode(),
                    alarmTitle,
                    "Weather alarm triggered. Check weather conditions.",
                    alarm.getType()
            );
            rescheduleAlarm(context, alarm);
            return;
        }
        
        // Check if alarm conditions are met
        boolean shouldTrigger = alarmService.shouldTriggerAlarm(alarm, weatherData);
        
        if (shouldTrigger) {
            // Show notification
            String title = alarmService.getNotificationTitle(alarm);
            String message = alarmService.getNotificationMessage(alarm, weatherData);
            
            WeatherAlarmNotification.showNotification(
                    context,
                    alarm.getId().hashCode(),
                    title,
                    message,
                    alarm.getType()
            );
            
            Log.d(TAG, "Alarm triggered: " + alarmTitle);
        } else {
            Log.d(TAG, "Alarm conditions not met: " + alarmTitle);
        }
        
        // Reschedule alarm for next occurrence
        rescheduleAlarm(context, alarm);
    }
    
    /**
     * Get current weather data from cache
     */
    private WeatherData getCurrentWeatherData(Context context) {
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE);
            String weatherJson = prefs.getString("current_weather", null);
            
            if (weatherJson != null) {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                return gson.fromJson(weatherJson, WeatherData.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading weather data", e);
        }
        return null;
    }
    
    /**
     * Reschedule a single alarm
     */
    private void rescheduleAlarm(Context context, WeatherAlarm alarm) {
        com.example.weatherapp.utils.AlarmScheduler scheduler = 
                new com.example.weatherapp.utils.AlarmScheduler(context);
        scheduler.rescheduleAlarm(alarm);
    }
    
    /**
     * Reschedule all alarms (after boot)
     */
    private void rescheduleAlarms(Context context) {
        WeatherAlarmService alarmService = new WeatherAlarmService(context);
        java.util.List<WeatherAlarm> alarms = alarmService.getEnabledAlarms();
        
        com.example.weatherapp.utils.AlarmScheduler scheduler = 
                new com.example.weatherapp.utils.AlarmScheduler(context);
        scheduler.rescheduleAllAlarms(alarms);
        
        Log.d(TAG, "Rescheduled " + alarms.size() + " alarms after boot");
    }
}
