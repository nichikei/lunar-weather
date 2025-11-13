package com.example.weatherapp.ui.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.data.responses.HourlyForecastResponse;

import java.util.ArrayList;
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
    
    // Store hourly data for dialog charts
    private List<HourlyForecastResponse.HourlyItem> hourlyDataForCharts;

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

        // Sort daily items by timestamp to ensure chronological order
        List<ForecastData.DailyForecast> sortedItems = new ArrayList<>(dailyItems);
        java.util.Collections.sort(sortedItems, new java.util.Comparator<ForecastData.DailyForecast>() {
            @Override
            public int compare(ForecastData.DailyForecast o1, ForecastData.DailyForecast o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });

        boolean isFirst = true;
        int itemsToShow = Math.min(10, sortedItems.size()); // Show 10 days
        android.util.Log.d("ForecastViewManager", "Daily forecast: total items=" + sortedItems.size() + ", showing=" + itemsToShow);

        for (int i = 0; i < itemsToShow; i++) {
            ForecastData.DailyForecast dailyForecast = sortedItems.get(i);

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
            tvTime.setText("Now");
        } else {
            int hour = itemTime.get(Calendar.HOUR_OF_DAY);
            // Use 12-hour format with AM/PM like iOS
            int hour12 = itemTime.get(Calendar.HOUR);
            if (hour12 == 0) hour12 = 12;
            String amPm = itemTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
            tvTime.setText(String.format(Locale.US, "%d%s", hour12, amPm));
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
            dayName = "Today";
        } else {
            // Check if it's tomorrow
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            
            if (calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                dayName = "Tomorrow";
            } else {
                // Use English day names (iOS style)
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                dayName = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            }
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

        // Add click listener to show detail dialog for this day
        itemView.setOnClickListener(v -> showDailyWeatherDetail(dailyForecast));
    }

    /**
     * Set hourly data for chart displays in daily detail dialog
     */
    public void setHourlyDataForCharts(List<HourlyForecastResponse.HourlyItem> hourlyData) {
        this.hourlyDataForCharts = hourlyData;
    }

    /**
     * Show weather detail dialog for selected day
     */
    private void showDailyWeatherDetail(ForecastData.DailyForecast dailyForecast) {
        android.util.Log.d("ForecastViewManager", "Show detail for day: " + dailyForecast.getTimestamp());
        
        // Show iOS-style detail dialog with hourly chart data
        com.example.weatherapp.ui.dialogs.DailyWeatherDetailDialog dialog = 
            new com.example.weatherapp.ui.dialogs.DailyWeatherDetailDialog(
                context, 
                dailyForecast,
                hourlyDataForCharts != null ? hourlyDataForCharts : new ArrayList<>()
            );
        dialog.show();
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
