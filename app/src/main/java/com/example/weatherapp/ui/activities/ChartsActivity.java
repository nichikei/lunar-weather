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
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


/**
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 * ACTIVITY HIỂN THỊ BIỂU ĐỒ THỐNG KÊ THỜI TIẾT
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 *
 * Activity này hiển thị 5 loại biểu đồ thống kê thời tiết chi tiết:
 *
 * ┌─────────────────────────────────────────────────────────────────────────────────────────────┐
 * │  1. BIỂU ĐỒ ĐƯỜNG - NHIỆT ĐỘ THEO GIỜ (Temperature Chart)                                  │
 * │     • Hiển thị xu hướng thay đổi nhiệt độ trong 12 giờ tới                                 │
 * │     • Đường cong màu tím với hiệu ứng gradient fill                                         │
 * │     • Trục X: Thời gian (Now, 3h, 6h, 9h, 12h...)                                          │
 * │     • Trục Y: Nhiệt độ (°C hoặc °F)                                                         │
 * │                                                                                              │
 * │  2. BIỂU ĐỒ CỘT - CÁC CHỈ SỐ THỜI TIẾT HIỆN TẠI (Weather Stats Chart)                     │
 * │     • 4 cột với màu sắc khác nhau đại diện cho 4 chỉ số:                                   │
 * │       - Cột Xanh dương: Độ ẩm (%)                                                           │
 * │       - Cột Xanh lá: Tốc độ gió (km/h hoặc m/s)                                            │
 * │       - Cột Cam: Áp suất khí quyển (hPa)                                                    │
 * │       - Cột Hồng: Chỉ số UV                                                                 │
 * │                                                                                              │
 * │  3. BIỂU ĐỒ ĐƯỜNG - XÁC SUẤT MƯA THEO GIỜ (Rain Probability Chart)                         │
 * │     • Hiển thị khả năng có mưa (0-100%) trong 12 giờ tới                                   │
 * │     • Đường cong màu xanh nước biển                                                          │
 * │     • Trục X: Thời gian                                                                      │
 * │     • Trục Y: Xác suất mưa (%)                                                              │
 * │                                                                                              │
 * │  4. BIỂU ĐỒ ĐƯỜNG - TỐC ĐỘ GIÓ THEO GIỜ (Wind Speed Chart)                                 │
 * │     • Hiển thị sự thay đổi của tốc độ gió                                                   │
 * │     • Đường cong màu xanh lá                                                                 │
 * │     • Trục X: Thời gian                                                                      │
 * │     • Trục Y: Tốc độ gió (km/h hoặc m/s)                                                    │
 * │                                                                                              │
 * │  5. BIỂU ĐỒ ĐƯỜNG - ĐỘ ẨM THEO GIỜ (Humidity Chart)                                        │
 * │     • Hiển thị sự thay đổi độ ẩm không khí                                                  │
 * │     • Đường cong màu xanh cyan                                                               │
 * │     • Trục X: Thời gian                                                                      │
 * │     • Trục Y: Độ ẩm (0-100%)                                                                │
 * └─────────────────────────────────────────────────────────────────────────────────────────────┘
 *
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 LUỒNG HOẠT ĐỘNG TỔNG THỂ 📊
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 *
 * BƯỚC 1: KHỞI ĐỘNG TỪ MAINACTIVITY
 * ──────────────────────────────────────────────────────────────────────────────────────────────
 * [MainActivity] → Người dùng nhấn nút "View Charts" (btnViewCharts)
 * ↓
 * [MainActivity.openChartsActivity()]
 * ↓ Kiểm tra dữ liệu có sẵn không?
 * ├─→ Nếu KHÔNG có dữ liệu: Hiển thị Toast "Weather data not available yet"
 * └─→ Nếu CÓ dữ liệu: Tạo Intent và truyền 3 loại dữ liệu:
 * • hourly_data: Dữ liệu dự báo theo giờ (HourlyForecastResponse)
 * • current_data: Dữ liệu thời tiết hiện tại (WeatherResponse)
 * • uv_index: Chỉ số UV hiện tại (int)
 * ↓
 * startActivity(intent) → Mở ChartsActivity
 *
 *
 * BƯỚC 2: NHẬN DỮ LIỆU VÀ KHỞI TẠO (onCreate)
 * ──────────────────────────────────────────────────────────────────────────────────────────────
 * [ChartsActivity.onCreate()]
 * ↓
 * ① Nhận dữ liệu từ Intent:
 * • hourlyForecastData = getIntent().getSerializableExtra("hourly_data")
 * • currentWeatherData = getIntent().getSerializableExtra("current_data")
 * • currentUVIndex = getIntent().getIntExtra("uv_index", 0)
 * ↓
 * ② Load cài đặt người dùng từ SharedPreferences:
 * • windSpeedUnit = "ms" hoặc "kmh" (đơn vị tốc độ gió)
 * ↓
 * ③ Setup UI Components:
 * • Nút Back (btnBack) → finish() khi nhấn
 * • Tiêu đề (tvChartTitle) → Hiển thị: "Tên thành phố - Weather Statistics"
 * ↓
 * ④ Khởi tạo tất cả 5 biểu đồ:
 * • setupTemperatureChart()      → Biểu đồ nhiệt độ
 * • setupWeatherStatsChart()     → Biểu đồ các chỉ số thời tiết
 * • setupRainProbabilityChart()  → Biểu đồ xác suất mưa
 * • setupWindSpeedChart()        → Biểu đồ tốc độ gió
 * • setupHumidityChart()         → Biểu đồ độ ẩm
 *
 *
 * BƯỚC 3: THIẾT LẬP TỪNG BIỂU ĐỒ (Quy trình chung cho mỗi biểu đồ)
 * ──────────────────────────────────────────────────────────────────────────────────────────────
 * [setupXXXChart()]
 * ↓
 * ① Kiểm tra dữ liệu:
 * if (dữ liệu == null) return; → Thoát nếu không có dữ liệu
 * ↓
 * ② Tìm view biểu đồ trong layout:
 * Chart chart = findViewById(R.id.xxxChart);
 * if (chart == null) return; → Thoát nếu không tìm thấy view
 * ↓
 * ③ Tạo danh sách điểm dữ liệu (Entries):
 * List<Entry> entries = new ArrayList<>();
 * for (dữ liệu từ API) {
 * entries.add(new Entry(index, value));
 * // Entry(vị trí trục X, giá trị trục Y)
 * }
 * ↓
 * ④ Tạo DataSet và cấu hình màu sắc/kiểu dáng:
 * • Màu đường/cột (setColor)
 * • Màu điểm dữ liệu (setCircleColor)
 * • Độ dày đường (setLineWidth)
 * • Hiệu ứng fill gradient (setDrawFilled, setFillColor)
 * • Làm mượt đường cong (setMode: CUBIC_BEZIER)
 * • Formatter giá trị (setValueFormatter)
 * ↓
 * ⑤ Gán dữ liệu vào biểu đồ:
 * chart.setData(lineData hoặc barData);
 * ↓
 * ⑥ Áp dụng cấu hình chung:
 * • setupChart(chart) hoặc setupBarChart(chart)
 * • Cấu hình trục X, Y
 * • Cấu hình lưới (grid)
 * • Cấu hình tương tác (touch, drag, zoom)
 * ↓
 * ⑦ Custom formatter cho trục X (nếu cần):
 * • Chuyển đổi index → giờ thực tế (14h, 17h, 20h...)
 * • Hoặc tên cột cho biểu đồ cột
 * ↓
 * ⑧ Animation và render:
 * • chart.animateXY(1200, 1200) → Animation 1.2 giây
 * • chart.invalidate() → Vẽ lại biểu đồ
 *
 *
 * BƯỚC 4: XỬ LÝ DỮ LIỆU API CHO BIỂU ĐỒ
 * ──────────────────────────────────────────────────────────────────────────────────────────────
 * Biểu đồ 1, 3, 4, 5: SỬ DỤNG hourlyForecastData (Dự báo theo giờ)
 * ├─→ API Endpoint: api.openweathermap.org/data/2.5/forecast
 * ├─→ Dữ liệu trả về: List<HourlyItem> (mỗi 3 giờ một điểm)
 * ├─→ Mỗi HourlyItem chứa:
 * │    • dt: Timestamp (Unix time)
 * │    • main.temp: Nhiệt độ
 * │    • main.humidity: Độ ẩm
 * │    • wind.speed: Tốc độ gió
 * │    • pop: Xác suất mưa (0-1)
 * └─→ Lấy tối đa 12 điểm = 36 giờ dự báo
 *
 * Biểu đồ 2: SỬ DỤNG currentWeatherData (Thời tiết hiện tại)
 * ├─→ API Endpoint: api.openweathermap.org/data/2.5/weather
 * ├─→ Dữ liệu trả về: WeatherResponse
 * ├─→ Chứa:
 * │    • main.humidity: Độ ẩm hiện tại
 * │    • main.pressure: Áp suất khí quyển
 * │    • wind.speed: Tốc độ gió hiện tại
 * │    • (UV từ biến currentUVIndex riêng)
 * └─→ Hiển thị 4 cột với 4 giá trị này
 *
 *
 * BƯỚC 5: CHUYỂN ĐỔI GIÁ TRỊ VÀ ĐƠN VỊ
 * ──────────────────────────────────────────────────────────────────────────────────────────────
 * • Nhiệt độ: Sử dụng trực tiếp từ API (đã được MainActivity xử lý theo Celsius/Fahrenheit)
 * • Tốc độ gió:
 * if (windSpeedUnit == "kmh") → windSpeed * 3.6 (m/s → km/h)
 * • Xác suất mưa:
 * API trả về 0-1 → Nhân 100 để có % (0-100%)
 * • Áp suất:
 * Chia 10 để cột không quá cao → Nhân lại 10 khi hiển thị label
 * • UV:
 * Nhân 10 để cột không quá thấp → Chia lại 10 khi hiển thị label
 *
 *
 * BƯỚC 6: TƯƠNG TÁC NGƯỜI DÙNG
 * ──────────────────────────────────────────────────────────────────────────────────────────────
 * [Người dùng xem biểu đồ]
 * ↓
 * • Scroll lên/xuống: ScrollView cho phép cuộn xem tất cả 5 biểu đồ
 * • Chạm vào điểm: Hiển thị giá trị chính xác của điểm đó
 * • Kéo biểu đồ: Drag ngang để xem các điểm khác (nếu có nhiều dữ liệu)
 * • Nhấn nút Back: Quay lại MainActivity
 * ↓
 * [finish()] → Đóng ChartsActivity, quay về MainActivity
 *
 *
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 * 🎨 THƯ VIỆN SỬ DỤNG 🎨
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 *
 * MPAndroidChart v3.1.0
 * ├─→ Repository: https://github.com/PhilJay/MPAndroidChart
 * ├─→ Gradle: implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
 * ├─→ Các component sử dụng:
 * │    • LineChart: Biểu đồ đường (nhiệt độ, mưa, gió, độ ẩm)
 * │    • BarChart: Biểu đồ cột (các chỉ số thời tiết)
 * │    • Entry: Điểm dữ liệu cho biểu đồ đường
 * │    • BarEntry: Điểm dữ liệu cho biểu đồ cột
 * │    • LineDataSet: Bộ dữ liệu cho biểu đồ đường
 * │    • BarDataSet: Bộ dữ liệu cho biểu đồ cột
 * │    • ValueFormatter: Format giá trị hiển thị
 * │    • XAxis: Cấu hình trục X
 * └─→ Tính năng:
 * • Animation mượt mà
 * • Touch interaction
 * • Custom màu sắc và gradient
 * • Zoom và pan
 *
 *
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 * 📁 CẤU TRÚC FILE LIÊN QUAN 📁
 * ════════════════════════════════════════════════════════════════════════════════════════════════
 *
 * Java Files:
 * ├─ ChartsActivity.java (file này)
 * │   └─ Xử lý logic và hiển thị tất cả biểu đồ
 * │
 * ├─ MainActivity.java
 * │   ├─ openChartsActivity() → Phương thức mở ChartsActivity
 * │   └─ btnViewCharts.setOnClickListener() → Sự kiện nhấn nút
 * │
 * └─ Data Models:
 * ├─ HourlyForecastResponse.java → Dữ liệu dự báo theo giờ
 * └─ WeatherResponse.java → Dữ liệu thời tiết hiện tại
 *
 * Layout Files:
 * ├─ activity_charts.xml
 * │   └─ Layout chính của ChartsActivity (ScrollView chứa 5 biểu đồ)
 * │
 * └─ Card Layouts (được include vào activity_charts.xml):
 * ├─ card_temperature_chart.xml → Layout biểu đồ nhiệt độ
 * ├─ card_weather_stats_chart.xml → Layout biểu đồ các chỉ số
 * ├─ card_rain_probability_chart.xml → Layout biểu đồ xác suất mưa
 * ├─ card_wind_speed_chart.xml → Layout biểu đồ tốc độ gió
 * └─ card_humidity_chart.xml → Layout biểu đồ độ ẩm
 *
 * ════════════════════════════════════════════════════════════════════════════════════════════════
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
     * • Điểm 0: 14h (Now)
     * • Điểm 1: 17h (3h sau)
     * • Điểm 2: 20h (6h sau)
     * • Điểm 3: 23h (9h sau)
     * • Điểm 4: 02h (12h sau - ngày hôm sau)
     *
     * === TRỤC Y (DỌC): NHIỆT ĐỘ ===
     * - Trục Y hiển thị nhiệt độ: 20°C, 22°C, 25°C, 28°C...
     * - Tự động scale theo nhiệt độ min/max
     *
     * VÍ DỤ BIỂU ĐỒ:
     *
     * Nhiệt độ (°C)
     * 30│                    ●
     * 28│              ●         ●
     * 26│        ●
     * 24│  ●
     * 22│
     * └─────────────────────────────> Thời gian
     * Now  3h   6h   9h   12h
     * 14h  17h  20h  23h  02h
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
        int count = Math.min(9, hourlyForecastData.getList().size());
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
                        return hour + "h";
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

        List<HourlyForecastResponse.HourlyItem> list = hourlyForecastData.getList();

        // 24h tới = 8 điểm (mỗi điểm cách 3h)
        int count = Math.min(9, list.size());

        // Entries: X = index (0..7), Y = % mưa
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double pop0to1 = list.get(i).getPop();
            entries.add(new Entry(i, (float) (pop0to1 * 100f)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rain Probability");
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
        dataSet.setDrawValues(true);

        chart.setData(new LineData(dataSet));
        setupChart(chart); // nếu bạn đã có hàm này để style chung

        // Y: 0–100%
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisRight().setEnabled(false);

        // X: hiển thị giờ thực (HHh) theo timezone city
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);          // mỗi 1 index là 1 nhãn
        xAxis.setLabelCount(count, true);  // đúng 8 nhãn cho 24h tới
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = Math.round(value);
                if (idx < 0 || idx >= count) return "";
                HourlyForecastResponse.HourlyItem it = list.get(idx);

                long tsMs = it.getDt() * 1000L;
                // cộng timezone offset (giây) nếu API có
                int tzSec = hourlyForecastData.getCity() != null ? hourlyForecastData.getCity().getTimezone() : 0;
                long localMs = tsMs + tzSec * 1000L;

                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(localMs);
                int h = cal.get(java.util.Calendar.HOUR_OF_DAY);
                return (h < 10 ? "0" + h : String.valueOf(h)) + "h";
            }
        });

        chart.animateXY(800, 800);
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

        List<HourlyForecastResponse.HourlyItem> list = hourlyForecastData.getList();
        int count = Math.min(9, list.size()); // 24h tới (8 mốc × 3h)

        // ==== DỮ LIỆU ====
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float speed = (float) list.get(i).getWind().getSpeed(); // m/s
            if ("kmh".equalsIgnoreCase(windSpeedUnit)) speed *= 3.6f; // đổi km/h nếu cần
            entries.add(new Entry(i, speed)); // X=index, Y=tốc độ gió
        }

        LineDataSet ds = new LineDataSet(entries, "Wind Speed");
        ds.setColor(0xFF66BB6A);
        ds.setCircleColor(0xFF81C784);
        ds.setLineWidth(3.5f);
        ds.setCircleRadius(5f);
        ds.setDrawCircleHole(true);
        ds.setCircleHoleColor(0xFF4CAF50);
        ds.setCircleHoleRadius(2.5f);
        ds.setDrawFilled(true);
        ds.setFillColor(0xFF66BB6A);
        ds.setFillAlpha(100);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setCubicIntensity(0.15f);
        ds.setDrawValues(true);

        chart.setData(new LineData(ds));


        // ==== TRỤC Y ====
        chart.getAxisRight().setEnabled(false);
        YAxis yLeft = chart.getAxisLeft();
        yLeft.setAxisMinimum(0f); // tốc độ gió không âm
        // *** THÊM CÀI ĐẶT MÀU SẮC TRỤC Y ***
        yLeft.setTextColor(0xCCFFFFFF);
        yLeft.setTextSize(11f);
        yLeft.setDrawGridLines(true);
        yLeft.setGridColor(0x30FFFFFF);
        yLeft.setGridLineWidth(1f);
        yLeft.setDrawAxisLine(false);


        // ==== TRỤC X: HIỂN THỊ GIỜ (HHh) ====
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);             // mỗi index = 1 nhãn
        x.setLabelCount(count, true);     // đúng 8 nhãn
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(true);
        // *** THÊM CÀI ĐẶT MÀU SẮC TRỤC X ***
        x.setTextColor(0xCCFFFFFF);
        x.setTextSize(11f);
        x.setDrawAxisLine(true);
        x.setAxisLineColor(0x40FFFFFF);
        x.setAxisLineWidth(1.5f);


        final int tzSec = (hourlyForecastData.getCity() != null)
                ? hourlyForecastData.getCity().getTimezone() : 0;

        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);                  // tránh 2.7, 3.2 khi zoom/pan
                if (index < 0 || index >= count) return "";
                long tsMs = list.get(index).getDt() * 1000L;    // giây → mili-giây
                long localMs = tsMs + tzSec * 1000L;            // cộng offset timezone

                java.util.Calendar cal = java.util.Calendar.getInstance(
                        java.util.TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(localMs);
                int h = cal.get(java.util.Calendar.HOUR_OF_DAY);
                return (h < 10 ? "0" + h : String.valueOf(h)) + "h";
            }
        });

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        chart.animateXY(1000, 1000);
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

        List<HourlyForecastResponse.HourlyItem> list = hourlyForecastData.getList();
        int count = Math.min(9, list.size()); // 24h tới (8 mốc × 3h)

        // ==== DỮ LIỆU ====
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float humidity = (float) list.get(i).getMain().getHumidity(); // 0..100
            entries.add(new Entry(i, humidity)); // X=index, Y=độ ẩm %
        }

        LineDataSet ds = new LineDataSet(entries, "Humidity");
        ds.setColor(0xFF26C6DA);
        ds.setCircleColor(0xFF4DD0E1);
        ds.setLineWidth(3.5f);
        ds.setCircleRadius(5f);
        ds.setDrawCircleHole(true);
        ds.setCircleHoleColor(0xFF00BCD4);
        ds.setCircleHoleRadius(2.5f);
        ds.setDrawFilled(true);
        ds.setFillColor(0xFF26C6DA);
        ds.setFillAlpha(100);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setCubicIntensity(0.15f);
        ds.setDrawValues(true);

        chart.setData(new LineData(ds));

        // ==== TRỤC Y: 0–100% ====
        chart.getAxisRight().setEnabled(false);
        YAxis yLeft = chart.getAxisLeft();
        yLeft.setAxisMinimum(0f);
        yLeft.setAxisMaximum(100f);
        // *** THÊM CÀI ĐẶT MÀU SẮC TRỤC Y ***
        yLeft.setTextColor(0xCCFFFFFF);
        yLeft.setTextSize(11f);
        yLeft.setDrawGridLines(true);
        yLeft.setGridColor(0x30FFFFFF);
        yLeft.setGridLineWidth(1f);
        yLeft.setDrawAxisLine(false);

        // ==== TRỤC X: HIỂN THỊ GIỜ (HHh) ====
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setLabelCount(count, true);
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(true);
        // *** THÊM CÀI ĐẶT MÀU SẮC TRỤC X ***
        x.setTextColor(0xCCFFFFFF);
        x.setTextSize(11f);
        x.setDrawAxisLine(true);
        x.setAxisLineColor(0x40FFFFFF);
        x.setAxisLineWidth(1.5f);

        final int tzSec = (hourlyForecastData.getCity() != null)
                ? hourlyForecastData.getCity().getTimezone() : 0;

        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index < 0 || index >= count) return "";
                long tsMs = list.get(index).getDt() * 1000L;   // giây → mili-giây
                long localMs = tsMs + tzSec * 1000L;           // cộng offset timezone

                java.util.Calendar cal = java.util.Calendar.getInstance(
                        java.util.TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(localMs);
                int h = cal.get(java.util.Calendar.HOUR_OF_DAY);
                return (h < 10 ? "0" + h : String.valueOf(h)) + "h";
            }
        });

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        chart.animateXY(1000, 1000);
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