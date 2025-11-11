package com.example.weatherapp.ui.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.ForecastData;

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
     * Create hourly forecast view from domain model
     */
    public void createHourlyForecastView(ForecastData forecastData) {
        forecastContainer.removeAllViews();

        List<ForecastData.HourlyForecast> hourlyItems = forecastData.getHourlyForecasts();
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return;
        }

        boolean isFirst = true;
        int itemsToShow = Math.min(8, hourlyItems.size());

        for (int i = 0; i < itemsToShow; i++) {
            ForecastData.HourlyForecast item = hourlyItems.get(i);

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
     * Create weekly forecast view from domain model
     */
    public void createWeeklyForecastView(ForecastData forecastData) {
        forecastContainer.removeAllViews();

        List<ForecastData.DailyForecast> dailyItems = forecastData.getDailyForecasts();
        if (dailyItems == null || dailyItems.isEmpty()) {
            return;
        }

        boolean isFirst = true;
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";

        for (ForecastData.DailyForecast dailyForecast : dailyItems) {
            View itemView;

            if (isFirst) {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_weekly_forecast_today, forecastContainer, false);
                isFirst = false;
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.item_weekly_forecast, forecastContainer, false);
            }

            populateWeeklyItem(itemView, dailyForecast, tempSymbol);
            forecastContainer.addView(itemView);
        }
    }

    // ============ Private Helper Methods ============

    private void populateHourlyItem(View itemView, ForecastData.HourlyForecast item, int position) {
        TextView txtHour = itemView.findViewById(R.id.tvHour);
        ImageView ivHourlyIcon = itemView.findViewById(R.id.ivHourlyIcon);
        TextView tvHourlyTemp = itemView.findViewById(R.id.tvHourlyTemp);
        TextView tvRainProbability = itemView.findViewById(R.id.tvRainProbability);

        // Format time
        Calendar itemTime = Calendar.getInstance();
        itemTime.setTimeInMillis(item.getTimestamp() * 1000);

        if (position == 0) {
            txtHour.setText("Now");
        } else {
            int hour = itemTime.get(Calendar.HOUR);
            if (hour == 0) hour = 12;
            String amPm = itemTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
            txtHour.setText(hour + amPm);
        }

        // Set weather icon
        String weatherIcon = item.getWeatherIcon().toLowerCase();
        ivHourlyIcon.setImageResource(getWeatherIconResource(weatherIcon));

        // Set temperature
        int temp = (int) Math.round(item.getTemperature());
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";
        tvHourlyTemp.setText(String.format(Locale.getDefault(), "%d%s", temp, tempSymbol));

        // Set rain probability
        if (tvRainProbability != null) {
            int rainProb = item.getRainProbability();
            if (rainProb > 0 && position > 0) {
                tvRainProbability.setText(String.format(Locale.getDefault(), "%d%%", rainProb));
                tvRainProbability.setVisibility(View.VISIBLE);
            } else {
                tvRainProbability.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void populateWeeklyItem(View itemView, ForecastData.DailyForecast dailyForecast, String tempSymbol) {
        TextView tvDay = itemView.findViewById(R.id.tvDay);
        ImageView ivWeeklyIcon = itemView.findViewById(R.id.ivWeeklyIcon);
        TextView tvWeeklyTemp = itemView.findViewById(R.id.tvWeeklyTemp);
        TextView tvWeeklyRainProbability = itemView.findViewById(R.id.tvWeeklyRainProbability);

        // Format day name
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dailyForecast.getTimestamp() * 1000);
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        String dayName = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        tvDay.setText(dayName);
        ivWeeklyIcon.setImageResource(getWeatherIconResource(dailyForecast.getWeatherIcon().toLowerCase()));
        tvWeeklyTemp.setText(String.format(Locale.getDefault(), "%.0f%s", dailyForecast.getTempMax(), tempSymbol));

        if (tvWeeklyRainProbability != null) {
            int rainProb = dailyForecast.getRainProbability();
            if (rainProb > 0) {
                tvWeeklyRainProbability.setText(String.format(Locale.getDefault(), "%d%%", rainProb));
                tvWeeklyRainProbability.setVisibility(View.VISIBLE);
            } else {
                tvWeeklyRainProbability.setVisibility(View.INVISIBLE);
            }
        }
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
}
