package com.example.weatherapp.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.weatherapp.utils.Constants;

/**
 * Quản lý tất cả các SharedPreferences trong ứng dụng
 * Tập trung logic đọc/ghi preferences, dễ maintain và test
 */
public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ============ Temperature Unit ============
    public void setTemperatureUnit(String unit) {
        sharedPreferences.edit().putString(Constants.KEY_TEMPERATURE_UNIT, unit).apply();
    }

    public String getTemperatureUnit() {
        return sharedPreferences.getString(Constants.KEY_TEMPERATURE_UNIT, Constants.DEFAULT_TEMPERATURE_UNIT);
    }

    // ============ Wind Speed Unit ============
    public void setWindSpeedUnit(String unit) {
        sharedPreferences.edit().putString(Constants.KEY_WIND_SPEED_UNIT, unit).apply();
    }

    public String getWindSpeedUnit() {
        return sharedPreferences.getString(Constants.KEY_WIND_SPEED_UNIT, Constants.DEFAULT_WIND_UNIT);
    }

    // ============ Pressure Unit ============
    public void setPressureUnit(String unit) {
        sharedPreferences.edit().putString(Constants.KEY_PRESSURE_UNIT, unit).apply();
    }

    public String getPressureUnit() {
        return sharedPreferences.getString(Constants.KEY_PRESSURE_UNIT, Constants.DEFAULT_PRESSURE_UNIT);
    }

    // ============ Dark Mode ============
    public void setDarkMode(boolean enabled) {
        sharedPreferences.edit().putBoolean(Constants.KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(Constants.KEY_DARK_MODE, true);
    }

    // ============ Notifications ============
    public void setNotificationsEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(Constants.KEY_NOTIFICATIONS, enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return sharedPreferences.getBoolean(Constants.KEY_NOTIFICATIONS, true);
    }

    // ============ Language ============
    public void setLanguage(String language) {
        sharedPreferences.edit().putString(Constants.KEY_LANGUAGE, language).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(Constants.KEY_LANGUAGE, Constants.DEFAULT_LANGUAGE);
    }

    // ============ Last City ============
    public void setLastCity(String cityName) {
        sharedPreferences.edit().putString(Constants.KEY_LAST_CITY, cityName).apply();
    }

    public String getLastCity() {
        return sharedPreferences.getString(Constants.KEY_LAST_CITY, Constants.DEFAULT_CITY);
    }

    // ============ Generic Methods ============
    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        sharedPreferences.edit().putFloat(key, value).apply();
    }

    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}

