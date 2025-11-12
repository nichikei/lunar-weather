package com.example.weatherapp.domain.model;

/**
 * Model representing different types of weather alerts
 */
public class WeatherAlert {
    private AlertType type;
    private String title;
    private String message;
    private AlertSeverity severity;
    private long timestamp;
    
    public enum AlertType {
        SUDDEN_WEATHER_CHANGE,      // Thay đổi thời tiết đột ngột
        RAIN_WARNING,                // Cảnh báo mưa
        UV_HIGH,                     // UV cao
        AIR_QUALITY_POOR,            // Chất lượng không khí xấu
        TEMPERATURE_EXTREME,         // Nhiệt độ cực đoan
        WIND_STRONG,                 // Gió mạnh
        STORM_WARNING               // Cảnh báo bão
    }
    
    public enum AlertSeverity {
        LOW,        // Thông tin
        MEDIUM,     // Cảnh báo
        HIGH,       // Nguy hiểm
        CRITICAL    // Rất nguy hiểm
    }
    
    public WeatherAlert(AlertType type, String title, String message, AlertSeverity severity) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public AlertType getType() {
        return type;
    }
    
    public void setType(AlertType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public AlertSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get notification icon based on alert type
     */
    public String getIconEmoji() {
        switch (type) {
            case SUDDEN_WEATHER_CHANGE:
                return "🌡️";
            case RAIN_WARNING:
                return "🌧️";
            case UV_HIGH:
                return "☀️";
            case AIR_QUALITY_POOR:
                return "😷";
            case TEMPERATURE_EXTREME:
                return "🥵";
            case WIND_STRONG:
                return "💨";
            case STORM_WARNING:
                return "⛈️";
            default:
                return "⚠️";
        }
    }
}
