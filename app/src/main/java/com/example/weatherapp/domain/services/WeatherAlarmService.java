package com.example.weatherapp.domain.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.weatherapp.domain.model.AlarmCondition;
import com.example.weatherapp.domain.model.AlarmType;
import com.example.weatherapp.domain.model.WeatherAlarm;
import com.example.weatherapp.domain.model.WeatherData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Weather Alarm Service
 * Manages weather alarms and checks conditions
 */
public class WeatherAlarmService {
    private static final String TAG = "WeatherAlarmService";
    private static final String PREFS_NAME = "WeatherAlarms";
    private static final String KEY_ALARMS = "alarms";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public WeatherAlarmService(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Save an alarm
     */
    public void saveAlarm(WeatherAlarm alarm) {
        List<WeatherAlarm> alarms = getAllAlarms();
        
        // Check if alarm exists (update)
        boolean found = false;
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).getId().equals(alarm.getId())) {
                alarms.set(i, alarm);
                found = true;
                break;
            }
        }
        
        // Add new alarm if not found
        if (!found) {
            alarms.add(alarm);
        }
        
        // Save to SharedPreferences
        String json = gson.toJson(alarms);
        prefs.edit().putString(KEY_ALARMS, json).apply();
        
        Log.d(TAG, "Alarm saved: " + alarm.getTitle());
    }
    
    /**
     * Delete an alarm
     */
    public void deleteAlarm(String alarmId) {
        List<WeatherAlarm> alarms = getAllAlarms();
        
        // Remove alarm with matching ID
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).getId().equals(alarmId)) {
                alarms.remove(i);
                break;
            }
        }
        
        String json = gson.toJson(alarms);
        prefs.edit().putString(KEY_ALARMS, json).apply();
        
        Log.d(TAG, "Alarm deleted: " + alarmId);
    }
    
    /**
     * Get all alarms
     */
    public List<WeatherAlarm> getAllAlarms() {
        String json = prefs.getString(KEY_ALARMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<ArrayList<WeatherAlarm>>(){}.getType();
        List<WeatherAlarm> alarms = gson.fromJson(json, listType);
        return alarms != null ? alarms : new ArrayList<>();
    }
    
    /**
     * Get all enabled alarms
     */
    public List<WeatherAlarm> getEnabledAlarms() {
        List<WeatherAlarm> allAlarms = getAllAlarms();
        List<WeatherAlarm> enabledAlarms = new ArrayList<>();
        
        for (WeatherAlarm alarm : allAlarms) {
            if (alarm.isEnabled()) {
                enabledAlarms.add(alarm);
            }
        }
        
        return enabledAlarms;
    }
    
    /**
     * Get alarms that should trigger now
     */
    public List<WeatherAlarm> getAlarmsDueNow() {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        
        List<WeatherAlarm> dueAlarms = new ArrayList<>();
        List<WeatherAlarm> enabledAlarms = getEnabledAlarms();
        
        for (WeatherAlarm alarm : enabledAlarms) {
            // Check if alarm time matches (within 1 minute window)
            if (alarm.getHourOfDay() == currentHour && 
                Math.abs(alarm.getMinute() - currentMinute) <= 1 &&
                alarm.shouldRunToday()) {
                dueAlarms.add(alarm);
            }
        }
        
        return dueAlarms;
    }
    
    /**
     * Check if alarm conditions are met
     */
    public boolean shouldTriggerAlarm(WeatherAlarm alarm, WeatherData weatherData) {
        if (weatherData == null) {
            Log.w(TAG, "No weather data available for alarm check");
            return false;
        }
        
        AlarmCondition condition = alarm.getCondition();
        AlarmType type = alarm.getType();
        
        switch (type) {
            case WAKE_UP_EARLY:
                return shouldWakeUpEarly(condition, weatherData);
                
            case UMBRELLA_REMINDER:
                return shouldBringUmbrella(weatherData);
                
            case UV_ALERT:
                return shouldAlertUV(condition, weatherData);
                
            case AIR_QUALITY_ALERT:
                return shouldAlertAirQuality(condition, weatherData);
                
            case ICY_ROADS_ALERT:
                return shouldAlertIcyRoads(condition, weatherData);
                
            case TEMPERATURE_ALERT:
                return shouldAlertTemperature(condition, weatherData);
                
            default:
                return false;
        }
    }
    
    /**
     * Check if should wake up early
     */
    private boolean shouldWakeUpEarly(AlarmCondition condition, WeatherData weatherData) {
        return condition.matchesWeatherData(weatherData);
    }
    
    /**
     * Check if should bring umbrella
     */
    private boolean shouldBringUmbrella(WeatherData weatherData) {
        String condition = weatherData.getCondition().toLowerCase();
        return condition.contains("rain") || condition.contains("drizzle") || 
               condition.contains("shower") || weatherData.getRainVolume() > 0;
    }
    
    /**
     * Check if UV index is high
     */
    private boolean shouldAlertUV(AlarmCondition condition, WeatherData weatherData) {
        // UV index from 0-11+ scale
        double uvIndex = weatherData.getUvIndex();
        return uvIndex >= condition.getUvThreshold();
    }
    
    /**
     * Check if air quality is poor
     */
    private boolean shouldAlertAirQuality(AlarmCondition condition, WeatherData weatherData) {
        // AQI scale: 0-50 Good, 51-100 Moderate, 101-150 Unhealthy for sensitive, 151-200 Unhealthy, 201+ Very Unhealthy
        double aqi = weatherData.getAqi();
        return aqi > condition.getAqiThreshold();
    }
    
    /**
     * Check if roads may be icy
     */
    private boolean shouldAlertIcyRoads(AlarmCondition condition, WeatherData weatherData) {
        double temp = weatherData.getTemperature();
        String cond = weatherData.getCondition().toLowerCase();
        
        // Icy roads likely if temp is at or below freezing and there's precipitation
        return temp <= condition.getTemperatureThreshold() && 
               (cond.contains("rain") || cond.contains("snow") || cond.contains("sleet"));
    }
    
    /**
     * Check temperature threshold
     */
    private boolean shouldAlertTemperature(AlarmCondition condition, WeatherData weatherData) {
        double temp = weatherData.getTemperature();
        double threshold = condition.getTemperatureThreshold();
        
        switch (condition.getComparisonOperator()) {
            case GREATER_THAN:
                return temp > threshold;
            case LESS_THAN:
                return temp < threshold;
            case EQUAL_TO:
                return Math.abs(temp - threshold) < 1;
            case GREATER_THAN_OR_EQUAL:
                return temp >= threshold;
            case LESS_THAN_OR_EQUAL:
                return temp <= threshold;
            default:
                return false;
        }
    }
    
    /**
     * Get notification message for alarm
     */
    public String getNotificationMessage(WeatherAlarm alarm, WeatherData weatherData) {
        String emoji = alarm.getType().getIcon();
        
        switch (alarm.getType()) {
            case WAKE_UP_EARLY:
                return emoji + " Time to wake up! " + weatherData.getCondition() + " today. " +
                       "Temperature: " + Math.round(weatherData.getTemperature()) + "°C";
                
            case UMBRELLA_REMINDER:
                return emoji + " Don't forget your umbrella! Rain expected today. " +
                       weatherData.getRainVolume() + "mm rainfall.";
                
            case UV_ALERT:
                return emoji + " High UV Index Alert! UV: " + weatherData.getUvIndex() + 
                       ". Wear sunscreen and protective clothing.";
                
            case AIR_QUALITY_ALERT:
                return emoji + " Poor Air Quality Alert! AQI: " + Math.round(weatherData.getAqi()) + 
                       ". Limit outdoor activities.";
                
            case ICY_ROADS_ALERT:
                return emoji + " Icy Roads Warning! Temperature: " + Math.round(weatherData.getTemperature()) + 
                       "°C. Drive carefully.";
                
            case TEMPERATURE_ALERT:
                return emoji + " Temperature Alert! Current: " + Math.round(weatherData.getTemperature()) + 
                       "°C. " + weatherData.getCondition();
                
            default:
                return emoji + " Weather Alarm: " + alarm.getTitle();
        }
    }
    
    /**
     * Get notification title for alarm
     */
    public String getNotificationTitle(WeatherAlarm alarm) {
        if (alarm.getTitle() != null && !alarm.getTitle().isEmpty()) {
            return alarm.getTitle();
        }
        return alarm.getType().getDisplayName();
    }
    
    /**
     * Toggle alarm enabled/disabled
     */
    public void toggleAlarm(String alarmId) {
        List<WeatherAlarm> alarms = getAllAlarms();
        for (WeatherAlarm alarm : alarms) {
            if (alarm.getId().equals(alarmId)) {
                alarm.setEnabled(!alarm.isEnabled());
                saveAlarm(alarm);
                Log.d(TAG, "Alarm toggled: " + alarm.getTitle() + " - " + alarm.isEnabled());
                break;
            }
        }
    }
    
    /**
     * Get alarm by ID
     */
    public WeatherAlarm getAlarmById(String alarmId) {
        List<WeatherAlarm> alarms = getAllAlarms();
        for (WeatherAlarm alarm : alarms) {
            if (alarm.getId().equals(alarmId)) {
                return alarm;
            }
        }
        return null;
    }
}
