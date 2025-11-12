package com.example.weatherapp.domain.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Weather Alarm Model
 * Represents a smart alarm that triggers based on weather conditions
 */
public class WeatherAlarm implements Serializable {
    private String id;
    private String title;
    private int hourOfDay;
    private int minute;
    private AlarmType type;
    private AlarmCondition condition;
    private boolean enabled;
    private long createdAt;
    private String cityName;
    
    // Days of week (0 = Sunday, 6 = Saturday)
    private boolean[] daysOfWeek;
    
    public WeatherAlarm() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.enabled = true;
        this.daysOfWeek = new boolean[7]; // All days disabled by default
    }
    
    public WeatherAlarm(String title, int hourOfDay, int minute, AlarmType type, AlarmCondition condition) {
        this();
        this.title = title;
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.type = type;
        this.condition = condition;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getHourOfDay() {
        return hourOfDay;
    }
    
    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    public AlarmType getType() {
        return type;
    }
    
    public void setType(AlarmType type) {
        this.type = type;
    }
    
    public AlarmCondition getCondition() {
        return condition;
    }
    
    public void setCondition(AlarmCondition condition) {
        this.condition = condition;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public boolean[] getDaysOfWeek() {
        return daysOfWeek;
    }
    
    public void setDaysOfWeek(boolean[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    
    public void setDayOfWeek(int day, boolean enabled) {
        if (day >= 0 && day < 7) {
            daysOfWeek[day] = enabled;
        }
    }
    
    public boolean isDayEnabled(int day) {
        if (day >= 0 && day < 7) {
            return daysOfWeek[day];
        }
        return false;
    }
    
    /**
     * Get formatted time string (e.g., "7:30 AM")
     */
    public String getFormattedTime() {
        int hour12 = hourOfDay % 12;
        if (hour12 == 0) hour12 = 12;
        String amPm = hourOfDay < 12 ? "AM" : "PM";
        return String.format("%d:%02d %s", hour12, minute, amPm);
    }
    
    /**
     * Get alarm description based on type and condition
     */
    public String getDescription() {
        switch (type) {
            case WAKE_UP_EARLY:
                return "Wake up " + condition.getEarlyMinutes() + " min early if " + condition.getWeatherCondition();
            case UMBRELLA_REMINDER:
                return "Remind me to bring umbrella if rain forecast";
            case UV_ALERT:
                return "Alert me when UV index ≥ " + condition.getUvThreshold();
            case AIR_QUALITY_ALERT:
                return "Alert when AQI > " + condition.getAqiThreshold() + " (Unhealthy)";
            case ICY_ROADS_ALERT:
                return "Alert if roads may be icy (temp ≤ " + condition.getTemperatureThreshold() + "°C)";
            case TEMPERATURE_ALERT:
                return "Alert when temp " + condition.getComparisonOperator() + " " + condition.getTemperatureThreshold() + "°C";
            default:
                return "Weather alarm";
        }
    }
    
    /**
     * Check if alarm should run today
     */
    public boolean shouldRunToday() {
        // If no days selected, run every day
        boolean anyDaySelected = false;
        for (boolean day : daysOfWeek) {
            if (day) {
                anyDaySelected = true;
                break;
            }
        }
        
        if (!anyDaySelected) {
            return true;
        }
        
        // Check if today is selected
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int today = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1; // Convert to 0-6
        return daysOfWeek[today];
    }
    
    @Override
    public String toString() {
        return "WeatherAlarm{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", time=" + getFormattedTime() +
                ", type=" + type +
                ", enabled=" + enabled +
                '}';
    }
}
