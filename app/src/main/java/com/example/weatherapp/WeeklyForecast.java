package com.example.weatherapp;

public class WeeklyForecast {
    private String day;
    private String icon;
    private int iconResource;
    private int temperature;
    private boolean isToday;
    private int rainProbability; // Rain probability percentage

    public WeeklyForecast(String day, String icon, int iconResource, int temperature, boolean isToday) {
        this.day = day;
        this.icon = icon;
        this.iconResource = iconResource;
        this.temperature = temperature;
        this.isToday = isToday;
        this.rainProbability = 0;
    }

    public WeeklyForecast(String day, String icon, int iconResource, int temperature, boolean isToday, int rainProbability) {
        this.day = day;
        this.icon = icon;
        this.iconResource = iconResource;
        this.temperature = temperature;
        this.isToday = isToday;
        this.rainProbability = rainProbability;
    }

    public String getDay() {
        return day;
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

    public boolean isToday() {
        return isToday;
    }

    public int getRainProbability() {
        return rainProbability;
    }

    public void setRainProbability(int rainProbability) {
        this.rainProbability = rainProbability;
    }
}
