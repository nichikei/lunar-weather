package com.example.weatherapp.domain.model;

import java.io.Serializable;

/**
 * Alarm Condition
 * Defines the weather conditions that trigger an alarm
 */
public class AlarmCondition implements Serializable {
    // Weather condition types
    public enum WeatherConditionType {
        RAIN("Rain", "rain"),
        SNOW("Snow", "snow"),
        STORM("Storm", "storm"),
        FOG("Fog", "fog"),
        EXTREME_COLD("Extreme Cold", "cold"),
        EXTREME_HEAT("Extreme Heat", "heat"),
        ANY_BAD_WEATHER("Any Bad Weather", "bad");
        
        private final String displayName;
        private final String keyword;
        
        WeatherConditionType(String displayName, String keyword) {
            this.displayName = displayName;
            this.keyword = keyword;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getKeyword() {
            return keyword;
        }
    }
    
    // Comparison operators for temperature
    public enum ComparisonOperator {
        GREATER_THAN(">", "above"),
        LESS_THAN("<", "below"),
        EQUAL_TO("=", "equal to"),
        GREATER_THAN_OR_EQUAL("≥", "at least"),
        LESS_THAN_OR_EQUAL("≤", "at most");
        
        private final String symbol;
        private final String description;
        
        ComparisonOperator(String symbol, String description) {
            this.symbol = symbol;
            this.description = description;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Condition parameters
    private WeatherConditionType weatherCondition;
    private int earlyMinutes; // How many minutes early to wake up
    private double temperatureThreshold; // Temperature threshold in Celsius
    private ComparisonOperator comparisonOperator;
    private int uvThreshold; // UV index threshold (0-11+)
    private int aqiThreshold; // AQI threshold (0-500)
    private int hoursBeforeEvent; // How many hours before to check
    
    public AlarmCondition() {
        // Default values
        this.weatherCondition = WeatherConditionType.ANY_BAD_WEATHER;
        this.earlyMinutes = 30;
        this.temperatureThreshold = 0;
        this.comparisonOperator = ComparisonOperator.LESS_THAN;
        this.uvThreshold = 8; // High UV
        this.aqiThreshold = 150; // Unhealthy
        this.hoursBeforeEvent = 2; // Check 2 hours before
    }
    
    // Static factory methods for common conditions
    public static AlarmCondition forWakeUpEarly(WeatherConditionType condition, int minutes) {
        AlarmCondition c = new AlarmCondition();
        c.weatherCondition = condition;
        c.earlyMinutes = minutes;
        return c;
    }
    
    public static AlarmCondition forUmbrellaReminder() {
        AlarmCondition c = new AlarmCondition();
        c.weatherCondition = WeatherConditionType.RAIN;
        c.hoursBeforeEvent = 2;
        return c;
    }
    
    public static AlarmCondition forUvAlert(int threshold) {
        AlarmCondition c = new AlarmCondition();
        c.uvThreshold = threshold;
        return c;
    }
    
    public static AlarmCondition forAirQualityAlert(int threshold) {
        AlarmCondition c = new AlarmCondition();
        c.aqiThreshold = threshold;
        return c;
    }
    
    public static AlarmCondition forIcyRoads(double tempThreshold) {
        AlarmCondition c = new AlarmCondition();
        c.temperatureThreshold = tempThreshold;
        c.weatherCondition = WeatherConditionType.EXTREME_COLD;
        return c;
    }
    
    public static AlarmCondition forTemperature(double threshold, ComparisonOperator operator) {
        AlarmCondition c = new AlarmCondition();
        c.temperatureThreshold = threshold;
        c.comparisonOperator = operator;
        return c;
    }
    
    // Getters and Setters
    public WeatherConditionType getWeatherCondition() {
        return weatherCondition;
    }
    
    public void setWeatherCondition(WeatherConditionType weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
    
    public int getEarlyMinutes() {
        return earlyMinutes;
    }
    
    public void setEarlyMinutes(int earlyMinutes) {
        this.earlyMinutes = earlyMinutes;
    }
    
    public double getTemperatureThreshold() {
        return temperatureThreshold;
    }
    
    public void setTemperatureThreshold(double temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }
    
    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }
    
    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }
    
    public int getUvThreshold() {
        return uvThreshold;
    }
    
    public void setUvThreshold(int uvThreshold) {
        this.uvThreshold = uvThreshold;
    }
    
    public int getAqiThreshold() {
        return aqiThreshold;
    }
    
    public void setAqiThreshold(int aqiThreshold) {
        this.aqiThreshold = aqiThreshold;
    }
    
    public int getHoursBeforeEvent() {
        return hoursBeforeEvent;
    }
    
    public void setHoursBeforeEvent(int hoursBeforeEvent) {
        this.hoursBeforeEvent = hoursBeforeEvent;
    }
    
    /**
     * Check if weather data matches this condition
     */
    public boolean matchesWeatherData(WeatherData weatherData) {
        if (weatherData == null) {
            return false;
        }
        
        // Check temperature condition
        double currentTemp = weatherData.getTemperature();
        switch (comparisonOperator) {
            case GREATER_THAN:
                if (currentTemp <= temperatureThreshold) return false;
                break;
            case LESS_THAN:
                if (currentTemp >= temperatureThreshold) return false;
                break;
            case EQUAL_TO:
                if (Math.abs(currentTemp - temperatureThreshold) > 1) return false;
                break;
            case GREATER_THAN_OR_EQUAL:
                if (currentTemp < temperatureThreshold) return false;
                break;
            case LESS_THAN_OR_EQUAL:
                if (currentTemp > temperatureThreshold) return false;
                break;
        }
        
        // Check weather condition
        String condition = weatherData.getCondition().toLowerCase();
        String keyword = weatherCondition.getKeyword();
        
        if (weatherCondition == WeatherConditionType.ANY_BAD_WEATHER) {
            return condition.contains("rain") || condition.contains("snow") || 
                   condition.contains("storm") || condition.contains("fog");
        } else {
            return condition.contains(keyword);
        }
    }
    
    @Override
    public String toString() {
        return "AlarmCondition{" +
                "weatherCondition=" + weatherCondition +
                ", earlyMinutes=" + earlyMinutes +
                ", temperatureThreshold=" + temperatureThreshold +
                ", uvThreshold=" + uvThreshold +
                ", aqiThreshold=" + aqiThreshold +
                '}';
    }
}
