package com.example.weatherapp.data.responses;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class HourlyForecastResponse implements Serializable {
    @SerializedName("list")
    private List<HourlyItem> list;

    @SerializedName("city")
    private City city;

    public List<HourlyItem> getList() {
        return list;
    }

    public City getCity() {
        return city;
    }

    public static class HourlyItem implements Serializable {
        @SerializedName("dt")
        private long dt; // Unix timestamp

        @SerializedName("main")
        private Main main;

        @SerializedName("weather")
        private List<Weather> weather;

        @SerializedName("clouds")
        private Clouds clouds;

        @SerializedName("wind")
        private Wind wind;

        @SerializedName("pop")
        private double pop; // Probability of precipitation (0-1)

        @SerializedName("rain")
        private Rain rain;

        public long getDt() {
            return dt;
        }

        public Main getMain() {
            return main;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public Clouds getClouds() {
            return clouds;
        }

        public Wind getWind() {
            return wind;
        }

        public double getPop() {
            return pop;
        }

        public Rain getRain() {
            return rain;
        }

        public static class Main implements Serializable {
            @SerializedName("temp")
            private double temp;

            @SerializedName("feels_like")
            private double feelsLike;

            @SerializedName("temp_min")
            private double tempMin;

            @SerializedName("temp_max")
            private double tempMax;

            @SerializedName("pressure")
            private int pressure;

            @SerializedName("humidity")
            private int humidity;

            public double getTemp() {
                return temp;
            }

            public double getFeelsLike() {
                return feelsLike;
            }

            public double getTempMin() {
                return tempMin;
            }

            public double getTempMax() {
                return tempMax;
            }

            public int getPressure() {
                return pressure;
            }

            public int getHumidity() {
                return humidity;
            }
        }

        public static class Weather implements Serializable {
            @SerializedName("id")
            private int id;

            @SerializedName("main")
            private String main;

            @SerializedName("description")
            private String description;

            @SerializedName("icon")
            private String icon;

            public int getId() {
                return id;
            }

            public String getMain() {
                return main;
            }

            public String getDescription() {
                return description;
            }

            public String getIcon() {
                return icon;
            }
        }

        public static class Clouds implements Serializable {
            @SerializedName("all")
            private int all;

            public int getAll() {
                return all;
            }
        }

        public static class Wind implements Serializable {
            @SerializedName("speed")
            private double speed;

            @SerializedName("deg")
            private int deg;

            public double getSpeed() {
                return speed;
            }

            public int getDeg() {
                return deg;
            }
        }

        public static class Rain implements Serializable {
            @SerializedName("3h")
            private Double threeHours;

            public Double get3h() {
                return threeHours;
            }
        }
    }

    public static class City implements Serializable {
        @SerializedName("name")
        private String name;

        @SerializedName("country")
        private String country;

        @SerializedName("timezone")
        private int timezone;

        @SerializedName("sunrise")
        private long sunrise;

        @SerializedName("sunset")
        private long sunset;

        public String getName() {
            return name;
        }

        public String getCountry() {
            return country;
        }

        public int getTimezone() {
            return timezone;
        }

        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }
    }
}

