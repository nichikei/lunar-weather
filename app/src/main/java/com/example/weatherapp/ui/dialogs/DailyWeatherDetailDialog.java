package com.example.weatherapp.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * iOS-style Daily Weather Detail Dialog
 * Displays weather information for a selected day from 10-day forecast
 * Including hourly temperature heatmap and rain probability charts
 */
public class DailyWeatherDetailDialog extends Dialog {

    private ForecastData.DailyForecast dailyForecast;
    private List<HourlyForecastResponse.HourlyItem> hourlyData;

    // Views
    private TextView tvDialogTitle;
    private TextView tvDateTime;
    private TextView tvCurrentTemp;
    private TextView tvDescription;
    private ImageView ivWeatherIcon;
    private TextView tvRainProb;
    private ImageButton btnClose;
    
    // Chart views
    private LineChart temperatureChart;
    private LineChart rainChart;

    public DailyWeatherDetailDialog(@NonNull Context context, 
                                    ForecastData.DailyForecast dailyForecast,
                                    List<HourlyForecastResponse.HourlyItem> hourlyData) {
        super(context, R.style.IOSDialogTheme);
        this.dailyForecast = dailyForecast;
        this.hourlyData = hourlyData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.util.Log.d("DailyWeatherDialog", "onCreate started");
        android.util.Log.d("DailyWeatherDialog", "dailyForecast: " + (dailyForecast != null));
        android.util.Log.d("DailyWeatherDialog", "hourlyData: " + (hourlyData != null ? hourlyData.size() + " items" : "null"));
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_daily_weather_detail_ios);

        // Make dialog full width with max height
        if (getWindow() != null) {
            android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(params);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        setupListeners();
        loadData();
        
        android.util.Log.d("DailyWeatherDialog", "onCreate finished");
    }

    private void initViews() {
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvDescription = findViewById(R.id.tvDescription);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvRainProb = findViewById(R.id.tvRainProb);
        btnClose = findViewById(R.id.btnClose);
        
        // Chart views
        temperatureChart = findViewById(R.id.temperatureChart);
        rainChart = findViewById(R.id.rainChart);
        
        android.util.Log.d("DailyWeatherDialog", "initViews - temperatureChart: " + (temperatureChart != null));
        android.util.Log.d("DailyWeatherDialog", "initViews - rainChart: " + (rainChart != null));
    }

    private void setupListeners() {
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }

    private void loadData() {
        if (dailyForecast == null) return;

        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM, yyyy", new Locale("vi"));
        Date date = new Date(dailyForecast.getTimestamp() * 1000L);
        
        if (tvDateTime != null) {
            tvDateTime.setText(dateFormat.format(date));
        }

        // Temperature range
        if (tvCurrentTemp != null) {
            int avgTemp = (int) Math.round((dailyForecast.getTempMax() + dailyForecast.getTempMin()) / 2);
            tvCurrentTemp.setText(avgTemp + "°");
        }

        // Weather description
        if (tvDescription != null) {
            tvDescription.setText(dailyForecast.getWeatherDescription());
        }

        // Weather icon
        if (ivWeatherIcon != null) {
            String weatherIcon = dailyForecast.getWeatherIcon().toLowerCase();
            int iconRes = getWeatherIconResource(weatherIcon);
            ivWeatherIcon.setImageResource(iconRes);
        }

        // Rain probability (available)
        if (tvRainProb != null) {
            tvRainProb.setText(dailyForecast.getRainProbability() + "%");
        }
        
        // Load charts with hourly data
        loadCharts();
    }
    
    private void loadCharts() {
        android.util.Log.d("DailyWeatherDialog", "loadCharts - hourlyData: " + 
            (hourlyData != null ? hourlyData.size() + " items" : "null"));
        
        if (hourlyData == null || hourlyData.isEmpty()) {
            android.util.Log.w("DailyWeatherDialog", "No hourly data available for charts!");
            return;
        }
        
        int itemCount = Math.min(24, hourlyData.size());
        List<HourlyForecastResponse.HourlyItem> displayData = hourlyData.subList(0, itemCount);
        
        // Setup temperature chart
        setupTemperatureChart(displayData);
        
        // Setup rain probability chart
        setupRainChart(displayData);
    }
    
