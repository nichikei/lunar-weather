package com.example.weatherapp.domain.model;

/**
 * Alarm Type Enum
 * Defines different types of weather-based alarms
 */
public enum AlarmType {
    /**
     * Wake up earlier if bad weather (rain, snow, etc.)
     */
    WAKE_UP_EARLY("⏰ Wake Up Early", "Wake up earlier to prepare for bad weather"),
    
    /**
     * Remind to bring umbrella if rain forecast
     */
    UMBRELLA_REMINDER("🌂 Umbrella Reminder", "Remind you to bring an umbrella"),
    
    /**
     * Alert when UV index is high
     */
    UV_ALERT("☀️ UV Index Alert", "Alert when UV index is dangerous"),
    
    /**
     * Alert when air quality is poor
     */
    AIR_QUALITY_ALERT("💨 Air Quality Alert", "Alert when air quality is unhealthy"),
    
    /**
     * Alert if roads may be icy
     */
    ICY_ROADS_ALERT("🚗 Icy Roads Alert", "Alert if roads may be icy for driving"),
    
    /**
     * Alert for specific temperature threshold
     */
    TEMPERATURE_ALERT("🌡️ Temperature Alert", "Alert when temperature crosses threshold");
    
    private final String displayName;
    private final String description;
    
    AlarmType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get icon emoji for this alarm type
     */
    public String getIcon() {
        switch (this) {
            case WAKE_UP_EARLY:
                return "⏰";
            case UMBRELLA_REMINDER:
                return "🌂";
            case UV_ALERT:
                return "☀️";
            case AIR_QUALITY_ALERT:
                return "💨";
            case ICY_ROADS_ALERT:
                return "🚗";
            case TEMPERATURE_ALERT:
                return "🌡️";
            default:
                return "⏰";
        }
    }
}
