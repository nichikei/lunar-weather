package com.example.weatherapp.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
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

/**
 * ACTIVITY HIỂN THỊ BIỂU ĐỒ THỐNG KÊ THỜI TIẾT
 *
 * Activity này hiển thị 5 loại biểu đồ:
 * 1. Biểu đồ đường: Nhiệt độ theo giờ (Temperature Chart)
 * 2. Biểu đồ cột: Các chỉ số thời tiết hiện tại (Weather Stats Chart)
 * 3. Biểu đồ đường: Xác suất mưa theo giờ (Rain Probability Chart)
 * 4. Biểu đồ đường: Tốc độ gió theo giờ (Wind Speed Chart)
 * 5. Biểu đồ đường: Độ ẩm theo giờ (Humidity Chart)
 *
 * Sử dụng thư viện MPAndroidChart để vẽ biểu đồ
 */
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

    /**
     * THIẾT LẬP BIỂU ĐỒ NHIỆT ĐỘ (Temperature Chart)
     *
     * Hiển thị nhiệt độ thay đổi theo giờ trong 12 giờ tới
     *
     * === TRỤC X (NGANG): THỜI GIAN ===
     * - Trục X hiển thị thời gian: 0h, 3h, 6h, 9h, 12h...
     * - API trả về dữ liệu mỗi 3 giờ một lần
     * - Ví dụ: Nếu bây giờ là 14h (2PM), các điểm sẽ là:
     *   • Điểm 0: 14h (Now)
     *   • Điểm 1: 17h (3h sau)
     *   • Điểm 2: 20h (6h sau)
     *   • Điểm 3: 23h (9h sau)
     *   • Điểm 4: 02h (12h sau - ngày hôm sau)
     *
     * === TRỤC Y (DỌC): NHIỆT ĐỘ ===
     * - Trục Y hiển thị nhiệt độ: 20°C, 22°C, 25°C, 28°C...
     * - Tự động scale theo nhiệt độ min/max
     *
     * VÍ DỤ BIỂU ĐỒ:
     *
     * Nhiệt độ (°C)
     *   30│                    ●
     *   28│              ●         ●
     *   26│        ●
     *   24│  ●
     *   22│
     *     └─────────────────────────────> Thời gian
     *      Now  3h   6h   9h   12h
     *      14h  17h  20h  23h  02h
     */
    private void setupTemperatureChart() {
        // Kiểm tra dữ liệu có tồn tại không
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) {
            return;
        }

        // Tìm biểu đồ trong layout
        LineChart chart = findViewById(R.id.temperatureChart);
        if (chart == null) return;

        // Danh sách các điểm dữ liệu (Entry) để vẽ biểu đồ
        List<Entry> entries = new ArrayList<>();

        // Lấy tối đa 12 điểm dữ liệu (tương đương 36 giờ, vì mỗi điểm cách 3h)
        int count = Math.min(12, hourlyForecastData.getList().size());
        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            float temp = (float) item.getMain().getTemp();

            // Thêm điểm vào biểu đồ: Entry(vị trí trên trục X, giá trị trên trục Y)
            // Entry(thời gian index, nhiệt độ)
            //
            // VÍ DỤ CỤ THỂ:
            // - Entry(0, 25) = Thời điểm 0 (Now), Nhiệt độ 25°C
            // - Entry(1, 27) = Thời điểm 1 (3h sau), Nhiệt độ 27°C
            // - Entry(2, 28) = Thời điểm 2 (6h sau), Nhiệt độ 28°C
            entries.add(new Entry(i, temp));
        }

        // Tạo DataSet (bộ dữ liệu) cho biểu đồ
        LineDataSet dataSet = new LineDataSet(entries, "Temperature");

        // === CÀI ĐẶT MÀU SẮC VÀ KIỂU DÁNG ===
        dataSet.setColor(0xFF9B6FFF);              // Màu đường - Tím nhạt
        dataSet.setCircleColor(0xFFE2DDFD);        // Màu điểm dữ liệu - Tím rất nhạt
        dataSet.setLineWidth(3.5f);                // Độ dày đường line
        dataSet.setCircleRadius(6f);               // Bán kính của điểm tròn
        dataSet.setDrawCircleHole(true);           // Vẽ lỗ giữa điểm tròn
        dataSet.setCircleHoleColor(0xFF5B3E9E);    // Màu lỗ giữa điểm
        dataSet.setCircleHoleRadius(3f);           // Bán kính lỗ
        dataSet.setValueTextSize(11f);             // Kích thước chữ hiển thị giá trị
        dataSet.setValueTextColor(0xFFFFFFFF);     // Màu chữ giá trị - Trắng

        // === TÔ MÀU DƯỚI ĐƯỜNG LINE ===
        dataSet.setDrawFilled(true);               // Bật tô màu vùng dưới đường
        dataSet.setFillColor(0xFF7B5EC6);          // Màu tô - Tím đậm
        dataSet.setFillAlpha(100);                 // Độ trong suốt (0-255)

        // === LÀM ĐƯỜNG CONG MƯỢT ===
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);  // Dùng đường cong Bezier
        dataSet.setCubicIntensity(0.15f);                // Độ cong (0-1)

        // Gán dữ liệu vào biểu đồ
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Áp dụng cài đặt chung cho biểu đồ
        setupChart(chart);

        // === THÊM LABELS THỜI GIAN CHO TRỤC X ===
        // Custom formatter để hiển thị giờ thực tế thay vì số 0,1,2,3...
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < hourlyForecastData.getList().size()) {
                    // Lấy timestamp từ API
                    HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(index);
                    long timestamp = item.getDt() * 1000L;  // Chuyển từ seconds sang milliseconds

                    // Format thành giờ: 14h, 17h, 20h...
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);

                    // Hiển thị "Now" cho điểm đầu tiên, còn lại hiển thị giờ
                    if (index == 0) {
                        return "Now";
                    } else {
                        return hour + "h";
                    }
                }
                return "";
            }
        });

        // Animation khi hiển thị: X và Y cùng animate trong 1.2 giây
        chart.animateXY(1200, 1200);

        // Vẽ lại biểu đồ
        chart.invalidate();
    }

    /**
     * THIẾT LẬP BIỂU ĐỒ CỘT - CÁC CHỈ SỐ THỜI TIẾT (Weather Stats Chart)
     *
     * Hiển thị 4 chỉ số thời tiết hiện tại dưới dạng cột:
     * - Cột 1 (Xanh dương): Độ ẩm (%) - VD: 75%
     * - Cột 2 (Xanh lá):    Tốc độ gió (km/h hoặc m/s) - VD: 12.5 km/h
     * - Cột 3 (Cam):        Áp suất khí quyển (hPa) - VD: 1013 hPa
     * - Cột 4 (Hồng):       Chỉ số UV - VD: UV 5
     *
     * LƯU Ý: Trục dọc không phải là GIỜ, mà là GIÁ TRỊ của từng chỉ số
     * Các giá trị được chia/nhân để cân bằng chiều cao cột cho đẹp
     */
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
        barData.setBarWidth(0.7f);  // Độ rộng của cột (0-1)
        chart.setData(barData);

        // Áp dụng cài đặt chung cho biểu đồ cột
        setupBarChart(chart);

        // Animation: Cột mọc lên từ dưới trong 1.2 giây
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

        List<Entry> entries = new ArrayList<>();
        int count = Math.min(12, hourlyForecastData.getList().size());

        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            // getPop() trả về 0-1, nhân 100 để có %
            float rainProb = (float) (item.getPop() * 100);
            entries.add(new Entry(i, rainProb));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rain Probability");

        // Màu xanh nước biển cho mưa
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
        dataSet.setDrawValues(false);  // Không hiển thị giá trị trên điểm

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        setupChart(chart);

        // Set giới hạn trục Y: 0% đến 100%
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);

        chart.animateXY(1200, 1200);
        chart.invalidate();
    }

    /**
     * THIẾT LẬP BIỂU ĐỒ TỐC ĐỘ GIÓ (Wind Speed Chart)
     *
     * Hiển thị tốc độ gió thay đổi theo giờ
     * - Trục X: Thời gian (giờ)
     * - Trục Y: Tốc độ gió (km/h hoặc m/s)
     */
    private void setupWindSpeedChart() {
        if (hourlyForecastData == null || hourlyForecastData.getList() == null) return;

        LineChart chart = findViewById(R.id.windSpeedChart);
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        int count = Math.min(12, hourlyForecastData.getList().size());

        for (int i = 0; i < count; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
            float windSpeed = (float) item.getWind().getSpeed();

            // Chuyển đổi đơn vị nếu cần
            if (windSpeedUnit.equals("kmh")) {
                windSpeed = windSpeed * 3.6f;
            }
            entries.add(new Entry(i, windSpeed));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Wind Speed");

        // Màu xanh lá cho gió
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

    /**
     * THIẾT LẬP BIỂU ĐỒ ĐỘ ẨM (Humidity Chart)
     *
     * Hiển thị độ ẩm không khí thay đổi theo giờ
     * - Trục X: Thời gian (giờ)
     * - Trục Y: Độ ẩm (0-100%)
     */
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

        // Màu xanh cyan cho độ ẩm
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

        // Set giới hạn trục Y: 0% đến 100%
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);

        chart.animateXY(1200, 1200);
        chart.invalidate();
    }

    /**
     * CÀI ĐẶT CHUNG CHO TẤT CẢ BIỂU ĐỒ ĐƯỜNG (Line Chart)
     *
     * Thiết lập:
     * - Màu chữ, kích thước chữ
     * - Lưới (grid lines)
     * - Trục tọa độ (axis)
     * - Khả năng tương tác (touch, drag)
     */
    private void setupChart(LineChart chart) {
        // Tắt mô tả biểu đồ
        chart.getDescription().setEnabled(false);

        // Tắt chú thích (legend)
        chart.getLegend().setEnabled(false);

        // Tắt nền lưới
        chart.setDrawGridBackground(false);

        // Tắt trục Y bên phải (chỉ dùng trục Y bên trái)
        chart.getAxisRight().setEnabled(false);

        // === CÀI ĐẶT TRỤC X (Thời gian) ===
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);  // Đặt trục X ở dưới
        chart.getXAxis().setTextColor(0xCCFFFFFF);    // Màu chữ trắng hơi trong suốt
        chart.getXAxis().setTextSize(11f);            // Kích thước chữ
        chart.getXAxis().setDrawGridLines(false);     // Không vẽ đường lưới dọc
        chart.getXAxis().setDrawAxisLine(true);       // Vẽ trục X
        chart.getXAxis().setAxisLineColor(0x40FFFFFF);// Màu trục X
        chart.getXAxis().setAxisLineWidth(1.5f);      // Độ dày trục X

        // === CÀI ĐẶT TRỤC Y (Giá trị) ===
        chart.getAxisLeft().setTextColor(0xCCFFFFFF);
        chart.getAxisLeft().setTextSize(11f);
        chart.getAxisLeft().setDrawGridLines(true);   // Vẽ đường lưới ngang
        chart.getAxisLeft().setGridColor(0x30FFFFFF); // Màu lưới (rất mờ)
        chart.getAxisLeft().setGridLineWidth(1f);
        chart.getAxisLeft().setDrawAxisLine(false);   // Không vẽ trục Y

        // === TƯƠNG TÁC ===
        chart.setTouchEnabled(true);    // Bật chạm
        chart.setDragEnabled(true);     // Cho phép kéo biểu đồ
        chart.setScaleEnabled(false);   // Không cho zoom
        chart.setPinchZoom(false);      // Không cho pinch zoom

        // Khoảng cách lề (left, top, right, bottom)
        chart.setExtraOffsets(8, 16, 8, 8);
    }

    /**
     * CÀI ĐẶT CHO BIỂU ĐỒ CỘT (Bar Chart)
     *
     * Tương tự như setupChart() nhưng dành riêng cho biểu đồ cột
     */
    private void setupBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);

        // === TRỤC X: HIỂN THỊ TÊN CÁC CỘT ===
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextColor(0xCCFFFFFF);
        chart.getXAxis().setTextSize(11f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setAxisLineColor(0x40FFFFFF);
        chart.getXAxis().setAxisLineWidth(1.5f);
        chart.getXAxis().setGranularity(1f);  // Khoảng cách giữa các giá trị

        // Custom formatter để hiển thị tên cột
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            // Tên của 4 cột
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

        // Tắt tương tác với biểu đồ cột
        chart.setTouchEnabled(false);

        // Tự động fit các cột vào khung
        chart.setFitBars(true);

        chart.setExtraOffsets(8, 16, 8, 8);
    }
}
