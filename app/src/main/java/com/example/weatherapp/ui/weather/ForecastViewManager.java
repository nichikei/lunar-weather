package com.example.weatherapp.ui.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weatherapp.R;
import com.example.weatherapp.data.responses.HourlyForecastResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Manages forecast views (hourly and weekly)
 * Handles creation and population of forecast items
 */
public class ForecastViewManager {

    private final Context context;
    private final LinearLayout forecastContainer;
    private final String temperatureUnit;

    public ForecastViewManager(Context context, LinearLayout forecastContainer, String temperatureUnit) {
        this.context = context;
        this.forecastContainer = forecastContainer;
        this.temperatureUnit = temperatureUnit;
    }

    /**
     * Create hourly forecast view from API data
     */
    public void createHourlyForecastView(HourlyForecastResponse forecastData) {
        forecastContainer.removeAllViews();

        List<HourlyForecastResponse.HourlyItem> hourlyItems = forecastData.getList();
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return;
        }

        boolean isFirst = true;
        int itemsToShow = Math.min(8, hourlyItems.size());

        for (int i = 0; i < itemsToShow; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyItems.get(i);

            View itemView;
            if (isFirst) {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_hourly_forecast_now, forecastContainer, false);
                isFirst = false;
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_hourly_forecast, forecastContainer, false);
            }

            populateHourlyItem(itemView, item, i);
            forecastContainer.addView(itemView);
        }
    }

    /**
     * Create weekly forecast view from API data
     */
    public void createWeeklyForecastView(HourlyForecastResponse forecastData) {
        forecastContainer.removeAllViews();

        List<HourlyForecastResponse.HourlyItem> hourlyItems = forecastData.getList();
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return;
        }

        List<DailyData> dailyDataList = groupByDay(hourlyItems);
        boolean isFirst = true;
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";

        for (DailyData dailyData : dailyDataList) {
            View itemView;

            if (isFirst) {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_weekly_forecast_today, forecastContainer, false);
                isFirst = false;
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_weekly_forecast, forecastContainer, false);
            }

            populateWeeklyItem(itemView, dailyData, tempSymbol);
            forecastContainer.addView(itemView);
        }
    }

    // ============ Private Helper Methods ============

    private void populateHourlyItem(View itemView, HourlyForecastResponse.HourlyItem item, int position) {
        TextView txtHour = itemView.findViewById(R.id.tvHour);
        ImageView ivHourlyIcon = itemView.findViewById(R.id.ivHourlyIcon);
        TextView tvHourlyTemp = itemView.findViewById(R.id.tvHourlyTemp);
        TextView tvRainProbability = itemView.findViewById(R.id.tvRainProbability);

        // Format time
        Calendar itemTime = Calendar.getInstance();
        itemTime.setTimeInMillis(item.getDt() * 1000);

        if (position == 0) {
            txtHour.setText("Now");
        } else {
            int hour = itemTime.get(Calendar.HOUR);
            if (hour == 0) hour = 12;
            String amPm = itemTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
            txtHour.setText(hour + amPm);
        }

        // Set weather icon
        String weatherCondition = item.getWeather().get(0).getMain().toLowerCase();
        ivHourlyIcon.setImageResource(getWeatherIconResource(weatherCondition));

        // Set temperature
        int temp = (int) Math.round(item.getMain().getTemp());
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";
        tvHourlyTemp.setText(String.format(Locale.getDefault(), "%d%s", temp, tempSymbol));

        // Set rain probability
        if (tvRainProbability != null) {
            int rainProb = (int) (item.getPop() * 100);
            if (rainProb > 0 && position > 0) {
                tvRainProbability.setText(String.format(Locale.getDefault(), "%d%%", rainProb));
                tvRainProbability.setVisibility(View.VISIBLE);
            } else {
                tvRainProbability.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void populateWeeklyItem(View itemView, DailyData dailyData, String tempSymbol) {
        TextView tvDay = itemView.findViewById(R.id.tvDay);
        ImageView ivWeeklyIcon = itemView.findViewById(R.id.ivWeeklyIcon);
        TextView tvWeeklyTemp = itemView.findViewById(R.id.tvWeeklyTemp);
        TextView tvWeeklyRainProbability = itemView.findViewById(R.id.tvWeeklyRainProbability);

        tvDay.setText(dailyData.dayName);
        ivWeeklyIcon.setImageResource(getWeatherIconResource(dailyData.mainWeatherCondition));
        tvWeeklyTemp.setText(String.format(Locale.getDefault(), "%d%s", dailyData.maxTemp, tempSymbol));

        if (tvWeeklyRainProbability != null) {
            if (dailyData.maxRainProbability > 0) {
                tvWeeklyRainProbability.setText(String.format(Locale.getDefault(), "%d%%", dailyData.maxRainProbability));
                tvWeeklyRainProbability.setVisibility(View.VISIBLE);
            } else {
                tvWeeklyRainProbability.setVisibility(View.INVISIBLE);
            }
        }
    }

    private List<DailyData> groupByDay(List<HourlyForecastResponse.HourlyItem> hourlyItems) {
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        Calendar calendar = Calendar.getInstance();
        List<DailyData> dailyDataList = new ArrayList<>();
        String currentDay = "";
        DailyData currentDailyData = null;

        for (HourlyForecastResponse.HourlyItem item : hourlyItems) {
            calendar.setTimeInMillis(item.getDt() * 1000);
            String dayKey = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

            if (!dayKey.equals(currentDay)) {
                if (currentDailyData != null) {
                    dailyDataList.add(currentDailyData);
                }
                currentDailyData = new DailyData(dayKey);
                currentDay = dayKey;
            }

            currentDailyData.addItem(item);

            if (dailyDataList.size() >= 7) break;
        }

        if (currentDailyData != null && dailyDataList.size() < 7) {
            dailyDataList.add(currentDailyData);
        }

        return dailyDataList;
    }

    private int getWeatherIconResource(String weatherCondition) {
        switch (weatherCondition) {
            case "clear":
                return R.drawable.sun_cloud_little_rain;
            case "clouds":
                return R.drawable.moon_cloud_mid_rain;
            case "rain":
                return R.drawable.big_rain_drops;
            case "drizzle":
                return R.drawable.sun_cloud_mid_rain;
            case "thunderstorm":
                return R.drawable.cloud_3_zap;
            case "snow":
                return R.drawable.big_snow;
            case "mist":
            case "fog":
            case "haze":
                return R.drawable.moon_cloud_fast_wind;
            case "tornado":
                return R.drawable.moon_fast_wind;
            default:
                return R.drawable.sun_cloud_angled_rain;
        }
    }

    // ============ Inner Class ============

    private static class DailyData {
        String dayName;
        int maxTemp = Integer.MIN_VALUE;
        int maxRainProbability = 0;
        String mainWeatherCondition = "clear";

        DailyData(String dayName) {
            this.dayName = dayName;
        }

        void addItem(HourlyForecastResponse.HourlyItem item) {
            int temp = (int) Math.round(item.getMain().getTemp());
            if (temp > maxTemp) {
                maxTemp = temp;
                mainWeatherCondition = item.getWeather().get(0).getMain().toLowerCase();
            }

            int rainProb = (int) (item.getPop() * 100);
            if (rainProb > maxRainProbability) {
                maxRainProbability = rainProb;
            }
        }
    }
}
