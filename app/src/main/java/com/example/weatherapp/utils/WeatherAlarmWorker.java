package com.example.weatherapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weatherapp.domain.model.WeatherAlarm;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.services.WeatherAlarmService;
import com.example.weatherapp.notification.WeatherAlarmNotification;

import java.util.List;

/**
 * Weather Alarm Worker
 * Background worker that checks weather conditions and triggers alarms
 */
public class WeatherAlarmWorker extends Worker {
    private static final String TAG = "WeatherAlarmWorker";
    
    public WeatherAlarmWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Weather alarm check started");
        
        try {
            Context context = getApplicationContext();
            WeatherAlarmService alarmService = new WeatherAlarmService(context);
            
            // Get all enabled alarms
            List<WeatherAlarm> enabledAlarms = alarmService.getEnabledAlarms();
            Log.d(TAG, "Checking " + enabledAlarms.size() + " enabled alarms");
            
            // Get current weather data from cache/API
            WeatherData weatherData = getCurrentWeatherData(context);
            
            if (weatherData == null) {
                Log.w(TAG, "No weather data available");
                return Result.retry();
            }
            
            // Check each alarm
            for (WeatherAlarm alarm : enabledAlarms) {
                if (alarmService.shouldTriggerAlarm(alarm, weatherData)) {
                    // Trigger notification
                    String title = alarmService.getNotificationTitle(alarm);
                    String message = alarmService.getNotificationMessage(alarm, weatherData);
                    
                    WeatherAlarmNotification.showNotification(
                            context,
                            alarm.getId().hashCode(),
                            title,
                            message,
                            alarm.getType()
                    );
                    
                    Log.d(TAG, "Alarm triggered: " + alarm.getTitle());
                }
            }
            
            Log.d(TAG, "Weather alarm check completed successfully");
            return Result.success();
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking weather alarms", e);
            return Result.retry();
        }
    }
    
    /**
     * Get current weather data from SharedPreferences cache
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
            Log.e(TAG, "Error loading weather data from cache", e);
        }
        return null;
    }
}
