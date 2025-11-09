# 📦 Cấu Trúc Dự Án Weather App - Đã Tối Ưu Hóa

## 🎯 Tổng Quan

Dự án đã được **cấu trúc lại theo chuẩn Layer-based Architecture** với các cải tiến sau:

### ✅ Những gì đã được thêm vào:

1. **BaseActivity** - Class base cho tất cả Activity
2. **Constants** - Tập trung tất cả hằng số
3. **NetworkUtils** - Kiểm tra kết nối mạng
4. **DateUtils** - Xử lý ngày tháng
5. **PermissionUtils** - Quản lý quyền
6. **PreferenceManager** - Quản lý SharedPreferences
7. **WeatherRepository** - Tập trung data access logic
8. **WeatherMapper** - Chuyển đổi API response sang domain model
9. **ViewHolders** - Tách ViewHolder ra khỏi Adapter

---

## 📂 Cấu Trúc Package Mới

```
com.example.weatherapp/
├── 📱 ui/
│   ├── base/                           ← MỚI
│   │   └── BaseActivity.java          ← Tất cả Activity extend từ đây
│   ├── activities/
│   │   ├── MainActivity.java
│   │   ├── SearchActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── OutfitSuggestionActivity.java
│   │   ├── ChartsActivity.java
│   │   ├── FavoriteCitiesActivity.java
│   │   └── WeatherDetailsActivity.java
│   ├── adapters/
│   │   ├── OutfitSuggestionAdapter.java
│   │   └── CityWeatherAdapter.java
│   └── viewholders/                    ← MỚI
│       ├── OutfitSuggestionViewHolder.java
│       └── CityWeatherViewHolder.java
│
├── 💾 data/
│   ├── models/
│   │   ├── HourlyForecast.java
│   │   ├── WeeklyForecast.java
│   │   ├── OutfitSuggestion.java
│   │   ├── FavoriteCity.java
│   │   └── WeatherAlert.java
│   ├── api/
│   │   ├── WeatherApiService.java
│   │   ├── RetrofitClient.java
│   │   └── OpenAIService.java
│   ├── responses/
│   │   ├── WeatherResponse.java
│   │   ├── HourlyForecastResponse.java
│   │   ├── AirQualityResponse.java
│   │   ├── UVIndexResponse.java
│   │   └── WeatherAlertsResponse.java
│   ├── repository/                     ← MỚI
│   │   └── WeatherRepository.java     ← Tập trung logic data access
│   ├── mapper/                         ← MỚI
│   │   └── WeatherMapper.java         ← Convert Response → Model
│   └── local/                          ← MỚI
│       └── prefs/
│           └── PreferenceManager.java  ← Quản lý SharedPreferences
│
├── 🔧 utils/
│   ├── Constants.java                  ← MỚI - Tất cả hằng số
│   ├── NetworkUtils.java               ← MỚI - Kiểm tra mạng
│   ├── DateUtils.java                  ← MỚI - Xử lý ngày tháng
│   ├── PermissionUtils.java            ← MỚI - Quản lý quyền
│   ├── LocaleHelper.java
│   ├── BlurHelper.java
│   ├── FavoriteCitiesManager.java
│   └── OutfitSuggestionService.java
│
├── 🔔 notification/
│   ├── WeatherNotificationManager.java
│   ├── WeatherNotificationWorker.java
│   └── NotificationReceiver.java
│
└── 📺 widget/
    └── WeatherWidget.java
```

---

## 🚀 Cách Sử Dụng Các Class Mới

### 1️⃣ BaseActivity

**Trước đây** (trong MainActivity.java):
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        loadData();
    }
}
```

**Bây giờ** (extend BaseActivity):
```java
public class MainActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }
    
    @Override
    protected void initViews() {
        // Khởi tạo views
    }
    
    @Override
    protected void setupListeners() {
        // Setup listeners
    }
    
    @Override
    protected void loadInitialData() {
        // Load data
    }
}
```

**Lợi ích:**
- ✅ Code sạch hơn, chuẩn hóa
- ✅ Tự động apply locale
- ✅ Có sẵn methods: showLoading(), showError(), isNetworkAvailable()

---

### 2️⃣ Constants Class

**Trước đây** (hardcode khắp nơi):
```java
// Trong MainActivity
private static final String API_KEY = "4f8cf691...";
sharedPreferences.edit().putString("temperature_unit", "celsius").apply();

