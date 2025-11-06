# 📊 TÀI LIỆU HỆ THỐNG BIỂU ĐỒ WEATHER APP

> **Tài liệu chi tiết về hệ thống biểu đồ thống kê thời tiết trong ứng dụng Weather App**

---

## 📑 MỤC LỤC

1. [Tổng Quan](#1-tổng-quan)
2. [Các File Liên Quan](#2-các-file-liên-quan)
3. [Luồng Hoạt Động Chi Tiết](#3-luồng-hoạt-động-chi-tiết)
4. [Các Loại Biểu Đồ](#4-các-loại-biểu-đồ)
5. [Xử Lý Dữ Liệu API](#5-xử-lý-dữ-liệu-api)
6. [Thư Viện Sử Dụng](#6-thư-viện-sử-dụng)
7. [Cấu Trúc Code](#7-cấu-trúc-code)

---

## 1. TỔNG QUAN

### 🎯 Mục Đích
Hệ thống biểu đồ cung cấp khả năng **trực quan hóa dữ liệu thời tiết** thông qua 5 loại biểu đồ khác nhau, giúp người dùng:
- Theo dõi xu hướng thay đổi nhiệt độ
- Xem các chỉ số thời tiết hiện tại một cách trực quan
- Dự đoán khả năng có mưa trong thời gian tới
- Theo dõi sự thay đổi của gió và độ ẩm

### 📊 5 Loại Biểu Đồ

| # | Loại Biểu Đồ | Mô Tả | Màu Sắc Chủ Đạo |
|---|--------------|-------|-----------------|
| 1 | **Temperature Chart** | Nhiệt độ theo giờ (12 giờ tới) | 🟣 Tím (#9B6FFF) |
| 2 | **Weather Stats Chart** | 4 chỉ số thời tiết hiện tại | 🔵🟢🟠🔴 Multi-color |
| 3 | **Rain Probability Chart** | Xác suất mưa theo giờ | 🔵 Xanh dương (#4FC3F7) |
| 4 | **Wind Speed Chart** | Tốc độ gió theo giờ | 🟢 Xanh lá (#66BB6A) |
| 5 | **Humidity Chart** | Độ ẩm theo giờ | 🔵 Cyan (#26C6DA) |

---

## 2. CÁC FILE LIÊN QUAN

### 📁 Java Files

```
app/src/main/java/com/example/weatherapp/
│
├── ui/activities/
│   ├── ChartsActivity.java          ⭐ File chính - Xử lý tất cả biểu đồ
│   └── MainActivity.java             → Khởi động ChartsActivity
│
├── data/responses/
│   ├── HourlyForecastResponse.java  → Model dữ liệu dự báo theo giờ
│   └── WeatherResponse.java         → Model dữ liệu thời tiết hiện tại
│
└── data/api/
    └── WeatherApiService.java       → Gọi API lấy dữ liệu
```

### 🎨 Layout Files

```
app/src/main/res/layout/
│
├── activity_charts.xml                    ⭐ Layout chính (ScrollView)
│
├── card_temperature_chart.xml            → Card biểu đồ nhiệt độ
├── card_weather_stats_chart.xml          → Card biểu đồ thống kê
├── card_rain_probability_chart.xml       → Card biểu đồ mưa
├── card_wind_speed_chart.xml             → Card biểu đồ gió
└── card_humidity_chart.xml               → Card biểu đồ độ ẩm
```

### 📦 Dependencies

```gradle
// app/build.gradle
dependencies {
    // MPAndroidChart - Thư viện vẽ biểu đồ
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

---

## 3. LUỒNG HOẠT ĐỘNG CHI TIẾT

### 🔄 Sơ Đồ Luồng Tổng Thể

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          NGƯỜI DÙNG                                      │
│                         Nhấn nút "View Charts"                           │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       MAINACTIVITY                                       │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ btnViewCharts.setOnClickListener()                                │  │
│  │    ↓                                                               │  │
│  │ openChartsActivity()                                              │  │
│  │    ↓                                                               │  │
│  │ ✓ Kiểm tra dữ liệu có sẵn?                                        │  │
│  │    ├─ Không → Toast "Data not available"                          │  │
│  │    └─ Có → Tạo Intent với dữ liệu:                                │  │
│  │              • hourly_data (HourlyForecastResponse)               │  │
│  │              • current_data (WeatherResponse)                     │  │
│  │              • uv_index (int)                                      │  │
│  │    ↓                                                               │  │
│  │ startActivity(intent)                                             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      CHARTSACTIVITY                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ onCreate()                                                         │  │
│  │    ↓                                                               │  │
│  │ ① Nhận dữ liệu từ Intent                                          │  │
│  │    • hourlyForecastData = getIntent().getSerializableExtra()     │  │
│  │    • currentWeatherData = getIntent().getSerializableExtra()     │  │
│  │    • currentUVIndex = getIntent().getIntExtra()                   │  │
│  │    ↓                                                               │  │
│  │ ② Load cài đặt từ SharedPreferences                              │  │
│  │    • windSpeedUnit = "ms" hoặc "kmh"                              │  │
│  │    ↓                                                               │  │
│  │ ③ Setup UI                                                        │  │
│  │    • btnBack → finish()                                           │  │
│  │    • tvChartTitle → "City - Weather Statistics"                  │  │
│  │    ↓                                                               │  │
│  │ ④ Khởi tạo 5 biểu đồ (tuần tự)                                   │  │
│  │    ├─ setupTemperatureChart()                                     │  │
│  │    ├─ setupWeatherStatsChart()                                    │  │
│  │    ├─ setupRainProbabilityChart()                                 │  │
│  │    ├─ setupWindSpeedChart()                                       │  │
│  │    └─ setupHumidityChart()                                        │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│               QUY TRÌNH THIẾT LẬP MỖI BIỂU ĐỒ                          │
│  (setupXXXChart() - Áp dụng cho cả 5 biểu đồ)                          │
│                                                                          │
│  ① Kiểm tra dữ liệu                                                     │
│     if (data == null) return;                                           │
│                                                                          │
│  ② Tìm view biểu đồ trong layout                                       │
│     Chart chart = findViewById(R.id.xxxChart);                          │
│     if (chart == null) return;                                          │
│                                                                          │
│  ③ Tạo danh sách Entry (điểm dữ liệu)                                  │
│     List<Entry> entries = new ArrayList<>();                            │
│     for (data) {                                                        │
│         entries.add(new Entry(x_index, y_value));                      │
│     }                                                                    │
│                                                                          │
│  ④ Tạo DataSet và cấu hình style                                       │
│     LineDataSet/BarDataSet dataSet = new DataSet(entries, "Label");    │
│     • setColor() - Màu đường/cột                                        │
│     • setLineWidth() - Độ dày                                           │
│     • setDrawFilled() - Tô màu bên dưới                                │
│     • setMode(CUBIC_BEZIER) - Làm mượt đường cong                      │
│     • setValueFormatter() - Format hiển thị giá trị                    │
│                                                                          │
│  ⑤ Gán dữ liệu vào biểu đồ                                             │
│     chart.setData(lineData / barData);                                  │
│                                                                          │
│  ⑥ Áp dụng cấu hình chung                                              │
│     setupChart(chart) / setupBarChart(chart)                            │
│     • Cấu hình trục X, Y                                                │
│     • Cấu hình lưới (grid)                                              │
│     • Cấu hình tương tác (touch, drag, zoom)                           │
│                                                                          │
│  ⑦ Custom formatter trục X (nếu cần)                                   │
│     chart.getXAxis().setValueFormatter(new ValueFormatter() {...});    │
│                                                                          │
│  ⑧ Animation và render                                                  │
│     chart.animateXY(1200, 1200);  // 1.2 giây                          │
│     chart.invalidate();           // Vẽ lại                             │
└─────────────────────────────────────────────────────────────────────────┘
```

### 📝 Chi Tiết Các Bước

#### **Bước 1: Khởi Động Từ MainActivity**

**File:** `MainActivity.java`

```java
// Sự kiện click nút "View Charts"
binding.btnViewCharts.setOnClickListener(v -> {
    openChartsActivity();
});

// Method mở ChartsActivity
private void openChartsActivity() {
    // Kiểm tra dữ liệu có sẵn không
    if (hourlyForecastData == null || currentWeatherData == null) {
        Toast.makeText(this, "Weather data not available yet", Toast.LENGTH_SHORT).show();
        return;
    }

    // Tạo Intent và truyền dữ liệu
    Intent intent = new Intent(this, ChartsActivity.class);
    intent.putExtra("hourly_data", hourlyForecastData);
    intent.putExtra("current_data", currentWeatherData);
    intent.putExtra("uv_index", currentUVIndex);
    
    // Mở ChartsActivity
    startActivity(intent);
}
```

**Dữ liệu được truyền:**
- `hourly_data`: Dự báo theo giờ (40 điểm dữ liệu ~ 5 ngày)
- `current_data`: Thời tiết hiện tại (nhiệt độ, độ ẩm, gió, áp suất...)
- `uv_index`: Chỉ số UV hiện tại (0-11+)

---

#### **Bước 2: Nh���n Dữ Liệu và Khởi Tạo**

**File:** `ChartsActivity.java` - Method `onCreate()`

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_charts);

    // ① Nhận dữ liệu từ Intent
    hourlyForecastData = (HourlyForecastResponse) getIntent()
        .getSerializableExtra("hourly_data");
    currentWeatherData = (WeatherResponse) getIntent()
        .getSerializableExtra("current_data");
    currentUVIndex = getIntent().getIntExtra("uv_index", 0);

    // ② Load cài đặt người dùng
    SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
    windSpeedUnit = SettingsActivity.getWindSpeedUnit(prefs); // "ms" hoặc "kmh"

    // ③ Setup UI Components
    ImageButton btnBack = findViewById(R.id.btnBack);
    btnBack.setOnClickListener(v -> finish());

    TextView tvTitle = findViewById(R.id.tvChartTitle);
    tvTitle.setText(currentWeatherData.getName() + " - Weather Statistics");

    // ④ Khởi tạo tất cả biểu đồ
    setupTemperatureChart();
    setupWeatherStatsChart();
    setupRainProbabilityChart();
    setupWindSpeedChart();
    setupHumidityChart();
}
```

---

#### **Bước 3: Thiết Lập Từng Biểu Đồ**

Mỗi biểu đồ đều tuân theo quy trình 8 bước như sơ đồ trên. Dưới đây là ví dụ chi tiết cho **Temperature Chart**:

```java
private void setupTemperatureChart() {
    // ① Kiểm tra dữ liệu
    if (hourlyForecastData == null || hourlyForecastData.getList() == null) {
        return;
    }

    // ② Tìm view biểu đồ
    LineChart chart = findViewById(R.id.temperatureChart);
    if (chart == null) return;

    // ③ Tạo danh sách Entry
    List<Entry> entries = new ArrayList<>();
    int count = Math.min(12, hourlyForecastData.getList().size());
    
    for (int i = 0; i < count; i++) {
        HourlyForecastResponse.HourlyItem item = hourlyForecastData.getList().get(i);
        float temp = (float) item.getMain().getTemp();
        entries.add(new Entry(i, temp)); // Entry(x_index, y_value)
    }

    // ④ Tạo DataSet và style
    LineDataSet dataSet = new LineDataSet(entries, "Temperature");
    dataSet.setColor(0xFF9B6FFF);              // Màu tím
    dataSet.setLineWidth(3.5f);                // Độ dày
    dataSet.setDrawFilled(true);               // Tô gradient
    dataSet.setFillColor(0xFF7B5EC6);
    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Làm mượt

    // ⑤ Gán dữ liệu
    LineData lineData = new LineData(dataSet);
    chart.setData(lineData);

    // ⑥ Cấu hình chung
    setupChart(chart);

    // ⑦ Custom formatter trục X
    chart.getXAxis().setValueFormatter(new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index == 0) return "Now";
            
            HourlyForecastResponse.HourlyItem item = 
                hourlyForecastData.getList().get(index);
            long timestamp = item.getDt() * 1000L;
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            
            return hour + "h";
        }
    });

    // ⑧ Animation và render
    chart.animateXY(1200, 1200);
    chart.invalidate();
}
```

---

## 4. CÁC LOẠI BIỂU ĐỒ

### 📈 1. TEMPERATURE CHART (Biểu Đồ Nhiệt Độ)

**Mô tả:**
- Biểu đồ đường (Line Chart) hiển thị nhiệt độ thay đổi theo giờ
- Hiển thị 12 điểm dữ liệu = 36 giờ (mỗi điểm cách 3h)

**Trục tọa độ:**
- **Trục X (ngang):** Thời gian - "Now", "3h", "6h", "9h", "12h"...
- **Trục Y (dọc):** Nhiệt độ - 20°C, 25°C, 30°C...

**Màu sắc:**
- Đường chính: `#9B6FFF` (Tím nhạt)
- Điểm dữ liệu: `#E2DDFD` (Tím rất nhạt)
- Fill gradient: `#7B5EC6` (Tím đậm)

**Ví dụ trực quan:**
```
Nhiệt độ (°C)
  30│                    ●
  28│              ●         ●
  26│        ●
  24│  ●
  22│
    └─────────────────────────────> Thời gian
     Now  3h   6h   9h   12h
```

**Data source:** `hourlyForecastData.getList().get(i).getMain().getTemp()`

---

### 📊 2. WEATHER STATS CHART (Biểu Đồ Thống Kê)

**Mô tả:**
- Biểu đồ cột (Bar Chart) hiển thị 4 chỉ số thời tiết **hiện tại**
- Mỗi cột đại diện cho 1 chỉ số khác nhau

**4 cột:**

| Cột | Chỉ Số | Màu Sắc | Đơn Vị | Công Thức Hiển Thị |
|-----|--------|---------|--------|--------------------|
| 1 | Độ ẩm | 🔵 `#4FC3F7` | % | Giá trị trực tiếp |
| 2 | Tốc độ gió | 🟢 `#66BB6A` | km/h hoặc m/s | `speed * 3.6` nếu km/h |
| 3 | Áp suất | 🟠 `#FFB347` | hPa | `pressure / 10` (hiển thị) |
| 4 | Chỉ số UV | 🔴 `#FF6B9D` | UV | `uv * 10` (hiển thị) |

**Lý do chia/nhân:**
- **Áp suất:** Giá trị ~1013 hPa quá lớn → Chia 10 để cột không quá cao
- **UV:** Giá trị ~5 quá nhỏ → Nhân 10 để cột không quá thấp
- **Mục đích:** Cân bằng chiều cao các cột cho đẹp mắt

**Data source:**
- Độ ẩm: `currentWeatherData.getMain().getHumidity()`
- Gió: `currentWeatherData.getWind().getSpeed()`
- Áp suất: `currentWeatherData.getMain().getPressure()`
- UV: `currentUVIndex` (từ API riêng)

---

### 💧 3. RAIN PROBABILITY CHART (Biểu Đồ Xác Suất Mưa)

**Mô tả:**
- Biểu đồ đường hiển thị khả năng có mưa (0-100%)
- Hiển thị 12 điểm dữ liệu

**Trục tọa độ:**
- **Trục X:** Thời gian (giờ)
- **Trục Y:** Xác suất mưa (%) - Cố định từ 0% đến 100%

**Màu sắc:**
- Đường chính: `#4FC3F7` (Xanh nước biển)
- Điểm dữ liệu: `#81D4FA` (Xanh nhạt)
- Fill: `#4FC3F7` với alpha 100

**Chuyển đổi dữ liệu:**
```java
float rainProb = (float) (item.getPop() * 100);
// API trả về 0.0 - 1.0 → Nhân 100 để có 0% - 100%
```

**Data source:** `hourlyForecastData.getList().get(i).getPop()`

---

### 🌬️ 4. WIND SPEED CHART (Biểu Đồ Tốc Độ Gió)

**Mô tả:**
- Biểu đồ đường hiển thị tốc độ gió thay đổi
- Tự động chuyển đổi đơn vị theo cài đặt người dùng

**Trục tọa độ:**
- **Trục X:** Thời gian (giờ)
- **Trục Y:** Tốc độ gió (m/s hoặc km/h)

**Màu sắc:**
- Đường chính: `#66BB6A` (Xanh lá)
- Điểm dữ liệu: `#81C784`
- Fill: `#66BB6A`

**Chuyển đổi đơn vị:**
```java
float windSpeed = (float) item.getWind().getSpeed();
if (windSpeedUnit.equals("kmh")) {
    windSpeed = windSpeed * 3.6f;  // 1 m/s = 3.6 km/h
}
```

**Data source:** `hourlyForecastData.getList().get(i).getWind().getSpeed()`

---

### 💦 5. HUMIDITY CHART (Biểu Đồ Độ Ẩm)

**Mô tả:**
- Biểu đồ đường hiển thị độ ẩm không khí
- Trục Y cố định từ 0% đến 100%

**Trục tọa độ:**
- **Trục X:** Thời gian (giờ)
- **Trục Y:** Độ ẩm (%) - Cố định 0-100%

**Màu sắc:**
- Đường chính: `#26C6DA` (Cyan)
- Điểm dữ liệu: `#4DD0E1`
- Fill: `#26C6DA`

**Data source:** `hourlyForecastData.getList().get(i).getMain().getHumidity()`

---

## 5. XỬ LÝ DỮ LIỆU API

### 🌐 API Endpoints

#### **1. Hourly Forecast API**
Sử dụng cho: Biểu đồ 1, 3, 4, 5

```
Endpoint: https://api.openweathermap.org/data/2.5/forecast
Method: GET
Parameters:
  - lat: Vĩ độ
  - lon: Kinh độ
  - appid: API key
  - units: metric (Celsius) hoặc imperial (Fahrenheit)
```

**Response Structure:**
```json
{
  "list": [
    {
      "dt": 1699999999,              // Timestamp (Unix)
      "main": {
        "temp": 25.5,                // Nhiệt độ
        "humidity": 75                // Độ ẩm (%)
      },
      "wind": {
        "speed": 5.2                 // Tốc độ gió (m/s)
      },
      "pop": 0.35                    // Xác suất mưa (0-1)
    },
    // ... 39 items nữa (40 điểm dữ liệu = 5 ngày)
  ]
}
```

#### **2. Current Weather API**
Sử dụng cho: Biểu đồ 2

```
Endpoint: https://api.openweathermap.org/data/2.5/weather
Method: GET
Parameters: Tương tự như trên
```

**Response Structure:**
```json
{
  "name": "Hanoi",
  "main": {
    "temp": 28.5,
    "humidity": 70,
    "pressure": 1013
  },
  "wind": {
    "speed": 3.5
  }
}
```

#### **3. UV Index API**
Sử dụng cho: Biểu đồ 2 (cột UV)

```
Endpoint: https://api.openweathermap.org/data/2.5/uvi
Method: GET
```

**Response:**
```json
{
  "value": 5.2    // Chỉ số UV (0-11+)
}
```

### 📊 Xử Lý Dữ Liệu

**Lọc dữ liệu:**
```java
// Chỉ lấy 12 điểm đầu tiên từ 40 điểm
int count = Math.min(12, hourlyForecastData.getList().size());
```

**Chuyển đổi timestamp:**
```java
long timestamp = item.getDt() * 1000L;  // Unix time → milliseconds
Calendar calendar = Calendar.getInstance();
calendar.setTimeInMillis(timestamp);
int hour = calendar.get(Calendar.HOUR_OF_DAY);
```

**Xử lý lỗi:**
```java
// Kiểm tra null trước khi sử dụng
if (hourlyForecastData == null || hourlyForecastData.getList() == null) {
    return;  // Thoát khỏi method
}
```

---

## 6. THƯ VIỆN SỬ DỤNG

### 📚 MPAndroidChart v3.1.0

**GitHub:** https://github.com/PhilJay/MPAndroidChart

**Cài đặt:**
```gradle
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

// app/build.gradle
dependencies {
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

### 🎨 Các Component Chính

#### **LineChart**
Biểu đồ đường - Dùng cho nhiệt độ, mưa, gió, độ ẩm

```java
LineChart chart = findViewById(R.id.temperatureChart);

// Tạo dữ liệu
List<Entry> entries = new ArrayList<>();
entries.add(new Entry(0, 25f));  // (x, y)
entries.add(new Entry(1, 27f));

// Tạo dataset
LineDataSet dataSet = new LineDataSet(entries, "Temperature");
dataSet.setColor(Color.BLUE);
dataSet.setLineWidth(3f);
dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);  // Làm mượt

// Gán vào chart
LineData lineData = new LineData(dataSet);
chart.setData(lineData);
chart.invalidate();  // Vẽ lại
```

#### **BarChart**
Biểu đồ cột - Dùng cho thống kê 4 chỉ số

```java
BarChart chart = findViewById(R.id.weatherStatsChart);

// Tạo dữ liệu
List<BarEntry> entries = new ArrayList<>();
entries.add(new BarEntry(0, 75f));   // Độ ẩm
entries.add(new BarEntry(1, 12.5f)); // Gió
entries.add(new BarEntry(2, 101.3f)); // Áp suất
entries.add(new BarEntry(3, 50f));   // UV

// Tạo dataset
BarDataSet dataSet = new BarDataSet(entries, "Stats");
int[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.RED};
dataSet.setColors(colors);

// Gán vào chart
BarData barData = new BarData(dataSet);
barData.setBarWidth(0.7f);
chart.setData(barData);
chart.invalidate();
```

### 🎯 Tính Năng Sử Dụng

| Tính Năng | Mô Tả | Code |
|-----------|-------|------|
| **Animation** | Hiệu ứng chuyển động khi vẽ | `chart.animateXY(1200, 1200)` |
| **Touch** | Chạm vào điểm để xem giá trị | `chart.setTouchEnabled(true)` |
| **Drag** | Kéo biểu đồ ngang | `chart.setDragEnabled(true)` |
| **Zoom** | Phóng to/thu nhỏ | `chart.setScaleEnabled(false)` |
| **Fill** | Tô màu dưới đường | `dataSet.setDrawFilled(true)` |
| **Smooth** | Làm mượt đường cong | `setMode(CUBIC_BEZIER)` |
| **Formatter** | Custom hiển thị giá trị | `setValueFormatter()` |
| **Grid** | Đường lưới | `setDrawGridLines(true)` |

---

## 7. CẤU TRÚC CODE

### 📋 Class Structure - ChartsActivity.java

```java
public class ChartsActivity extends AppCompatActivity {
    
    // ═══════════════════════════════════════════════════════════
    // VARIABLES
    // ═══════════════════════════════════════════════════════════
    
    private HourlyForecastResponse hourlyForecastData;  // Dữ liệu dự báo
    private WeatherResponse currentWeatherData;         // Dữ liệu hiện tại
    private int currentUVIndex;                         // Chỉ số UV
    private String windSpeedUnit = "ms";                // Đơn vị gió
    
    
    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE METHODS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Nhận dữ liệu từ Intent
        // Load cài đặt
        // Setup UI
        // Khởi tạo 5 biểu đồ
    }
    
    
    // ═══════════════════════════════════════════════════════════
    // CHART SETUP METHODS (Public - Main Setup)
    // ═══════════════════════════════════════════════════════════
    
    private void setupTemperatureChart() {
        // ① Kiểm tra dữ liệu
        // ② Tìm view
        // ③ Tạo entries
        // ④ Tạo dataset + style
        // ⑤ Gán dữ liệu
        // ⑥ Cấu hình chung
        // ⑦ Custom formatter
        // ⑧ Animation
    }
    
    private void setupWeatherStatsChart() { /* ... */ }
    private void setupRainProbabilityChart() { /* ... */ }
    private void setupWindSpeedChart() { /* ... */ }
    private void setupHumidityChart() { /* ... */ }
    
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS (Private - Common Configuration)
    // ═══════════════════════════════════════════════════════════
    
    private void setupChart(LineChart chart) {
        // Cấu hình chung cho biểu đồ đường:
        // - Description: tắt
        // - Legend: tắt
        // - Grid background: tắt
        // - Right axis: tắt
        // - X axis: vị trí, màu, size, grid
        // - Left axis: màu, size, grid
        // - Touch: bật/tắt
        // - Offsets: padding
    }
    
    private void setupBarChart(BarChart chart) {
        // Cấu hình chung cho biểu đồ cột
        // Tương tự setupChart() nhưng:
        // - X axis có custom formatter cho labels
        // - Touch: tắt (không cần tương tác)
        // - FitBars: true (tự động fit cột)
    }
}
```

### 🔧 Method Organization

```
ChartsActivity.java (600+ lines)
│
├─ Variables (4 biến)
│  ├─ hourlyForecastData
│  ├─ currentWeatherData
│  ├─ currentUVIndex
│  └─ windSpeedUnit
│
├─ Lifecycle
│  └─ onCreate() - 30 lines
│
├─ Chart Setup Methods (5 methods)
│  ├─ setupTemperatureChart() - 80 lines
│  ├─ setupWeatherStatsChart() - 120 lines
│  ├─ setupRainProbabilityChart() - 60 lines
│  ├─ setupWindSpeedChart() - 60 lines
│  └─ setupHumidityChart() - 60 lines
│
└─ Helper Methods (2 methods)
   ├─ setupChart(LineChart) - 40 lines
   └─ setupBarChart(BarChart) - 50 lines
```

### 📊 Code Metrics

| Metric | Value |
|--------|-------|
| Tổng số dòng | ~600 lines |
| Số methods | 8 methods |
| Số biểu đồ | 5 charts |
| Số view tương tác | 6 views (5 charts + 1 button) |
| Dependencies | 1 (MPAndroidChart) |
| API calls | 0 (dùng dữ liệu từ MainActivity) |

---

## 🎨 UI/UX DETAILS

### Layout Structure

```
activity_charts.xml
│
└─ ScrollView (cho phép cuộn)
   └─ LinearLayout (vertical)
      ├─ Header LinearLayout
      │  ├─ ImageButton (btnBack)
      │  └─ TextView (tvChartTitle)
      │
      └─ Charts LinearLayout (vertical, padding 16dp)
         ├─ include: card_temperature_chart.xml
         ├─ include: card_weather_stats_chart.xml
         ├─ include: card_rain_probability_chart.xml
         ├─ include: card_wind_speed_chart.xml
         └─ include: card_humidity_chart.xml
```

### Card Layout Pattern

Mỗi card đều có cấu trúc tương tự:

```xml
<!-- card_temperature_chart.xml -->
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Icon + Title -->
        <LinearLayout android:orientation="horizontal">
            <ImageView android:src="@drawable/ic_temperature" />
            <TextView android:text="Temperature Trend" />
        </LinearLayout>
        
        <!-- Chart -->
        <com.github.mikephil.charting.charts.LineChart
            android:id="@+id/temperatureChart"
            android:layout_width="match_parent"
            android:layout_height="220dp" />
            
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### Animation Timeline

```
User opens ChartsActivity
    ↓
    t=0ms: Activity appears
    t=100ms: Layout rendered
    ↓
    t=100ms-300ms: Temperature Chart animates
    t=300ms-500ms: Weather Stats Chart animates
    t=500ms-700ms: Rain Chart animates
    t=700ms-900ms: Wind Chart animates
    t=900ms-1100ms: Humidity Chart animates
    ↓
    t=1200ms: All animations complete
```

---

## 🔍 DEBUGGING & TESTING

### Common Issues

**1. Chart không hiển thị**
```java
// Kiểm tra:
- Dữ liệu có null không?
- View có tồn tại trong layout không?
- invalidate() đã được gọi chưa?
```

**2. Giá trị hiển thị sai**
```java
// Kiểm tra:
- Đơn vị có đúng không? (m/s vs km/h)
- Có cần chuyển đổi không? (* 100 cho %)
- ValueFormatter có đúng không?
```

**3. Animation không mượt**
```java
// Thử:
- Giảm số lượng điểm dữ liệu
- Tăng thời gian animation
- Kiểm tra performance device
```

### Test Cases

```
✓ Test 1: Dữ liệu null - Chart không crash
✓ Test 2: Dữ liệu rỗng - Chart trống
✓ Test 3: 1 điểm dữ liệu - Hiển thị 1 điểm
✓ Test 4: 12 điểm dữ liệu - Hiển thị đầy đủ
✓ Test 5: 40 điểm dữ liệu - Chỉ hiển thị 12 điểm
✓ Test 6: Đổi đơn vị gió - Giá trị thay đổi đúng
✓ Test 7: Nhấn Back - Quay về MainActivity
✓ Test 8: Chạm vào điểm - Hiện giá trị
✓ Test 9: Kéo biểu đồ - Scroll ngang
✓ Test 10: Scroll activity - Xem tất cả biểu đồ
```

---

## 📝 CHANGE LOG

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-11 | ✨ Initial release với 5 biểu đồ cơ bản |
| 1.1.0 | 2024-11 | 🎨 Thêm gradient fill và animation |
| 1.2.0 | 2024-11 | 📊 Custom formatter cho trục X và labels |
| 1.3.0 | 2024-11 | 🌐 Thêm hỗ trợ chuyển đổi đơn vị |

---

## 🚀 FUTURE IMPROVEMENTS

### Planned Features

- [ ] **Zoom functionality** - Cho phép zoom từng biểu đồ
- [ ] **Export chart as image** - Lưu biểu đồ thành ảnh
- [ ] **Share chart** - Chia sẻ biểu đồ lên mạng xã hội
- [ ] **Dark mode** - Tối ưu màu sắc cho chế độ tối
- [ ] **More chart types** - Thêm biểu đồ radar, pie chart
- [ ] **Historical data** - So sánh với ngày/tuần trước
- [ ] **Landscape mode** - Tối ưu cho chế độ ngang
- [ ] **Offline mode** - Cache dữ liệu để xem offline

### Performance Optimization

- [ ] Lazy loading charts (chỉ load khi scroll đến)
- [ ] RecyclerView thay vì ScrollView
- [ ] Reduce animation duration
- [ ] Cache rendered bitmaps

---

## 📚 REFERENCES

### Documentation
- [MPAndroidChart Wiki](https://github.com/PhilJay/MPAndroidChart/wiki)
- [OpenWeatherMap API](https://openweathermap.org/api)
- [Android Charts Tutorial](https://developer.android.com/)

### Related Files
- `DASHBOARD_AND_ANIMATIONS.md` - Hướng dẫn animation
- `DESIGN_SYSTEM.md` - Hệ thống màu sắc và font
- `LAYOUT_SPECIFICATIONS.md` - Quy chuẩn layout

---

## 👥 CONTRIBUTORS

- **Developer:** Weather App Team
- **Library:** PhilJay (MPAndroidChart)
- **API:** OpenWeatherMap

---

## 📄 LICENSE

This documentation is part of Weather App project.

---

**Cập nhật lần cuối:** 2024-11-06
**Version:** 1.3.0