    private void setupTemperatureChart(List<HourlyForecastResponse.HourlyItem> data) {
        if (temperatureChart == null || data.isEmpty()) return;
        
        // Prepare data entries
        ArrayList<Entry> entries = new ArrayList<>();
        final ArrayList<String> hourLabels = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        for (int i = 0; i < data.size(); i++) {
            HourlyForecastResponse.HourlyItem item = data.get(i);
            float temp = (float) item.getMain().getTemp();
            entries.add(new Entry(i, temp));
            
            Date date = new Date(item.getDt() * 1000L);
            hourLabels.add(timeFormat.format(date));
        }
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Nhiệt độ");
        
        // iOS-style styling - Beautiful orange gradient
        dataSet.setColor(android.graphics.Color.parseColor("#FF9500")); // iOS Orange
        dataSet.setLineWidth(4f);
        dataSet.setCircleColor(android.graphics.Color.parseColor("#FF9500"));
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(3.5f);
        dataSet.setCircleHoleColor(android.graphics.Color.parseColor("#1C1C1E"));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curve like iOS
        dataSet.setCubicIntensity(0.15f);
        
        // Beautiful gradient fill under line
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(android.graphics.Color.parseColor("#FF9500"));
        dataSet.setFillAlpha(80);
        
        // Value formatter to show degree symbol
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f°", value);
            }
        });
        
        LineData lineData = new LineData(dataSet);
        temperatureChart.setData(lineData);
        
        // Chart styling - iOS dark theme
        temperatureChart.getDescription().setEnabled(false);
        temperatureChart.setDrawGridBackground(false);
        temperatureChart.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        temperatureChart.setTouchEnabled(true);
        temperatureChart.setDragEnabled(true);
        temperatureChart.setScaleEnabled(false);
        temperatureChart.setPinchZoom(false);
        temperatureChart.setExtraBottomOffset(15f);
        temperatureChart.setExtraTopOffset(20f);
        temperatureChart.getLegend().setEnabled(false);
        temperatureChart.setViewPortOffsets(40f, 30f, 40f, 60f);
        
        // X-axis styling - More labels for better readability
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(android.graphics.Color.parseColor("#AEAEB2"));
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(8, hourLabels.size()), false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < hourLabels.size()) {
                    return hourLabels.get(index);
                }
                return "";
            }
        });
        
        // Y-axis styling - Subtle grid
        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.setTextColor(android.graphics.Color.parseColor("#AEAEB2"));
        leftAxis.setTextSize(11f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(android.graphics.Color.parseColor("#3A3A3C"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setLabelCount(5, false);
        
        temperatureChart.getAxisRight().setEnabled(false);
        
        temperatureChart.animateX(800);
        temperatureChart.invalidate();
    }
    
    private void setupRainChart(List<HourlyForecastResponse.HourlyItem> data) {
        if (rainChart == null || data.isEmpty()) return;
        
        // Prepare data entries
        ArrayList<Entry> entries = new ArrayList<>();
        final ArrayList<String> hourLabels = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        for (int i = 0; i < data.size(); i++) {
            HourlyForecastResponse.HourlyItem item = data.get(i);
            float pop = (float) (item.getPop() * 100); // Convert to percentage
            entries.add(new Entry(i, pop));
            
            Date date = new Date(item.getDt() * 1000L);
            hourLabels.add(timeFormat.format(date));
        }
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Khả năng mưa");
        
        // iOS-style blue styling - Beautiful gradient
        dataSet.setColor(android.graphics.Color.parseColor("#007AFF")); // iOS Blue
        dataSet.setLineWidth(4f);
        dataSet.setCircleColor(android.graphics.Color.parseColor("#007AFF"));
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(3.5f);
        dataSet.setCircleHoleColor(android.graphics.Color.parseColor("#1C1C1E"));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);
        
        // Beautiful blue gradient fill
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(android.graphics.Color.parseColor("#007AFF"));
        dataSet.setFillAlpha(85);
        
        // Value formatter to show percentage
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 0) {
                    return String.format(Locale.getDefault(), "%.0f%%", value);
                }
                return "";
            }
        });
        
        LineData lineData = new LineData(dataSet);
        rainChart.setData(lineData);
        
        // Chart styling - iOS style
        rainChart.getDescription().setEnabled(false);
        rainChart.setDrawGridBackground(false);
        rainChart.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        rainChart.setTouchEnabled(true);
        rainChart.setDragEnabled(true);
        rainChart.setScaleEnabled(false);
        rainChart.setPinchZoom(false);
        rainChart.setExtraBottomOffset(15f);
        rainChart.setExtraTopOffset(20f);
        rainChart.getLegend().setEnabled(false);
        rainChart.setViewPortOffsets(40f, 30f, 40f, 60f);
        
        // X-axis styling
        XAxis xAxis = rainChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(android.graphics.Color.parseColor("#AEAEB2"));
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(8, hourLabels.size()), false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < hourLabels.size()) {
                    return hourLabels.get(index);
                }
                return "";
            }
        });
        
        // Y-axis styling
        YAxis leftAxis = rainChart.getAxisLeft();
        leftAxis.setTextColor(android.graphics.Color.parseColor("#AEAEB2"));
        leftAxis.setTextSize(11f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(android.graphics.Color.parseColor("#3A3A3C"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setLabelCount(5, false);
        
        rainChart.getAxisRight().setEnabled(false);
        
        rainChart.animateX(800);
        rainChart.invalidate();
    }

    private int getWeatherIconResource(String weatherCondition) {
        switch (weatherCondition.toLowerCase()) {
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
