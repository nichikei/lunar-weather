package com.example.weatherapp.domain.model;

import java.io.Serializable;

/**
 * Domain model for Weather data
 * Clean, business-logic focused model independent of API response structure
 */
public class WeatherData implements Serializable {
    private final String cityName;
    private final String countryCode;
    private final double temperature;
    private final double feelsLike;
    private final double minTemperature;
    private final double maxTemperature;
    private final int humidity;
    private final double pressure;
    private final double windSpeed;
    private final int windDegree;
    private final String weatherMain;
    private final String weatherDescription;
    private final String weatherIcon;
    private final int cloudiness;
    private final double visibility;
    private final long sunrise;
    private final long sunset;
    private final long timestamp;
    private final double latitude;
    private final double longitude;
    private final Double rainVolume; // Can be null
    private final String temperatureUnit;
    
    private WeatherData(Builder builder) {
        this.cityName = builder.cityName;
        this.countryCode = builder.countryCode;
        this.temperature = builder.temperature;
        this.feelsLike = builder.feelsLike;
        this.minTemperature = builder.minTemperature;
        this.maxTemperature = builder.maxTemperature;
        this.humidity = builder.humidity;
        this.pressure = builder.pressure;
        this.windSpeed = builder.windSpeed;
        this.windDegree = builder.windDegree;
        this.weatherMain = builder.weatherMain;
        this.weatherDescription = builder.weatherDescription;
        this.weatherIcon = builder.weatherIcon;
        this.cloudiness = builder.cloudiness;
        this.visibility = builder.visibility;
        this.sunrise = builder.sunrise;
        this.sunset = builder.sunset;
        this.timestamp = builder.timestamp;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.rainVolume = builder.rainVolume;
        this.temperatureUnit = builder.temperatureUnit;
    }
    
    // Getters
    public String getCityName() { return cityName; }
    public String getCountryCode() { return countryCode; }
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public double getMinTemperature() { return minTemperature; }
    public double getMaxTemperature() { return maxTemperature; }
    public int getHumidity() { return humidity; }
    public double getPressure() { return pressure; }
    public double getWindSpeed() { return windSpeed; }
    public int getWindDegree() { return windDegree; }
    public int getWindDirection() { return windDegree; } // Alias
    public String getWeatherMain() { return weatherMain; }
    public String getWeatherDescription() { return weatherDescription; }
    public String getWeatherIcon() { return weatherIcon; }
    public int getCloudiness() { return cloudiness; }
    public double getVisibility() { return visibility; }
    public long getSunrise() { return sunrise; }
    public long getSunset() { return sunset; }
    public long getTimestamp() { return timestamp; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Double getRainVolume() { return rainVolume; }
    public String getTemperatureUnit() { return temperatureUnit; }
    
    // Builder Pattern
    public static class Builder {
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
        private Double rainVolume;
        private String temperatureUnit = "celsius";
        
        public Builder setCityName(String cityName) {
            this.cityName = cityName;
            return this;
        }
        
        public Builder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }
        
        public Builder setTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder setFeelsLike(double feelsLike) {
            this.feelsLike = feelsLike;
            return this;
        }
        
        public Builder setMinTemperature(double minTemperature) {
            this.minTemperature = minTemperature;
            return this;
        }
        
        public Builder setMaxTemperature(double maxTemperature) {
            this.maxTemperature = maxTemperature;
            return this;
        }
        
        public Builder setHumidity(int humidity) {
            this.humidity = humidity;
            return this;
        }
        
        public Builder setPressure(double pressure) {
            this.pressure = pressure;
            return this;
        }
        
        public Builder setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }
        
        public Builder setWindDegree(int windDegree) {
            this.windDegree = windDegree;
            return this;
        }
        
        public Builder setWeatherMain(String weatherMain) {
            this.weatherMain = weatherMain;
            return this;
        }
        
        public Builder setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
            return this;
        }
        
        public Builder setWeatherIcon(String weatherIcon) {
            this.weatherIcon = weatherIcon;
            return this;
        }
        
        public Builder setCloudiness(int cloudiness) {
            this.cloudiness = cloudiness;
            return this;
        }
        
        public Builder setVisibility(double visibility) {
            this.visibility = visibility;
            return this;
        }
        
        public Builder setSunrise(long sunrise) {
            this.sunrise = sunrise;
            return this;
        }
        
        public Builder setSunset(long sunset) {
            this.sunset = sunset;
            return this;
        }
        
        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }
        
        public Builder setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }
        
        public Builder setRainVolume(Double rainVolume) {
            this.rainVolume = rainVolume;
            return this;
        }
        
        public Builder setTemperatureUnit(String temperatureUnit) {
            this.temperatureUnit = temperatureUnit;
            return this;
        }
        
        public WeatherData build() {
            return new WeatherData(this);
        }
    }
}
