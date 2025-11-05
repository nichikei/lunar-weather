package com.example.weatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ChartsActivity extends AppCompatActivity {

    private HourlyForecastResponse hourlyForecastData;
    private WeatherResponse currentWeatherData;
    private int currentUVIndex;
    private String windSpeedUnit = "ms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        // Get data from intent
        hourlyForecastData = (HourlyForecastResponse) getIntent().getSerializableExtra("hourly_data");
        currentWeatherData = (WeatherResponse) getIntent().getSerializableExtra("current_data");
        currentUVIndex = getIntent().getIntExtra("uv_index", 0);

        // Load settings
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        windSpeedUnit = SettingsActivity.getWindSpeedUnit(prefs);

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Update title
        TextView tvTitle = findViewById(R.id.tvChartTitle);
        if (tvTitle != null && currentWeatherData != null) {
            tvTitle.setText(currentWeatherData.getName() + " - " + getString(R.string.weather_statistics));
        }

        // Setup all charts
        setupTemperatureChart();
        setupWeatherStatsChart();
        setupRainProbabilityChart();
        setupWindSpeedChart();
        setupHumidityChart();
    }

    private void setupTemperatureChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) {
            return;
        }

        LineChart chart = findViewById(R.id.temperatureChart);
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();

        int count = Math.min(12, hourlyForecastData.getList().size());
        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            float temp = (float) item.getMain().getTemp();
            entries.add(new Entry(i, temp));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        // Unified purple gradient theme
        dataSet.setColor(0xFF9B6FFF); // Soft purple
        dataSet.setCircleColor(0xFFE2DDFD);
        dataSet.setLineWidth(3.5f);
        dataSet.setCircleRadius(6f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(0xFF5B3E9E);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0xFF7B5EC6);
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart);
        chart.animateXY(1200, 1200);
        chart.invalidate();
    }

    private void setupWeatherStatsChart() {
        if (currentWeatherData == null) return;

        BarChart chart = findViewById(R.id.weatherStatsChart);
        if (chart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, currentWeatherData.getMain().getHumidity()));
        entries.add(new BarEntry(1, (float) currentWeatherData.getWind().getSpeed() * 3.6f));
        entries.add(new BarEntry(2, currentWeatherData.getMain().getPressure() / 10f));
        entries.add(new BarEntry(3, currentUVIndex * 10f));

        BarDataSet dataSet = new BarDataSet(entries, "Weather Stats");
        // Unified gradient color scheme - cyan to purple
        int[] colors = {0xFF4FC3F7, 0xFF29B6F6, 0xFFFFB347, 0xFFFF6B9D};
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setHighLightAlpha(255);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);

        setupBarChart(chart);
        chart.animateY(1200);
        chart.invalidate();
    }

    private void setupRainProbabilityChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.rainProbabilityChart);
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        int count = Math.min(12, hourlyForecastData.getList().size());
        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            float rainProb = (float) (item.getPop() * 100);
            entries.add(new Entry(i, rainProb));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rain Probability");
        // Unified blue theme for water
        dataSet.setColor(0xFF4FC3F7);
        dataSet.setCircleColor(0xFF81D4FA);
        dataSet.setLineWidth(3.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(0xFF29B6F6);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0xFF4FC3F7);
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.animateXY(1200, 1200);
        chart.invalidate();
    }

    private void setupWindSpeedChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.windSpeedChart);
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        int count = Math.min(12, hourlyForecastData.getList().size());
        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            float windSpeed = (float) item.getWind().getSpeed();

            if (windSpeedUnit.equals("kmh")) {
                windSpeed = windSpeed * 3.6f;
            }
            entries.add(new Entry(i, windSpeed));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Wind Speed");
        // Unified green theme for wind
        dataSet.setColor(0xFF66BB6A);
        dataSet.setCircleColor(0xFF81C784);
        dataSet.setLineWidth(3.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(0xFF4CAF50);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0xFF66BB6A);
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart);
        chart.animateXY(1200, 1200);
        chart.invalidate();
    }

    private void setupHumidityChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.humidityChart);
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        int count = Math.min(12, hourlyForecastData.getList().size());
        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            float humidity = (float) item.getMain().getHumidity();
            entries.add(new Entry(i, humidity));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Humidity");
        // Unified cyan theme for humidity
        dataSet.setColor(0xFF26C6DA);
        dataSet.setCircleColor(0xFF4DD0E1);
        dataSet.setLineWidth(3.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(0xFF00BCD4);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0xFF26C6DA);
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.animateXY(1200, 1200);
        chart.invalidate();
    }

    private void setupChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextColor(0xCCFFFFFF); // Slightly transparent white
        chart.getXAxis().setTextSize(11f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setAxisLineColor(0x40FFFFFF);
        chart.getXAxis().setAxisLineWidth(1.5f);
        chart.getAxisLeft().setTextColor(0xCCFFFFFF);
        chart.getAxisLeft().setTextSize(11f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(0x30FFFFFF);
        chart.getAxisLeft().setGridLineWidth(1f);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setExtraOffsets(8, 16, 8, 8); // Better spacing
    }

    private void setupBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextColor(0xCCFFFFFF);
        chart.getXAxis().setTextSize(11f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setAxisLineColor(0x40FFFFFF);
        chart.getXAxis().setAxisLineWidth(1.5f);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            private final String[] labels = {"Humidity", "Wind", "Pressure", "UV Index"};
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < labels.length ? labels[index] : "";
            }
        });
        chart.getAxisLeft().setTextColor(0xCCFFFFFF);
        chart.getAxisLeft().setTextSize(11f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(0x30FFFFFF);
        chart.getAxisLeft().setGridLineWidth(1f);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.setTouchEnabled(false);
        chart.setFitBars(true);
        chart.setExtraOffsets(8, 16, 8, 8); // Better spacing
    }
}
