package com.example.weatherapp.presentation.viewmodel;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for SettingsActivity
 * Manages app settings with reactive updates
 */
public class SettingsViewModel extends ViewModel {
    
    private final MutableLiveData<String> temperatureUnit = new MutableLiveData<>();
    private final MutableLiveData<String> windSpeedUnit = new MutableLiveData<>();
    private final MutableLiveData<String> pressureUnit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notificationsEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> language = new MutableLiveData<>();
    private final MutableLiveData<Boolean> settingsChanged = new MutableLiveData<>(false);
    
    private SharedPreferences sharedPreferences;
    
    // Preference keys
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_WIND_SPEED_UNIT = "wind_speed_unit";
    private static final String KEY_PRESSURE_UNIT = "pressure_unit";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_LANGUAGE = "language";
    
    public LiveData<String> getTemperatureUnit() {
        return temperatureUnit;
    }
    
    public LiveData<String> getWindSpeedUnit() {
        return windSpeedUnit;
    }
    
    public LiveData<String> getPressureUnit() {
        return pressureUnit;
    }
    
    public LiveData<Boolean> getDarkMode() {
        return darkMode;
    }
    
    public LiveData<Boolean> getNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public LiveData<String> getLanguage() {
        return language;
    }
    
    public LiveData<Boolean> getSettingsChanged() {
        return settingsChanged;
    }
    
    /**
     * Initialize ViewModel with SharedPreferences
     */
    public void init(SharedPreferences prefs) {
        this.sharedPreferences = prefs;
        loadSettings();
    }
    
    /**
     * Load current settings from SharedPreferences
     */
    private void loadSettings() {
        temperatureUnit.setValue(sharedPreferences.getString(KEY_TEMPERATURE_UNIT, "celsius"));
        windSpeedUnit.setValue(sharedPreferences.getString(KEY_WIND_SPEED_UNIT, "ms"));
        pressureUnit.setValue(sharedPreferences.getString(KEY_PRESSURE_UNIT, "hpa"));
        darkMode.setValue(sharedPreferences.getBoolean(KEY_DARK_MODE, false));
        notificationsEnabled.setValue(sharedPreferences.getBoolean(KEY_NOTIFICATIONS, false));
        language.setValue(sharedPreferences.getString(KEY_LANGUAGE, "en"));
    }
    
    /**
     * Update temperature unit
     */
    public void setTemperatureUnit(String unit) {
        if (savePreference(KEY_TEMPERATURE_UNIT, unit)) {
            temperatureUnit.setValue(unit);
            markSettingsChanged();
        }
    }
    
    /**
     * Update wind speed unit
     */
    public void setWindSpeedUnit(String unit) {
        if (savePreference(KEY_WIND_SPEED_UNIT, unit)) {
            windSpeedUnit.setValue(unit);
            markSettingsChanged();
        }
    }
    
    /**
     * Update pressure unit
     */
    public void setPressureUnit(String unit) {
        if (savePreference(KEY_PRESSURE_UNIT, unit)) {
            pressureUnit.setValue(unit);
            markSettingsChanged();
        }
    }
    
    /**
     * Update dark mode
     */
    public void setDarkMode(boolean enabled) {
        if (savePreference(KEY_DARK_MODE, enabled)) {
            darkMode.setValue(enabled);
            markSettingsChanged();
        }
    }
    
    /**
     * Update notifications
     */
    public void setNotificationsEnabled(boolean enabled) {
        if (savePreference(KEY_NOTIFICATIONS, enabled)) {
            notificationsEnabled.setValue(enabled);
            markSettingsChanged();
        }
    }
    
    /**
     * Update language
     */
    public void setLanguage(String lang) {
        if (savePreference(KEY_LANGUAGE, lang)) {
            language.setValue(lang);
            markSettingsChanged();
        }
    }
    
    /**
     * Save string preference
     */
    private boolean savePreference(String key, String value) {
        return sharedPreferences.edit().putString(key, value).commit();
    }
    
    /**
     * Save boolean preference
     */
    private boolean savePreference(String key, boolean value) {
        return sharedPreferences.edit().putBoolean(key, value).commit();
    }
    
    /**
     * Mark that settings have been changed
     */
    private void markSettingsChanged() {
        settingsChanged.setValue(true);
    }
    
    /**
     * Get current temperature unit value
     */
    public String getCurrentTemperatureUnit() {
        return temperatureUnit.getValue();
    }
    
    /**
     * Get current wind speed unit value
     */
    public String getCurrentWindSpeedUnit() {
        return windSpeedUnit.getValue();
    }
    
    /**
     * Get current pressure unit value
     */
    public String getCurrentPressureUnit() {
        return pressureUnit.getValue();
    }
    
    /**
     * Check if dark mode is enabled
     */
    public boolean isDarkModeEnabled() {
        Boolean value = darkMode.getValue();
        return value != null && value;
    }
    
    /**
     * Check if notifications are enabled
     */
    public boolean areNotificationsEnabled() {
        Boolean value = notificationsEnabled.getValue();
        return value != null && value;
    }
    
    /**
     * Get current language
     */
    public String getCurrentLanguage() {
        return language.getValue();
    }
}