// Trong SettingsActivity
private static final String KEY_TEMP = "temperature_unit";

// Trong nhiều file khác...
```

**Bây giờ** (dùng Constants):
```java
// Import 1 lần
import com.example.weatherapp.utils.Constants;

// Sử dụng
String apiKey = Constants.WEATHER_API_KEY;
prefs.edit().putString(Constants.KEY_TEMPERATURE_UNIT, Constants.UNIT_CELSIUS).apply();
```

**Lợi ích:**
- ✅ Không còn hardcode
- ✅ Thay đổi 1 chỗ, áp dụng toàn bộ
- ✅ Dễ tìm và quản lý

---

### 3️⃣ NetworkUtils

**Sử dụng:**
```java
import com.example.weatherapp.utils.NetworkUtils;

// Kiểm tra kết nối internet
if (!NetworkUtils.isNetworkAvailable(this)) {
    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
    return;
}

// Kiểm tra WiFi
if (NetworkUtils.isWifiConnected(this)) {
    // Download large data
}
```

---

### 4️⃣ DateUtils

**Sử dụng:**
```java
import com.example.weatherapp.utils.DateUtils;

// Format timestamp thành giờ
String hour = DateUtils.getHour(1699876800); // "14:00"

// Lấy tên ngày
String dayName = DateUtils.getDayName(1699876800); // "Monday"

// Kiểm tra hôm nay
boolean isToday = DateUtils.isToday(timestamp);

// Format custom
String formatted = DateUtils.formatTimestamp(timestamp, "dd/MM/yyyy HH:mm");
```

---

### 5️⃣ PermissionUtils

**Sử dụng:**
```java
import com.example.weatherapp.utils.PermissionUtils;

// Kiểm tra quyền location
if (!PermissionUtils.hasLocationPermission(this)) {
    PermissionUtils.requestLocationPermission(this);
} else {
    // Get location
}

// Kiểm tra quyền notification
if (!PermissionUtils.hasNotificationPermission(this)) {
    PermissionUtils.requestNotificationPermission(this);
}
```

---

### 6️⃣ PreferenceManager

**Trước đây**:
```java
SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
String unit = prefs.getString("temperature_unit", "celsius");
prefs.edit().putString("temperature_unit", "fahrenheit").apply();
```

**Bây giờ**:
```java
PreferenceManager prefManager = new PreferenceManager(this);

// Đọc
String unit = prefManager.getTemperatureUnit();
String lastCity = prefManager.getLastCity();

// Ghi
prefManager.setTemperatureUnit("fahrenheit");
prefManager.setLastCity("Tokyo");
```

**Lợi ích:**
- ✅ Type-safe
- ✅ Không cần nhớ key strings
- ✅ Default values tập trung

---

### 7️⃣ WeatherRepository

**Trước đây** (trong MainActivity):
```java
WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
Call<WeatherResponse> call = apiService.getWeatherByCity(cityName, API_KEY, "metric");
call.enqueue(new Callback<WeatherResponse>() {
    // Handle response...
});
```

**Bây giờ**:
```java
WeatherRepository repository = new WeatherRepository(this);

repository.getWeatherByCity("Hanoi", new WeatherRepository.WeatherCallback() {
    @Override
    public void onSuccess(WeatherResponse response) {
        // Update UI
    }
    
    @Override
    public void onError(String error) {
        // Show error
    }
});
```

**Lợi ích:**
- ✅ Tập trung logic API calls
- ✅ Tự động handle units từ settings
- ✅ Clean callbacks
- ✅ Dễ test và mock

---

### 8️⃣ WeatherMapper

**Sử dụng:**
```java
import com.example.weatherapp.data.mapper.WeatherMapper;

// Lấy dữ liệu từ Response
String cityName = WeatherMapper.getCityName(weatherResponse);
double temp = WeatherMapper.getTemperature(weatherResponse);
String description = WeatherMapper.getWeatherDescription(weatherResponse);

