package com.example.weatherapp.data.responses;

import com.example.weatherapp.data.models.WeatherAlert;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherAlertsResponse {
    @SerializedName("alerts")
    private List<WeatherAlert> alerts;

    public List<WeatherAlert> getAlerts() {
        return alerts;
    }

    public boolean hasAlerts() {
        return alerts != null && !alerts.isEmpty();
    }
}
