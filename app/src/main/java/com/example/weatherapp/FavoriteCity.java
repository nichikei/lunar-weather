package com.example.weatherapp;

public class FavoriteCity {
    private String cityName;
    private String country;
    private double latitude;
    private double longitude;
    private double currentTemp;
    private String weatherCondition;
    private String weatherDescription;
    private long lastUpdated;

    public FavoriteCity(String cityName, String country, double latitude, double longitude) {
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public double getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(double currentTemp) {
        this.currentTemp = currentTemp;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDisplayName() {
        return cityName + ", " + country;
    }
}

