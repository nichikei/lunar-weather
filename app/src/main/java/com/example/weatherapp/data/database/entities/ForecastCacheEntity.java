package com.example.weatherapp.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for caching forecast data
 * Stores hourly and daily forecast information
 */
@Entity(tableName = "forecast_cache")
public class ForecastCacheEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "city_name")
    private String cityName;
    
    @ColumnInfo(name = "latitude")
    private double latitude;
    
    @ColumnInfo(name = "longitude")
    private double longitude;
    
    @ColumnInfo(name = "forecast_type")
    private String forecastType; // "hourly" or "daily"
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    @ColumnInfo(name = "temperature")
    private double temperature;
    
    @ColumnInfo(name = "temp_min")
    private Double tempMin; // For daily forecasts
    
    @ColumnInfo(name = "temp_max")
    private Double tempMax; // For daily forecasts
    
    @ColumnInfo(name = "weather_main")
    private String weatherMain;
    
    @ColumnInfo(name = "weather_description")
    private String weatherDescription;
    
    @ColumnInfo(name = "weather_icon")
    private String weatherIcon;
    
    @ColumnInfo(name = "humidity")
    private int humidity;
    
    @ColumnInfo(name = "wind_speed")
    private double windSpeed;
    
    @ColumnInfo(name = "rain_probability")
    private int rainProbability; // 0-100
    
    @ColumnInfo(name = "cached_at")
    private long cachedAt; // When this data was cached
    
    // Constructors
    public ForecastCacheEntity() {
    }
    
    @Ignore
    public ForecastCacheEntity(String cityName, double latitude, double longitude,
                              String forecastType, long timestamp, double temperature,
                              Double tempMin, Double tempMax, String weatherMain,
                              String weatherDescription, String weatherIcon, int humidity,
                              double windSpeed, int rainProbability, long cachedAt) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.forecastType = forecastType;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.weatherMain = weatherMain;
        this.weatherDescription = weatherDescription;
        this.weatherIcon = weatherIcon;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.rainProbability = rainProbability;
        this.cachedAt = cachedAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getForecastType() {
        return forecastType;
    }
    
    public void setForecastType(String forecastType) {
        this.forecastType = forecastType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public Double getTempMin() {
        return tempMin;
    }
    
    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }
    
    public Double getTempMax() {
        return tempMax;
    }
    
    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }
    
    public String getWeatherMain() {
        return weatherMain;
    }
    
    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }
    
    public String getWeatherDescription() {
        return weatherDescription;
    }
    
    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }
    
    public String getWeatherIcon() {
        return weatherIcon;
    }
    
    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }
    
    public int getHumidity() {
        return humidity;
    }
    
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
    
    public double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public int getRainProbability() {
        return rainProbability;
    }
    
    public void setRainProbability(int rainProbability) {
        this.rainProbability = rainProbability;
    }
    
    public long getCachedAt() {
        return cachedAt;
    }
    
    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
