package com.example.weatherapp.data.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AirQualityResponse {
    @SerializedName("list")
    private List<AirQualityData> list;

    public List<AirQualityData> getList() {
        return list;
    }

    public static class AirQualityData {
        @SerializedName("main")
        private Main main;

        @SerializedName("components")
        private Components components;

        public Main getMain() {
            return main;
        }

        public Components getComponents() {
            return components;
        }
    }

    public static class Main {
        @SerializedName("aqi")
        private int aqi; // 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor

        public int getAqi() {
            return aqi;
        }
    }

    public static class Components {
        @SerializedName("co")
        private double co; // Carbon monoxide

        @SerializedName("no")
        private double no; // Nitrogen monoxide

        @SerializedName("no2")
        private double no2; // Nitrogen dioxide

        @SerializedName("o3")
        private double o3; // Ozone

        @SerializedName("so2")
        private double so2; // Sulphur dioxide

        @SerializedName("pm2_5")
        private double pm2_5; // Fine particles matter

        @SerializedName("pm10")
        private double pm10; // Coarse particulate matter

        @SerializedName("nh3")
        private double nh3; // Ammonia

        public double getCo() {
            return co;
        }

        public double getNo() {
            return no;
        }

        public double getNo2() {
            return no2;
        }

        public double getO3() {
            return o3;
        }

        public double getSo2() {
            return so2;
        }

        public double getPm2_5() {
            return pm2_5;
        }

        public double getPm10() {
            return pm10;
        }

        public double getNh3() {
            return nh3;
        }
    }
}


