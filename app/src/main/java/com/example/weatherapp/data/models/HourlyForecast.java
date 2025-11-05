package com.example.weatherapp.data.models;

public class HourlyForecast {
    private String hour;
    private String icon;
    private int iconResource;
    private int temperature;
    private boolean isNow;
    private int rainProbability; // Rain probability percentage

    public HourlyForecast(String hour, String icon, int iconResource, int temperature, boolean isNow) {
        this.hour = hour;
        this.icon = icon;
        this.iconResource = iconResource;
        this.temperature = temperature;
        this.isNow = isNow;
        this.rainProbability = 0;
    }

    public HourlyForecast(String hour, String icon, int iconResource, int temperature, boolean isNow, int rainProbability) {
        this.hour = hour;
        this.icon = icon;
        this.iconResource = iconResource;
        this.temperature = temperature;
        this.isNow = isNow;
        this.rainProbability = rainProbability;
    }

    public String getHour() {
        return hour;
    }

    public String getIcon() {
        return icon;
    }

    public int getIconResource() {
        return iconResource;
    }

    public int getTemperature() {
        return temperature;
    }

    public boolean isNow() {
        return isNow;
    }

    public int getRainProbability() {
        return rainProbability;
    }

    public void setRainProbability(int rainProbability) {
        this.rainProbability = rainProbability;
    }
}

