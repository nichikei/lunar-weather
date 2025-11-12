package com.example.weatherapp.domain.model;

/**
 * Domain model for Air Quality data
 */
public class AirQualityData {
    private final int aqi; // Air Quality Index (1-5)
    private final double co; // Carbon Monoxide
    private final double no; // Nitrogen Monoxide
    private final double no2; // Nitrogen Dioxide
    private final double o3; // Ozone
    private final double so2; // Sulfur Dioxide
    private final double pm2_5; // Fine particles matter
    private final double pm10; // Coarse particulate matter
    private final double nh3; // Ammonia
    private final String aqiDescription;
    private final String aqiColor;
    
    public AirQualityData(int aqi, double co, double no, double no2, double o3, 
                          double so2, double pm2_5, double pm10, double nh3) {
        this.aqi = aqi;
        this.co = co;
        this.no = no;
        this.no2 = no2;
        this.o3 = o3;
        this.so2 = so2;
        this.pm2_5 = pm2_5;
        this.pm10 = pm10;
        this.nh3 = nh3;
        this.aqiDescription = getAQIDescription(aqi);
        this.aqiColor = getAQIColor(aqi);
    }
    
    // Getters
    public int getAqi() { return aqi; }
    public double getCo() { return co; }
    public double getNo() { return no; }
    public double getNo2() { return no2; }
    public double getO3() { return o3; }
    public double getSo2() { return so2; }
    public double getPm2_5() { return pm2_5; }
    public double getPm10() { return pm10; }
    public double getNh3() { return nh3; }
    public String getAqiDescription() { return aqiDescription; }
    public String getAqiColor() { return aqiColor; }
    
    /**
     * Get AQI description based on index
     */
    private String getAQIDescription(int aqi) {
        switch (aqi) {
            case 1: return "Good";
            case 2: return "Fair";
            case 3: return "Moderate";
            case 4: return "Poor";
            case 5: return "Very Poor";
            default: return "Unknown";
        }
    }
    
    /**
     * Get AQI color based on index
     */
    private String getAQIColor(int aqi) {
        switch (aqi) {
            case 1: return "#00E400"; // Green
            case 2: return "#FFFF00"; // Yellow
            case 3: return "#FF7E00"; // Orange
            case 4: return "#FF0000"; // Red
            case 5: return "#8F3F97"; // Purple
            default: return "#808080"; // Gray
        }
    }
    
    /**
     * Get main pollutant based on highest value
     */
    public String getMainPollutant() {
        double maxValue = Math.max(Math.max(Math.max(pm2_5, pm10), Math.max(o3, no2)), Math.max(so2, co));
        
        if (maxValue == pm2_5) return "PM2.5";
        if (maxValue == pm10) return "PM10";
        if (maxValue == o3) return "O3";
        if (maxValue == no2) return "NO2";
        if (maxValue == so2) return "SO2";
        if (maxValue == co) return "CO";
        
        return "Unknown";
    }
    
    /**
     * Convert AQI (1-5 scale) to US AQI (0-500 scale)
     */
    public int getUSAQI() {
        switch (aqi) {
            case 1: return 50;   // Good: 0-50
            case 2: return 100;  // Fair: 51-100
            case 3: return 150;  // Moderate: 101-150
            case 4: return 200;  // Poor: 151-200
            case 5: return 300;  // Very Poor: 201-300
            default: return 0;
        }
    }
}
