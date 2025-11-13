package com.example.weatherapp.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.weatherapp.R;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.ui.views.charts.RainProbabilityChartView;
import com.example.weatherapp.ui.views.charts.TemperatureHeatmapView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * iOS-style Weather Detail Dialog
 * Displays:
 * - Temperature heatmap by hour
 * - Rain probability chart
 * - Weather metrics (humidity, wind, pressure, UV)
 */
public class WeatherDetailDialog extends Dialog {

    private HourlyForecastResponse.HourlyItem selectedHourData;
    private List<HourlyForecastResponse.HourlyItem> dayDataList;
    private String windSpeedUnit;

    // Views
    private TextView tvDialogTitle;
    private TextView tvDateTime;
    private TextView tvCurrentTemp;
    private TextView tvTempRange;
    private ImageView ivWeatherIcon;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvPressure;
    private TextView tvUVIndex;
    private ImageButton btnClose;
    private ImageButton btnToggle;
    
    private TemperatureHeatmapView heatmapTemperature;
    private RainProbabilityChartView chartRainProbability;

    public WeatherDetailDialog(@NonNull Context context, 
                               HourlyForecastResponse.HourlyItem selectedData,
                               List<HourlyForecastResponse.HourlyItem> dayData,
                               String windUnit) {
        super(context, R.style.IOSDialogTheme);
        this.selectedHourData = selectedData;
        this.dayDataList = dayData;
        this.windSpeedUnit = windUnit != null ? windUnit : "ms";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_weather_detail_ios);

        // Make dialog full width
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 
                                 ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvTempRange = findViewById(R.id.tvTempRange);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvPressure = findViewById(R.id.tvPressure);
        tvUVIndex = findViewById(R.id.tvUVIndex);
        btnClose = findViewById(R.id.btnClose);
        btnToggle = findViewById(R.id.btnToggle);
        
        heatmapTemperature = findViewById(R.id.heatmapTemperature);
        chartRainProbability = findViewById(R.id.chartRainProbability);
    }

    private void setupListeners() {
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        if (btnToggle != null) {
            btnToggle.setOnClickListener(v -> {
                // Toggle expand/collapse scroll view
                // You can implement expand/collapse animation here
            });
        }
    }

    private void loadData() {
        if (selectedHourData == null) return;

        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM, yyyy", new Locale("vi"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        Date date = new Date(selectedHourData.getDt() * 1000L);
        
        if (tvDateTime != null) {
            tvDateTime.setText(dateFormat.format(date));
        }

        // Current temperature
        if (tvCurrentTemp != null) {
            int temp = (int) Math.round(selectedHourData.getMain().getTemp());
            tvCurrentTemp.setText(temp + "°");
        }

        // Temperature range (find min/max from day data)
        if (dayDataList != null && !dayDataList.isEmpty()) {
            double minTemp = dayDataList.stream()
                .mapToDouble(d -> d.getMain().getTempMin())
                .min()
                .orElse(selectedHourData.getMain().getTempMin());
            double maxTemp = dayDataList.stream()
                .mapToDouble(d -> d.getMain().getTempMax())
                .max()
                .orElse(selectedHourData.getMain().getTempMax());
            
            if (tvTempRange != null) {
                tvTempRange.setText(String.format(Locale.getDefault(), 
                    "C:%d° T:%d°", 
                    (int) Math.round(maxTemp), 
                    (int) Math.round(minTemp)));
            }
        }

        // Weather icon
        if (ivWeatherIcon != null && selectedHourData.getWeather() != null 
            && !selectedHourData.getWeather().isEmpty()) {
            String weatherMain = selectedHourData.getWeather().get(0).getMain();
            int iconRes = getWeatherIconResource(weatherMain);
            ivWeatherIcon.setImageResource(iconRes);
        }

        // Humidity
        if (tvHumidity != null) {
            tvHumidity.setText(selectedHourData.getMain().getHumidity() + "%");
        }

        // Wind speed
        if (tvWindSpeed != null) {
            double windSpeed = selectedHourData.getWind().getSpeed();
            if ("kmh".equalsIgnoreCase(windSpeedUnit)) {
                windSpeed = windSpeed * 3.6;
                tvWindSpeed.setText(String.format(Locale.getDefault(), "%.0f km/h", windSpeed));
            } else {
                tvWindSpeed.setText(String.format(Locale.getDefault(), "%.1f m/s", windSpeed));
            }
        }

        // Pressure
        if (tvPressure != null) {
            tvPressure.setText(selectedHourData.getMain().getPressure() + " hPa");
        }

        // UV Index (if available)
        if (tvUVIndex != null) {
            // Note: UV index might not be in hourly data, may need separate API call
            tvUVIndex.setText("0");
        }

        // Load heatmap data
        if (heatmapTemperature != null && dayDataList != null) {
            heatmapTemperature.setData(dayDataList);
        }

        // Load rain probability chart
        if (chartRainProbability != null && dayDataList != null) {
            chartRainProbability.setData(dayDataList);
        }
    }

    private int getWeatherIconResource(String weatherMain) {
        switch (weatherMain.toLowerCase()) {
            case "clear":
                return R.drawable.ic_weather_sun;
            case "clouds":
                return R.drawable.ic_weather_cloud;
            case "rain":
            case "drizzle":
                return R.drawable.ic_weather_rain;
            case "thunderstorm":
                return R.drawable.ic_weather_storm;
            case "snow":
                return R.drawable.ic_weather_snow;
            case "mist":
            case "fog":
                return R.drawable.ic_weather_fog;
            default:
                return R.drawable.ic_weather_cloud;
        }
    }
}
