package com.example.weatherapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity for caching weather data
 * Allows offline access to last fetched weather
 */
@Entity(tableName = "weather_cache")
public class WeatherCacheEntity {
    
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String cityName;
    
    private String countryCode;
    private double temperature;
    private double feelsLike;
    private double minTemperature;
    private double maxTemperature;
    private int humidity;
    private double pressure;
    private double windSpeed;
    private int windDegree;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private int cloudiness;
    private double visibility;
    private long sunrise;
    private long sunset;
    private long timestamp;
    private double latitude;
    private double longitude;
    private String temperatureUnit;
    private long cachedAt; // When was this cached
    
    // Constructor
    public WeatherCacheEntity() {
        this.cachedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    
    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }
    
    public double getMinTemperature() { return minTemperature; }
    public void setMinTemperature(double minTemperature) { this.minTemperature = minTemperature; }
    
    public double getMaxTemperature() { return maxTemperature; }
    public void setMaxTemperature(double maxTemperature) { this.maxTemperature = maxTemperature; }
    
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    
    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    
    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    
    public int getWindDegree() { return windDegree; }
    public void setWindDegree(int windDegree) { this.windDegree = windDegree; }
    
    public String getWeatherMain() { return weatherMain; }
    public void setWeatherMain(String weatherMain) { this.weatherMain = weatherMain; }
    
    public String getWeatherDescription() { return weatherDescription; }
    public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }
    
    public String getWeatherIcon() { return weatherIcon; }
    public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }
    
    public int getCloudiness() { return cloudiness; }
    public void setCloudiness(int cloudiness) { this.cloudiness = cloudiness; }
    
    public double getVisibility() { return visibility; }
    public void setVisibility(double visibility) { this.visibility = visibility; }
    
    public long getSunrise() { return sunrise; }
    public void setSunrise(long sunrise) { this.sunrise = sunrise; }
    
    public long getSunset() { return sunset; }
    public void setSunset(long sunset) { this.sunset = sunset; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getTemperatureUnit() { return temperatureUnit; }
    public void setTemperatureUnit(String temperatureUnit) { this.temperatureUnit = temperatureUnit; }
    
    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
    
    /**
     * Check if cache is still valid (within 10 minutes)
     */
    public boolean isValid() {
        long tenMinutes = 10 * 60 * 1000;
        return (System.currentTimeMillis() - cachedAt) < tenMinutes;
    }
}
