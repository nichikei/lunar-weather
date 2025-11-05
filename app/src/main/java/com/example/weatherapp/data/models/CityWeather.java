package com.example.weatherapp.data.models;

import com.example.weatherapp.R;

public class CityWeather {
    private String cityName;
    private String country;
    private String weatherDescription;
    private int temperature;
    private int highTemp;
    private int lowTemp;
    private String iconName;
    private int iconResourceId;
    private int gradientBackground; // Gradient drawable resource for card

    public CityWeather(String cityName, String country, String weatherDescription,
                       int temperature, int highTemp, int lowTemp, String iconName) {
        this.cityName = cityName;
        this.country = country;
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.iconName = iconName;
        this.iconResourceId = 0;
        this.gradientBackground = R.drawable.gradient_weather_1; // Default gradient
    }

    // Full constructor with icon resource and gradient
    public CityWeather(String cityName, String country, String weatherDescription,
                       int temperature, int highTemp, int lowTemp,
                       int iconResourceId, int gradientBackground) {
        this.cityName = cityName;
        this.country = country;
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.iconName = "";
        this.iconResourceId = iconResourceId;
        this.gradientBackground = gradientBackground;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getHighTemp() {
        return highTemp;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public String getIconName() {
        return iconName;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public int getGradientBackground() {
        return gradientBackground;
    }

    public void setGradientBackground(int gradientBackground) {
        this.gradientBackground = gradientBackground;
    }
}
