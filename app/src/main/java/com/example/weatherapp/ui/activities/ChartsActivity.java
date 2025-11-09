package com.example.weatherapp.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.ui.charts.ChartHelper;
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

    // Dữ liệu dự báo theo giờ (từ API OpenWeatherMap)
    private HourlyForecastResponse hourlyForecastData;

    // Dữ liệu thời tiết hiện tại
    private WeatherResponse currentWeatherData;

    // Chỉ số UV hiện tại
    private int currentUVIndex;

    // Đơn vị tốc độ gió (m/s hoặc km/h)
    private String windSpeedUnit = "ms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        // Nhận dữ liệu từ MainActivity qua Intent
        hourlyForecastData = (HourlyForecastResponse) getIntent().getSerializableExtra("hourly_data");
        currentWeatherData = (WeatherResponse) getIntent().getSerializableExtra("current_data");
        currentUVIndex = getIntent().getIntExtra("uv_index", 0);

        // Load cài đặt đơn vị từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        windSpeedUnit = SettingsActivity.getWindSpeedUnit(prefs);

        // Setup nút Back để quay lại màn hình trước
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Cập nhật tiêu đề với tên thành phố
        TextView tvTitle = findViewById(R.id.tvChartTitle);
        if (tvTitle != null && currentWeatherData != null) {
            tvTitle.setText(currentWeatherData.getName() + " - " + getString(R.string.weather_statistics));
        }

        // Khởi tạo tất cả các biểu đồ
        setupTemperatureChart();      // Biểu đồ nhiệt độ
        setupWeatherStatsChart();     // Biểu đồ các chỉ số thời tiết
        setupRainProbabilityChart();  // Biểu đồ xác suất mưa
        setupWindSpeedChart();        // Biểu đồ tốc độ gió
        setupHumidityChart();         // Biểu đồ độ ẩm
    }


    private void setupTemperatureChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.temperatureChart);
        if (chart == null) return;

        int count = Math.min(9, hourlyForecastData.getList().size());
        
        // Prepare data
        List<Entry> entries = ChartHelper.prepareChartEntries(
            hourlyForecastData, 
            count, 
            item -> (float) item.getMain().getTemp()
        );

        // Create and style dataset
        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        ChartHelper.styleTemperatureDataSet(dataSet, ChartHelper.ChartColors.TEMPERATURE);

        // Setup chart
        chart.setData(new LineData(dataSet));
        ChartHelper.setupLineChart(chart);
        ChartHelper.applyTimeFormatter(chart, hourlyForecastData, count);

        chart.animateXY(1200, 1200);
        chart.invalidate();
    }


    private void setupWeatherStatsChart() {
        if (currentWeatherData == null) return;

        // Tìm biểu đồ cột trong layout
        BarChart chart = findViewById(R.id.weatherStatsChart);
        if (chart == null) return;

        // Lấy các giá trị thực từ dữ liệu thời tiết
        final float humidityValue = currentWeatherData.getMain().getHumidity();

        float windSpeed = (float) currentWeatherData.getWind().getSpeed();
        // Chuyển đổi đơn vị gió nếu cần (m/s -> km/h)
        if (windSpeedUnit.equals("kmh")) {
            windSpeed = windSpeed * 3.6f;  // 1 m/s = 3.6 km/h
        }
        final float windValue = windSpeed;

        final float pressureValue = currentWeatherData.getMain().getPressure();
        final float uvValue = currentUVIndex;

        // Tạo danh sách các cột (BarEntry)
        List<BarEntry> entries = new ArrayList<>();

        // === THÊM 4 CỘT VÀO BIỂU ĐỒ ===

        // Cột 1: Độ ẩm (0-100%)
        // BarEntry(vị trí cột, chiều cao cột)
        entries.add(new BarEntry(0, humidityValue));

        // Cột 2: Tốc độ gió (km/h hoặc m/s)
        entries.add(new BarEntry(1, windValue));

        // Cột 3: Áp suất (chia 10 để cột không quá cao so với các cột khác)
        // VD: 1013 hPa / 10 = 101.3 (hiển thị lại 1013 hPa khi format)
        entries.add(new BarEntry(2, pressureValue / 10f));

        // Cột 4: Chỉ số UV (nhân 10 để cột không quá thấp)
        // VD: UV 5 * 10 = 50 (hiển thị lại UV 5 khi format)
        entries.add(new BarEntry(3, uvValue * 10f));

        // Tạo DataSet cho biểu đồ cột
        BarDataSet dataSet = new BarDataSet(entries, "Chỉ số thời tiết hiện tại");

        // === MÀU SẮC CHO 4 CỘT ===
        int[] colors = {
                0xFF4FC3F7,  // Xanh dương - Độ ẩm (nước)
                0xFF66BB6A,  // Xanh lá - Gió
                0xFFFFB347,  // Cam - Áp suất
                0xFFFF6B9D   // Hồng - UV (nguy hiểm)
        };
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);          // Kích thước chữ trên cột
        dataSet.setValueTextColor(0xFFFFFFFF);  // Màu chữ - Trắng
        dataSet.setHighLightAlpha(255);         // Độ sáng khi chạm vào cột

        // === FORMATTER: HIỂN THỊ GIÁ TRỊ + ĐỐN VỊ TRÊN MỖI CỘT ===
        final String windUnit = windSpeedUnit.equals("kmh") ? "km/h" : "m/s";
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Method này hiển thị giá trị mặc định
                return String.format(java.util.Locale.getDefault(), "%.0f", value);
            }

            @Override
            public String getBarLabel(BarEntry barEntry) {
                // Method này custom label cho từng cột cụ thể
                int index = (int) barEntry.getX();  // Lấy vị trí cột (0, 1, 2, 3)
                float value = barEntry.getY();      // Lấy chiều cao cột

                switch (index) {
                    case 0: // Cột 1 - Độ ẩm
                        return String.format(java.util.Locale.getDefault(), "%.0f%%", value);

                    case 1: // Cột 2 - Tốc độ gió
                        return String.format(java.util.Locale.getDefault(), "%.1f\n%s", value, windUnit);

                    case 2: // Cột 3 - Áp suất (nhân lại 10 để có giá trị thực)
                        return String.format(java.util.Locale.getDefault(), "%.0f\nhPa", value * 10);

                    case 3: // Cột 4 - Chỉ số UV (chia lại 10 để có giá trị thực)
                        return String.format(java.util.Locale.getDefault(), "UV\n%.0f", value / 10);

                    default:
                        return "";
                }
            }
        });

        // Gán dữ liệu vào biểu đồ
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        chart.setData(barData);

        // Setup chart with labels
        String[] labels = {"Humidity", "Wind", "Pressure", "UV Index"};
        ChartHelper.setupBarChart(chart, labels);

        chart.animateY(1200);
        chart.invalidate();
    }

    /**
     * THIẾT LẬP BIỂU ĐỒ XÁC SUẤT MƯA (Rain Probability Chart)
     *
     * Hiển thị % khả năng có mưa trong 12 giờ tới
     * - Trục X: Thời gian (giờ)
     * - Trục Y: Xác suất mưa (0-100%)
     */
    private void setupRainProbabilityChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.rainProbabilityChart);
        if (chart == null) return;

        int count = Math.min(9, hourlyForecastData.getList().size());
        
        // Prepare data: convert probability to percentage
        List<Entry> entries = ChartHelper.prepareChartEntries(
            hourlyForecastData,
            count,
            item -> (float) (item.getPop() * 100f)
        );

        // Create and style dataset
        LineDataSet dataSet = new LineDataSet(entries, "Rain Probability");
        ChartHelper.styleLineDataSet(dataSet, ChartHelper.ChartColors.RAIN);

        // Setup chart
        chart.setData(new LineData(dataSet));
        ChartHelper.setupLineChart(chart);
        ChartHelper.setYAxisRange(chart, 0f, 100f);
        ChartHelper.applyTimeFormatter(chart, hourlyForecastData, count);

        chart.animateXY(800, 800);
        chart.invalidate();
    }


    private void setupWindSpeedChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.windSpeedChart);
        if (chart == null) return;

        int count = Math.min(9, hourlyForecastData.getList().size());
        
        // Prepare data with unit conversion
        List<Entry> entries = ChartHelper.prepareChartEntries(
            hourlyForecastData,
            count,
            item -> {
                float speed = (float) item.getWind().getSpeed();
                return "kmh".equalsIgnoreCase(windSpeedUnit) ? speed * 3.6f : speed;
            }
        );

        // Create and style dataset
        LineDataSet dataSet = new LineDataSet(entries, "Wind Speed");
        ChartHelper.styleLineDataSet(dataSet, ChartHelper.ChartColors.WIND);

        // Setup chart
        chart.setData(new LineData(dataSet));
        ChartHelper.setupLineChart(chart);
        ChartHelper.setYAxisRange(chart, 0f, chart.getAxisLeft().getAxisMaximum());
        ChartHelper.applyTimeFormatter(chart, hourlyForecastData, count);

        chart.animateXY(1000, 1000);
        chart.invalidate();
    }



    private void setupHumidityChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.humidityChart);
        if (chart == null) return;

        int count = Math.min(9, hourlyForecastData.getList().size());
        
        // Prepare data
        List<Entry> entries = ChartHelper.prepareChartEntries(
            hourlyForecastData,
            count,
            item -> (float) item.getMain().getHumidity()
        );

        // Create and style dataset
        LineDataSet dataSet = new LineDataSet(entries, "Humidity");
        ChartHelper.styleLineDataSet(dataSet, ChartHelper.ChartColors.HUMIDITY);

        // Setup chart
        chart.setData(new LineData(dataSet));
        ChartHelper.setupLineChart(chart);
        ChartHelper.setYAxisRange(chart, 0f, 100f);
        ChartHelper.applyTimeFormatter(chart, hourlyForecastData, count);

        chart.animateXY(1000, 1000);
        chart.invalidate();
    }




}