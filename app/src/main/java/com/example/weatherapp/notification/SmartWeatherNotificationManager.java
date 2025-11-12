package com.example.weatherapp.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.WeatherAlert;
import com.example.weatherapp.ui.activities.MainActivity;

/**
 * Smart Weather Notification Manager
 * Handles intelligent weather alerts with different priorities and channels
 */
public class SmartWeatherNotificationManager {
    private static final String TAG = "SmartWeatherNotif";
    
    // Notification Channels
    private static final String CHANNEL_CRITICAL = "weather_critical";
    private static final String CHANNEL_WARNING = "weather_warning";
    private static final String CHANNEL_INFO = "weather_info";
    
    // Notification IDs
    private static final int NOTIF_RAIN_WARNING = 2001;
    private static final int NOTIF_UV_HIGH = 2002;
    private static final int NOTIF_AIR_QUALITY = 2003;
    private static final int NOTIF_WEATHER_CHANGE = 2004;
    private static final int NOTIF_TEMPERATURE = 2005;
    private static final int NOTIF_WIND = 2006;
    private static final int NOTIF_STORM = 2007;
    
    // SharedPreferences keys
    private static final String PREFS_NAME = "weather_alerts_prefs";
    private static final String KEY_LAST_RAIN_ALERT = "last_rain_alert";
    private static final String KEY_LAST_UV_ALERT = "last_uv_alert";
    private static final String KEY_LAST_AQ_ALERT = "last_aq_alert";
    private static final String KEY_LAST_TEMP = "last_temperature";
    
    // Alert cooldown: 2 minutes between same alert types
    private static final long ALERT_COOLDOWN = 2 * 60 * 1000;
    
    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final SharedPreferences prefs;
    private final com.example.weatherapp.utils.WeatherAlertPreferences alertPrefs;
    
