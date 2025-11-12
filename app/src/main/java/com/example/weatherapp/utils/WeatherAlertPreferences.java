package com.example.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manager for weather alert preferences
 */
public class WeatherAlertPreferences {
    private static final String PREFS_NAME = "weather_alert_settings";
    
    // Preference keys
    private static final String KEY_ALERTS_ENABLED = "alerts_enabled";
    private static final String KEY_RAIN_ALERTS = "rain_alerts";
    private static final String KEY_UV_ALERTS = "uv_alerts";
    private static final String KEY_AIR_QUALITY_ALERTS = "air_quality_alerts";
    private static final String KEY_WEATHER_CHANGE_ALERTS = "weather_change_alerts";
    private static final String KEY_TEMPERATURE_ALERTS = "temperature_alerts";
    private static final String KEY_STORM_ALERTS = "storm_alerts";
    private static final String KEY_ALERT_FREQUENCY = "alert_frequency"; // minutes
    
    private final SharedPreferences prefs;
    
    public WeatherAlertPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Master switch
    public boolean areAlertsEnabled() {
        return prefs.getBoolean(KEY_ALERTS_ENABLED, true);
    }
    
    public void setAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERTS_ENABLED, enabled).apply();
    }
    
    // Rain alerts
    public boolean areRainAlertsEnabled() {
        return prefs.getBoolean(KEY_RAIN_ALERTS, true);
    }
    
    public void setRainAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_RAIN_ALERTS, enabled).apply();
    }
    
    // UV alerts
    public boolean areUVAlertsEnabled() {
        return prefs.getBoolean(KEY_UV_ALERTS, true);
    }
    
    public void setUVAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_UV_ALERTS, enabled).apply();
    }
    
    // Air quality alerts
    public boolean areAirQualityAlertsEnabled() {
        return prefs.getBoolean(KEY_AIR_QUALITY_ALERTS, true);
    }
    
    public void setAirQualityAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AIR_QUALITY_ALERTS, enabled).apply();
    }
    
    // Weather change alerts
    public boolean areWeatherChangeAlertsEnabled() {
        return prefs.getBoolean(KEY_WEATHER_CHANGE_ALERTS, true);
    }
    
    public void setWeatherChangeAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WEATHER_CHANGE_ALERTS, enabled).apply();
    }
    
    // Temperature alerts
    public boolean areTemperatureAlertsEnabled() {
        return prefs.getBoolean(KEY_TEMPERATURE_ALERTS, false);
    }
    
    public void setTemperatureAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TEMPERATURE_ALERTS, enabled).apply();
    }
    
    // Storm alerts
    public boolean areStormAlertsEnabled() {
        return prefs.getBoolean(KEY_STORM_ALERTS, true);
    }
    
    public void setStormAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_STORM_ALERTS, enabled).apply();
    }
    
    // Alert frequency (in minutes)
    public int getAlertFrequency() {
        return prefs.getInt(KEY_ALERT_FREQUENCY, 30); // Default 30 minutes
    }
    
    public void setAlertFrequency(int minutes) {
        prefs.edit().putInt(KEY_ALERT_FREQUENCY, minutes).apply();
    }
}
