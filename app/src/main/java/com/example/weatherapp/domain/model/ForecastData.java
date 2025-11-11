package com.example.weatherapp.domain.model;

import java.util.List;

/**
 * Domain model for Forecast data (hourly and daily)
 */
public class ForecastData {
    private final List<HourlyForecast> hourlyForecasts;
    private final List<DailyForecast> dailyForecasts;
    private final String cityName;
    
    public ForecastData(List<HourlyForecast> hourlyForecasts, 
                        List<DailyForecast> dailyForecasts, 
                        String cityName) {
        this.hourlyForecasts = hourlyForecasts;
        this.dailyForecasts = dailyForecasts;
        this.cityName = cityName;
    }
    
    public List<HourlyForecast> getHourlyForecasts() {
        return hourlyForecasts;
    }
    
    public List<DailyForecast> getDailyForecasts() {
        return dailyForecasts;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    /**
     * Hourly forecast item
     */
    public static class HourlyForecast {
        private final long timestamp;
        private final double temperature;
        private final String weatherIcon;
        private final String weatherDescription;
        private final int humidity;
        private final double windSpeed;
        private final int rainProbability; // 0-100
        
        public HourlyForecast(long timestamp, double temperature, String weatherIcon, 
                              String weatherDescription, int humidity, double windSpeed, 
                              int rainProbability) {
            this.timestamp = timestamp;
            this.temperature = temperature;
            this.weatherIcon = weatherIcon;
            this.weatherDescription = weatherDescription;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.rainProbability = rainProbability;
        }
        
        public long getTimestamp() { return timestamp; }
        public double getTemperature() { return temperature; }
        public String getWeatherIcon() { return weatherIcon; }
        public String getWeatherDescription() { return weatherDescription; }
        public int getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public int getRainProbability() { return rainProbability; }
    }
    
    /**
     * Daily forecast item
     */
    public static class DailyForecast {
        private final long timestamp;
        private final double tempMin;
        private final double tempMax;
        private final String weatherIcon;
        private final String weatherDescription;
        private final int rainProbability; // 0-100
        
        public DailyForecast(long timestamp, double tempMin, double tempMax, 
                             String weatherIcon, String weatherDescription, 
                             int rainProbability) {
            this.timestamp = timestamp;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.weatherIcon = weatherIcon;
            this.weatherDescription = weatherDescription;
            this.rainProbability = rainProbability;
        }
        
        public long getTimestamp() { return timestamp; }
        public double getTempMin() { return tempMin; }
        public double getTempMax() { return tempMax; }
        public String getWeatherIcon() { return weatherIcon; }
        public String getWeatherDescription() { return weatherDescription; }
        public int getRainProbability() { return rainProbability; }
    }
}