// Convert forecast list
List<HourlyForecast> forecasts = WeatherMapper.mapHourlyForecast(forecastResponse);
```

**Lợi ích:**
- ✅ Tách biệt API response và business logic
- ✅ Null-safe
- ✅ Dễ test

---

### 9️⃣ ViewHolders

**Trước đây** (trong Adapter):
```java
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        // ... init and bind logic here
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.tvName.setText(item.getName());
        // ... many lines of binding code
    }
}
```

**Bây giờ** (tách ViewHolder):
```java
// Adapter
public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(items.get(position));
    }
}

// ViewHolder (file riêng)
public class MyViewHolder extends RecyclerView.ViewHolder {
    public void bind(Item item) {
        // All binding logic here
    }
}
```

**Lợi ích:**
- ✅ Adapter ngắn gọn hơn
- ✅ ViewHolder có thể reuse
- ✅ Dễ test

---

## ⚡ Các Bước Tiếp Theo

### 🔄 Refactor các Activity hiện có

**Cần làm:**
1. ✅ Update MainActivity extend BaseActivity
2. ✅ Thay tất cả hardcoded strings bằng Constants
3. ✅ Sử dụng PreferenceManager thay vì SharedPreferences trực tiếp
4. ✅ Sử dụng WeatherRepository thay vì gọi API trực tiếp
5. ✅ Sử dụng DateUtils để format date/time
6. ✅ Sử dụng NetworkUtils để check network
7. ✅ Sử dụng PermissionUtils để xử lý permissions

### 📝 Ví dụ Refactor MainActivity

**Trước:**
```java
private static final String API_KEY = "4f8cf691...";
SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
String unit = prefs.getString("temperature_unit", "celsius");

WeatherApiService api = RetrofitClient.getInstance().getWeatherApi();
Call<WeatherResponse> call = api.getWeatherByCity(city, API_KEY, "metric");
```

**Sau:**
```java
private WeatherRepository repository;
private PreferenceManager prefManager;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    repository = new WeatherRepository(this);
    prefManager = new PreferenceManager(this);
}

private void fetchWeather(String city) {
    if (!NetworkUtils.isNetworkAvailable(this)) {
        showError("No internet connection");
        return;
    }
    
    showLoading();
    repository.getWeatherByCity(city, new WeatherRepository.WeatherCallback() {
        @Override
        public void onSuccess(WeatherResponse response) {
            hideLoading();
            updateUI(response);
        }
        
        @Override
        public void onError(String error) {
            hideLoading();
            showError(error);
        }
    });
}
```

---

## 📊 So Sánh Trước & Sau

| Aspect | Trước | Sau |
|--------|-------|-----|
| **Hardcoded values** | Khắp nơi | Constants class |
| **SharedPreferences** | Trực tiếp | PreferenceManager |
| **API calls** | Trong Activity | Repository |
| **Network check** | Tự implement | NetworkUtils |
| **Date formatting** | SimpleDateFormat rải rác | DateUtils |
| **Permissions** | Manual check | PermissionUtils |
| **ViewHolder** | Trong Adapter | File riêng |
| **Base Activity** | Không có | BaseActivity |

---

## ✅ Checklist Hoàn Thành

- [x] Tạo BaseActivity
- [x] Tạo Constants
- [x] Tạo NetworkUtils
- [x] Tạo DateUtils
- [x] Tạo PermissionUtils
- [x] Tạo PreferenceManager
- [x] Tạo WeatherRepository
- [x] Tạo WeatherMapper
- [x] Tạo ViewHolders
- [ ] Refactor MainActivity
- [ ] Refactor các Activity khác
- [ ] Refactor Adapters
- [ ] Update RetrofitClient sử dụng Constants
- [ ] Test tất cả tính năng

---

## 🎯 Kết Luận

**Dự án đã được cấu trúc lại theo chuẩn:**

✅ **Clean Architecture principles**
✅ **Separation of Concerns**
✅ **Single Responsibility Principle**
✅ **DRY (Don't Repeat Yourself)**
✅ **Easy to maintain và scale**

**Bước tiếp theo:** Refactor từng Activity để sử dụng các class mới này!

