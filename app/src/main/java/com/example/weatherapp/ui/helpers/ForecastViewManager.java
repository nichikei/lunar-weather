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
 * Manages forecast views (hourly and daily)
 * Handles creation and population of forecast items
 */
public class ForecastViewManager {

    private final Context context;
    private final LinearLayout hourlyForecastContainer;
    private final LinearLayout dailyForecastContainer;
    private final String temperatureUnit;

    // Old constructor for backward compatibility
    public ForecastViewManager(Context context, LinearLayout forecastContainer, String temperatureUnit) {
        this.context = context;
        this.hourlyForecastContainer = forecastContainer;
        this.dailyForecastContainer = forecastContainer;
        this.temperatureUnit = temperatureUnit;
    }

    // New constructor with separate containers
    public ForecastViewManager(Context context, LinearLayout hourlyContainer, LinearLayout dailyContainer, String temperatureUnit) {
        this.context = context;
        this.hourlyForecastContainer = hourlyContainer;
        this.dailyForecastContainer = dailyContainer;
        this.temperatureUnit = temperatureUnit;
    }

    /**
     * Create hourly forecast view from domain model (iOS style)
     */
    public void createHourlyForecastView(ForecastData forecastData) {
        hourlyForecastContainer.removeAllViews();

        List<ForecastData.HourlyForecast> hourlyItems = forecastData.getHourlyForecasts();
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return;
        }

        boolean isFirst = true;
        int itemsToShow = Math.min(24, hourlyItems.size()); // Show 24 hours

        for (int i = 0; i < itemsToShow; i++) {
            ForecastData.HourlyForecast item = hourlyItems.get(i);

            // Use iOS style layout
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_hourly_forecast_ios, hourlyForecastContainer, false);

            populateHourlyItemIOS(itemView, item, isFirst);
            hourlyForecastContainer.addView(itemView);
            
            if (isFirst) {
                isFirst = false;
            }
        }
    }

    /**
     * Create weekly/daily forecast view from domain model (iOS style - vertical list)
     */
    public void createWeeklyForecastView(ForecastData forecastData) {
        dailyForecastContainer.removeAllViews();

        List<ForecastData.DailyForecast> dailyItems = forecastData.getDailyForecasts();
        if (dailyItems == null || dailyItems.isEmpty()) {
            android.util.Log.w("ForecastViewManager", "Daily forecast data is null or empty");
            return;
        }

        boolean isFirst = true;
        int itemsToShow = Math.min(10, dailyItems.size()); // Show 10 days
        android.util.Log.d("ForecastViewManager", "Daily forecast: total items=" + dailyItems.size() + ", showing=" + itemsToShow);

        for (int i = 0; i < itemsToShow; i++) {
            ForecastData.DailyForecast dailyForecast = dailyItems.get(i);

            // Use iOS style layout
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_daily_forecast_ios, dailyForecastContainer, false);

            populateDailyItemIOS(itemView, dailyForecast, isFirst);
            dailyForecastContainer.addView(itemView);
            
            if (isFirst) {
                isFirst = false;
            }
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

    /**
     * Populate hourly item with iOS style layout
     */
    private void populateHourlyItemIOS(View itemView, ForecastData.HourlyForecast item, boolean isFirst) {
        TextView tvTime = itemView.findViewById(R.id.tvTime);
        ImageView ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        TextView tvTemperature = itemView.findViewById(R.id.tvTemperature);

        // Format time
        Calendar itemTime = Calendar.getInstance();
        itemTime.setTimeInMillis(item.getTimestamp() * 1000);

        if (isFirst) {
            tvTime.setText("Bây giờ");
        } else {
            int hour = itemTime.get(Calendar.HOUR_OF_DAY);
            tvTime.setText(String.format(Locale.getDefault(), "%d giờ", hour));
        }

        // Set weather icon
        String weatherIcon = item.getWeatherIcon().toLowerCase();
        ivWeatherIcon.setImageResource(getWeatherIconResource(weatherIcon));

        // Set temperature
        int temp = (int) Math.round(item.getTemperature());
        tvTemperature.setText(String.format(Locale.getDefault(), "%d°", temp));
    }

    /**
     * Populate daily item with iOS style layout (vertical list)
     */
    private void populateDailyItemIOS(View itemView, ForecastData.DailyForecast dailyForecast, boolean isFirst) {
        TextView tvDayName = itemView.findViewById(R.id.tvDayName);
        ImageView ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        TextView tvLowTemp = itemView.findViewById(R.id.tvLowTemp);
        TextView tvHighTemp = itemView.findViewById(R.id.tvHighTemp);
        View divider = itemView.findViewById(R.id.divider);

        // Format day name
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dailyForecast.getTimestamp() * 1000);
        
        String dayName;
        if (isFirst) {
            dayName = "Hôm nay";
        } else {
            String[] days = {"CN", "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7"};
            dayName = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        }

        tvDayName.setText(dayName);
        ivWeatherIcon.setImageResource(getWeatherIconResource(dailyForecast.getWeatherIcon().toLowerCase()));
        
        // Set temperatures
        int lowTemp = (int) Math.round(dailyForecast.getTempMin());
        int highTemp = (int) Math.round(dailyForecast.getTempMax());
        tvLowTemp.setText(String.format(Locale.getDefault(), "%d°", lowTemp));
        tvHighTemp.setText(String.format(Locale.getDefault(), "%d°", highTemp));

        // Hide divider for last item (will be handled by parent)
        if (divider != null) {
            divider.setVisibility(View.VISIBLE);
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