    public SmartWeatherNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.alertPrefs = new com.example.weatherapp.utils.WeatherAlertPreferences(context);
        createNotificationChannels();
    }
    
    /**
     * Create notification channels for different alert types
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;
            
            // Critical alerts channel (storms, extreme weather)
            NotificationChannel criticalChannel = new NotificationChannel(
                    CHANNEL_CRITICAL,
                    "Critical Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            criticalChannel.setDescription("Urgent weather warnings and storm alerts");
            criticalChannel.enableVibration(true);
            criticalChannel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
            criticalChannel.setShowBadge(true);
            manager.createNotificationChannel(criticalChannel);
            
            // Warning channel (rain, UV, air quality)
            NotificationChannel warningChannel = new NotificationChannel(
                    CHANNEL_WARNING,
                    "Weather Warnings",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            warningChannel.setDescription("Weather warnings and advisories");
            warningChannel.enableVibration(true);
            warningChannel.setVibrationPattern(new long[]{0, 300, 200, 300});
            warningChannel.setShowBadge(true);
            manager.createNotificationChannel(warningChannel);
            
            // Info channel (general updates)
            NotificationChannel infoChannel = new NotificationChannel(
                    CHANNEL_INFO,
                    "Weather Information",
                    NotificationManager.IMPORTANCE_LOW
            );
            infoChannel.setDescription("General weather information and updates");
            infoChannel.setShowBadge(false);
            manager.createNotificationChannel(infoChannel);
        }
    }
    
    /**
     * Show smart weather alert
     */
    public void showWeatherAlert(WeatherAlert alert) {
        if (!areNotificationsEnabled() || !alertPrefs.areAlertsEnabled()) {
            Log.w(TAG, "Notifications are disabled");
            return;
        }
        
        // Check if specific alert type is enabled
        if (!isAlertTypeEnabled(alert.getType())) {
            Log.d(TAG, "Alert type disabled: " + alert.getType());
            return;
        }
        
        // Check cooldown for specific alert types
        if (!shouldShowAlert(alert.getType())) {
            Log.d(TAG, "Alert in cooldown period: " + alert.getType());
            return;
        }
        
        String channelId = getChannelForAlert(alert);
        int notificationId = getNotificationId(alert.getType());
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(getIconForAlertType(alert.getType()))
                .setContentTitle(alert.getIconEmoji() + " " + alert.getTitle())
                .setContentText(alert.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alert.getMessage()))
                .setPriority(getPriorityForSeverity(alert.getSeverity()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // Add vibration for high severity
        if (alert.getSeverity() == WeatherAlert.AlertSeverity.HIGH || 
            alert.getSeverity() == WeatherAlert.AlertSeverity.CRITICAL) {
            builder.setVibrate(new long[]{0, 500, 200, 500, 200, 500});
        }
        
        try {
            notificationManager.notify(notificationId, builder.build());
            updateLastAlertTime(alert.getType());
            Log.d(TAG, "Alert shown: " + alert.getTitle());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to show notification", e);
        }
    }
    
    /**
     * Check if rain is coming in forecast
     * @param rainProbability probability of rain (0-100)
     * @param minutesUntilRain estimated minutes until rain
     */
    public void checkRainWarning(int rainProbability, int minutesUntilRain) {
        if (rainProbability >= 60 && minutesUntilRain > 0 && minutesUntilRain <= 60) {
            String message = String.format(
                    "Rain expected in %d minutes! Probability: %d%%. " +
                    "Don't forget your umbrella! ☂️",
                    minutesUntilRain, rainProbability
            );
            
            WeatherAlert alert = new WeatherAlert(
                    WeatherAlert.AlertType.RAIN_WARNING,
                    "Rain Alert",
                    message,
                    rainProbability >= 80 ? WeatherAlert.AlertSeverity.HIGH : WeatherAlert.AlertSeverity.MEDIUM
            );
            showWeatherAlert(alert);
        }
    }
    
    /**
     * Check UV index and alert if high
     * @param uvIndex current UV index
     * @param hour current hour (0-23)
     */
    public void checkUVIndex(int uvIndex, int hour) {
        // Check UV during peak sun hours (10 AM - 3 PM)
        if (hour >= 10 && hour <= 15 && uvIndex >= 6) {
            String message;
            WeatherAlert.AlertSeverity severity;
            
            if (uvIndex >= 11) {
                message = String.format("⚠️ EXTREME UV Index: %d! Stay indoors or use maximum sun protection. " +
                        "Skin damage can occur in minutes!", uvIndex);
                severity = WeatherAlert.AlertSeverity.CRITICAL;
            } else if (uvIndex >= 8) {
                message = String.format("☀️ Very High UV Index: %d! Use SPF 30+ sunscreen, wear hat and sunglasses. " +
                        "Seek shade during midday hours.", uvIndex);
                severity = WeatherAlert.AlertSeverity.HIGH;
            } else {
                message = String.format("☀️ High UV Index: %d. Apply sunscreen and wear protective clothing if outdoors.", uvIndex);
                severity = WeatherAlert.AlertSeverity.MEDIUM;
            }
            
            WeatherAlert alert = new WeatherAlert(
                    WeatherAlert.AlertType.UV_HIGH,
                    "UV Index Warning",
                    message,
                    severity
            );
            showWeatherAlert(alert);
        }
    }
    
    /**
     * Check air quality and alert if poor
     * @param aqi Air Quality Index
     * @param mainPollutant main pollutant (PM2.5, PM10, etc.)
     */
    public void checkAirQuality(int aqi, String mainPollutant) {
        String message;
        WeatherAlert.AlertSeverity severity;
        
        // Alert for unhealthy air quality (AQI > 100)
        if (aqi >= 201) {
            message = String.format("⚠️ VERY UNHEALTHY Air Quality! AQI: %d (%s). " +
                    "Everyone should avoid outdoor activities. Health alert!", aqi, mainPollutant);
            severity = WeatherAlert.AlertSeverity.CRITICAL;
        } else if (aqi >= 151) {
            message = String.format("😷 Unhealthy Air Quality! AQI: %d (%s). " +
                    "Everyone may experience health effects. Reduce prolonged outdoor activities.", aqi, mainPollutant);
            severity = WeatherAlert.AlertSeverity.HIGH;
        } else if (aqi >= 101) {
            message = String.format("😷 Unhealthy for Sensitive Groups. AQI: %d (%s). " +
                    "People with respiratory issues should limit outdoor exposure.", aqi, mainPollutant);
            severity = WeatherAlert.AlertSeverity.MEDIUM;
        } else {
            return; // Good air quality, no alert needed
        }
        
        WeatherAlert alert = new WeatherAlert(
                WeatherAlert.AlertType.AIR_QUALITY_POOR,
                "Air Quality Alert",
                message,
                severity
        );
        showWeatherAlert(alert);
    }
    
    /**
     * Check for sudden weather changes
     * @param currentTemp current temperature
     * @param previousTemp temperature from previous check
     * @param currentCondition current weather condition
     * @param previousCondition previous weather condition
     */
    public void checkSuddenWeatherChange(double currentTemp, double previousTemp, 
                                         String currentCondition, String previousCondition) {
        double tempChange = Math.abs(currentTemp - previousTemp);
        
        // Alert for significant temperature changes (5°C or more)
        if (tempChange >= 5.0) {
            String direction = currentTemp > previousTemp ? "risen" : "dropped";
            String message = String.format(
                    "🌡️ Temperature has %s by %.1f°C! Current: %.1f°C (was %.1f°C). " +
                    "Dress accordingly!",
                    direction, tempChange, currentTemp, previousTemp
            );
            
            WeatherAlert.AlertSeverity severity = tempChange >= 10 ? 
                    WeatherAlert.AlertSeverity.HIGH : WeatherAlert.AlertSeverity.MEDIUM;
            
            WeatherAlert alert = new WeatherAlert(
                    WeatherAlert.AlertType.SUDDEN_WEATHER_CHANGE,
                    "Temperature Change Alert",
                    message,
                    severity
            );
            showWeatherAlert(alert);
        }
        
        // Alert if weather condition changed significantly
        if (!currentCondition.equals(previousCondition) && isSignificantConditionChange(previousCondition, currentCondition)) {
            String message = String.format(
                    "Weather conditions changed from %s to %s. Stay updated!",
                    previousCondition, currentCondition
            );
            
            WeatherAlert alert = new WeatherAlert(
                    WeatherAlert.AlertType.SUDDEN_WEATHER_CHANGE,
                    "Weather Condition Changed",
                    message,
                    WeatherAlert.AlertSeverity.MEDIUM
            );
            showWeatherAlert(alert);
        }
    }
    
    /**
     * Check if condition change is significant
     */
    private boolean isSignificantConditionChange(String from, String to) {
        String[] significantConditions = {"Rain", "Storm", "Snow", "Clear", "Clouds"};
        
        for (String condition : significantConditions) {
            if ((from.contains(condition) && !to.contains(condition)) ||
                (!from.contains(condition) && to.contains(condition))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if specific alert type is enabled in preferences
     */
    private boolean isAlertTypeEnabled(WeatherAlert.AlertType type) {
        switch (type) {
            case RAIN_WARNING:
                return alertPrefs.areRainAlertsEnabled();
            case UV_HIGH:
                return alertPrefs.areUVAlertsEnabled();
            case AIR_QUALITY_POOR:
                return alertPrefs.areAirQualityAlertsEnabled();
            case SUDDEN_WEATHER_CHANGE:
                return alertPrefs.areWeatherChangeAlertsEnabled();
            case TEMPERATURE_EXTREME:
                return alertPrefs.areTemperatureAlertsEnabled();
            case STORM_WARNING:
                return alertPrefs.areStormAlertsEnabled();
            default:
                return true;
        }
    }
    
    /**
     * Check if alert should be shown (cooldown check)
     */
    private boolean shouldShowAlert(WeatherAlert.AlertType type) {
        String key = getPrefsKeyForAlertType(type);
        long lastAlert = prefs.getLong(key, 0);
        long now = System.currentTimeMillis();
        return (now - lastAlert) >= ALERT_COOLDOWN;
    }
    
    /**
     * Update last alert time
     */
    private void updateLastAlertTime(WeatherAlert.AlertType type) {
        String key = getPrefsKeyForAlertType(type);
        prefs.edit().putLong(key, System.currentTimeMillis()).apply();
    }
    
    /**
     * Get SharedPreferences key for alert type
     */
    private String getPrefsKeyForAlertType(WeatherAlert.AlertType type) {
        switch (type) {
            case RAIN_WARNING:
                return KEY_LAST_RAIN_ALERT;
            case UV_HIGH:
                return KEY_LAST_UV_ALERT;
            case AIR_QUALITY_POOR:
                return KEY_LAST_AQ_ALERT;
            default:
                return "last_" + type.name().toLowerCase() + "_alert";
        }
    }
    
    /**
     * Get notification channel for alert
     */
    private String getChannelForAlert(WeatherAlert alert) {
        switch (alert.getSeverity()) {
            case CRITICAL:
            case HIGH:
                return CHANNEL_CRITICAL;
            case MEDIUM:
                return CHANNEL_WARNING;
            case LOW:
            default:
                return CHANNEL_INFO;
        }
    }
    
    /**
     * Get notification ID for alert type
     */
    private int getNotificationId(WeatherAlert.AlertType type) {
        switch (type) {
            case RAIN_WARNING:
                return NOTIF_RAIN_WARNING;
            case UV_HIGH:
                return NOTIF_UV_HIGH;
            case AIR_QUALITY_POOR:
                return NOTIF_AIR_QUALITY;
            case SUDDEN_WEATHER_CHANGE:
                return NOTIF_WEATHER_CHANGE;
            case TEMPERATURE_EXTREME:
                return NOTIF_TEMPERATURE;
            case WIND_STRONG:
                return NOTIF_WIND;
            case STORM_WARNING:
                return NOTIF_STORM;
            default:
                return 2999;
        }
    }
    
    /**
     * Get icon resource for alert type
     */
    private int getIconForAlertType(WeatherAlert.AlertType type) {
        // Using default launcher icon - replace with specific icons later
        return R.drawable.ic_launcher_foreground;
    }
    
    /**
     * Get priority for severity
     */
    private int getPriorityForSeverity(WeatherAlert.AlertSeverity severity) {
        switch (severity) {
            case CRITICAL:
            case HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case MEDIUM:
                return NotificationCompat.PRIORITY_DEFAULT;
            case LOW:
            default:
                return NotificationCompat.PRIORITY_LOW;
        }
    }
    
    /**
     * Get last stored temperature
     */
    public double getLastTemperature() {
        return prefs.getFloat(KEY_LAST_TEMP, Float.NaN);
    }
    
    /**
     * Save current temperature for next comparison
     */
    public void saveLastTemperature(double temperature) {
        prefs.edit().putFloat(KEY_LAST_TEMP, (float) temperature).apply();
        Log.d(TAG, "Saved temperature: " + temperature + "°C");
    }
    
    /**
     * Check if notifications are enabled
     */
    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }
    
    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
