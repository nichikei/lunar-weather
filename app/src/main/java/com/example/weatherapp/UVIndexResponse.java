package com.example.weatherapp;

import com.google.gson.annotations.SerializedName;

public class UVIndexResponse {
    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("date_iso")
    private String dateIso;

    @SerializedName("date")
    private long date;

    @SerializedName("value")
    private double value;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getDateIso() {
        return dateIso;
    }

    public long getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}

