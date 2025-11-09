# 📦 Android Package Structure - Best Practices Guide

## 🎯 Mục đích
Tài liệu này giải thích **cấu trúc package tốt nhất** cho dự án Android bằng Java, giúp code **dễ đọc, dễ bảo trì, dễ mở rộng**.

---

## 🏗️ CẤU TRÚC DỰ ÁN HIỆN TẠI (Weather App)

### ✅ Cấu trúc hiện tại (KHUYẾN NGHỊ - Layer-based Architecture):

```
com.example.weatherapp/
├── 📱 ui/                          # Presentation Layer (Giao diện)
│   ├── activities/                 # Các Activity
│   │   ├── MainActivity.java
│   │   ├── SearchActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── OutfitSuggestionActivity.java
│   │   ├── ChartsActivity.java
│   │   ├── FavoriteCitiesActivity.java
│   │   └── WeatherDetailsActivity.java
│   │
│   ├── fragments/                  # Các Fragment (nếu có)
│   │   ├── HomeFragment.java
│   │   ├── ForecastFragment.java
│   │   └── SettingsFragment.java
│   │
│   └── adapters/                   # RecyclerView Adapters
│       ├── OutfitSuggestionAdapter.java
│       └── CityWeatherAdapter.java
│
├── 💾 data/                        # Data Layer (Dữ liệu)
│   ├── models/                     # Data Models (POJO)
│   │   ├── WeatherData.java
│   │   ├── HourlyForecast.java
│   │   ├── WeeklyForecast.java
│   │   ├── OutfitSuggestion.java
│   │   ├── FavoriteCity.java
│   │   └── WeatherAlert.java
│   │
│   ├── api/                        # API Services
│   │   ├── WeatherApiService.java
│   │   ├── RetrofitClient.java
│   │   └── OpenAIService.java
│   │
│   ├── responses/                  # API Response Models
│   │   ├── WeatherResponse.java
│   │   ├── HourlyForecastResponse.java
│   │   ├── AirQualityResponse.java
│   │   ├── UVIndexResponse.java
│   │   └── WeatherAlertsResponse.java
│   │
│   ├── repository/                 # Repository Pattern (nếu dùng)
│   │   └── WeatherRepository.java
│   │
│   └── database/                   # Local Database (Room)
│       ├── AppDatabase.java
│       ├── dao/
│       │   ├── WeatherDao.java
│       │   └── CityDao.java
│       └── entities/
│           ├── WeatherEntity.java
│           └── CityEntity.java
│
├── 🔧 utils/                       # Utility Classes
│   ├── Constants.java              # Hằng số chung
│   ├── DateUtils.java              # Xử lý ngày tháng
│   ├── StringUtils.java            # Xử lý chuỗi
│   ├── NetworkUtils.java           # Kiểm tra mạng
│   ├── PermissionUtils.java        # Xử lý quyền
│   ├── LocaleHelper.java           # Đa ngôn ngữ
│   ├── BlurHelper.java             # Hiệu ứng blur
│   ├── FavoriteCitiesManager.java  # Quản lý favorites
│   └── OutfitSuggestionService.java # Service logic
│
├── 🔔 notification/                # Notification System
│   ├── WeatherNotificationManager.java
│   ├── WeatherNotificationWorker.java
│   └── NotificationReceiver.java
│
├── 📺 widget/                      # App Widgets
│   ├── WeatherWidget.java
│   └── WeatherWidgetProvider.java
│
├── 🎨 custom/                      # Custom Views (nếu có)
│   ├── GlassMorphismView.java
│   └── WeatherCardView.java
│
└── 🔐 security/                    # Security (nếu cần)
    ├── EncryptionHelper.java
    └── SecurePreferences.java
```

---

## 📊 SO SÁNH CÁC CÁCH TỔ CHỨC PACKAGE

### 1️⃣ **Layer-based (Feature Layer)** ⭐ KHUYẾN NGHỊ - Dự án bạn đang dùng

```
com.example.app/
├── ui/              # Tất cả UI components
├── data/            # Tất cả data-related
├── domain/          # Business logic (nếu có)
└── utils/           # Utilities
```

**✅ Ưu điểm:**
- **Dễ hiểu** cho người mới
- **Phân tách rõ ràng** theo chức năng kỹ thuật
- **Phù hợp** với Clean Architecture
- **Dễ refactor** khi chuyển từ Activity sang Fragment
- **IDE hỗ trợ tốt** (auto-import, navigation)

**❌ Nhược điểm:**
- Khó khăn khi cần xóa một feature hoàn chỉnh (phải tìm ở nhiều folder)
- Các file liên quan đến 1 feature nằm rải rác

**🎯 Phù hợp cho:**
- Dự án vừa và nhỏ (như Weather App của bạn)
- Team mới bắt đầu với Android
- App có ít tính năng độc lập

---

### 2️⃣ **Feature-based (Modular by Feature)** 🚀 CHO DỰ ÁN LỚN

```
com.example.app/
├── feature/
│   ├── weather/
│   │   ├── WeatherActivity.java
│   │   ├── WeatherAdapter.java
│   │   ├── WeatherViewModel.java
│   │   └── WeatherRepository.java
│   │
│   ├── forecast/
│   │   ├── ForecastActivity.java
│   │   ├── ForecastAdapter.java
│   │   └── ForecastViewModel.java
│   │
│   └── settings/
│       ├── SettingsActivity.java
│       └── SettingsViewModel.java
│
├── core/            # Shared components
│   ├── api/
│   ├── database/
│   └── utils/
│
└── common/          # Common UI/Utils
```

**✅ Ưu điểm:**
- **Dễ module hóa** - mỗi feature có thể tách thành module riêng
- **Xóa feature dễ dàng** - chỉ cần xóa 1 folder
- **Team làm việc song song** - mỗi team làm 1 feature
- **Scalability tốt** cho app lớn

**❌ Nhược điểm:**
- **Phức tạp** cho dự án nhỏ
- **Duplicate code** nếu không quản lý shared components tốt
- **Learning curve cao** cho người mới

**🎯 Phù hợp cho:**
- Dự án lớn với nhiều features độc lập
- Team đông (5+ developers)
- App có kế hoạch modular architecture

---

### 3️⃣ **Hybrid Approach** 🎨 KẾT HỢP CẢ 2

```
com.example.app/
├── features/                    # Major features
│   ├── weather/
│   │   ├── ui/
│   │   ├── data/
│   │   └── domain/
│   │
│   └── forecast/
│       ├── ui/
│       ├── data/
│       └── domain/
│
├── ui/                          # Shared UI
│   ├── base/
│   └── custom/
│
├── data/                        # Shared data
│   ├── api/
│   └── database/
│
└── utils/                       # Shared utils
```

---

## 🎯 KHUYẾN NGHỊ CHO DỰ ÁN CỦA BẠN

### ✅ Dự án Weather App của bạn đã tốt! Nhưng có thể cải thiện:

### **Option 1: Giữ nguyên (Layer-based) + Cải tiến nhỏ** ⭐ KHUYẾN NGHỊ

```
com.example.weatherapp/
├── ui/
│   ├── activities/
│   ├── fragments/              # ← THÊM (nếu cần Fragment)
│   ├── adapters/
│   ├── viewholders/            # ← THÊM (tách ViewHolder ra)
│   └── base/                   # ← THÊM (BaseActivity, BaseFragment)
│       ├── BaseActivity.java
│       └── BaseFragment.java
│
├── data/
│   ├── models/
│   ├── api/
│   ├── responses/
│   ├── repository/             # ← THÊM (Repository pattern)
│   │   ├── WeatherRepository.java
│   │   └── SettingsRepository.java
│   │
│   └── local/                  # ← ĐỔI TÊN từ "database"
│       ├── prefs/              # SharedPreferences wrappers
│       │   ├── PreferenceManager.java
│       │   └── SettingsPrefs.java
│       │
│       └── database/           # Room Database (nếu dùng)
│           ├── AppDatabase.java
│           └── dao/
│
├── domain/                     # ← THÊM (Business Logic Layer)
│   ├── usecases/               # Use cases cho Clean Architecture
│   │   ├── GetWeatherUseCase.java
│   │   ├── SaveFavoriteCityUseCase.java
│   │   └── GetOutfitSuggestionUseCase.java
│   │
│   └── mapper/                 # Data mappers
│       ├── WeatherMapper.java
│       └── ForecastMapper.java
│
├── utils/
│   ├── Constants.java          # ← THÊM
│   ├── DateUtils.java          # ← THÊM
│   ├── NetworkUtils.java       # ← THÊM
│   ├── PermissionUtils.java    # ← THÊM
│   ├── extensions/             # ← THÊM (Kotlin-style extensions for Java)
│   │   ├── ViewExtensions.java
│   │   └── StringExtensions.java
│   │
│   └── helpers/                # ← ĐỔI TÊN từ các service
│       ├── LocaleHelper.java
│       ├── BlurHelper.java
│       ├── FavoriteCitiesManager.java
│       └── OutfitSuggestionService.java
│
├── notification/               # ✅ GIỮ NGUYÊN
├── widget/                     # ✅ GIỮ NGUYÊN
│
└── di/                         # ← THÊM (Dependency Injection - nếu dùng Dagger/Hilt)
    ├── modules/
    │   ├── AppModule.java
    │   ├── NetworkModule.java
    │   └── DatabaseModule.java
    └── AppComponent.java
```

---

### **Option 2: Chuyển sang Feature-based** 🚀 CHO TƯƠNG LAI

**Nếu app phát triển lớn hơn, bạn có thể refactor sang:**

```
com.example.weatherapp/
├── features/
│   ├── home/                   # Màn hình chính
│   │   ├── HomeActivity.java
│   │   ├── HomeViewModel.java
│   │   └── HomeRepository.java
│   │
│   ├── forecast/               # Dự báo thời tiết
│   │   ├── ForecastActivity.java
│   │   ├── ForecastAdapter.java
│   │   └── ForecastViewModel.java
│   │
│   ├── outfit/                 # Gợi ý trang phục
│   │   ├── OutfitSuggestionActivity.java
│   │   ├── OutfitSuggestionAdapter.java
│   │   ├── OutfitSuggestionViewModel.java
│   │   └── OutfitSuggestionService.java
│   │
│   ├── charts/                 # Biểu đồ
│   │   ├── ChartsActivity.java
│   │   └── ChartsViewModel.java
│   │
│   ├── favorites/              # Thành phố yêu thích
│   │   ├── FavoriteCitiesActivity.java
│   │   ├── CityWeatherAdapter.java
│   │   ├── FavoriteCitiesViewModel.java
│   │   └── FavoriteCitiesManager.java
│   │
│   ├── search/                 # Tìm kiếm
│   │   ├── SearchActivity.java
│   │   └── SearchViewModel.java
│   │
│   └── settings/               # Cài đặt
│       ├── SettingsActivity.java
│       └── SettingsViewModel.java
│
├── core/                       # Core shared components
│   ├── api/
│   │   ├── WeatherApiService.java
│   │   ├── RetrofitClient.java
│   │   └── interceptors/
│   │
│   ├── database/
│   │   ├── AppDatabase.java
│   │   └── dao/
│   │
│   ├── models/                 # Shared models
│   │   ├── Weather.java
│   │   ├── City.java
│   │   └── Forecast.java
│   │
│   └── preferences/
│       └── PreferenceManager.java
│
├── common/                     # Common UI/Utils
│   ├── base/
│   │   ├── BaseActivity.java
│   │   ├── BaseFragment.java
│   │   └── BaseViewModel.java
│   │
│   ├── adapters/               # Base adapters
│   │   └── BaseRecyclerAdapter.java
│   │
│   └── utils/
│       ├── Constants.java
│       ├── DateUtils.java
│       └── NetworkUtils.java
│
├── notification/
└── widget/
```

---

## 📝 QUY TẮC ĐẶT TÊN PACKAGE

### ✅ DO (Nên làm):

1. **Chữ thường toàn bộ**: `com.example.weatherapp`
2. **Không dấu gạch dưới**: `utils` ✅ (không phải `util_helpers` ❌)
3. **Tên ngắn gọn, rõ ràng**: `ui`, `data`, `utils`
4. **Số nhiều cho collections**: `activities`, `adapters`, `models`
5. **Tên theo chức năng**: `notification`, `widget`, `security`

### ❌ DON'T (Không nên):

1. ❌ Chữ hoa: `UI`, `Data`
2. ❌ Tên dài: `userInterfaceComponents`
3. ❌ Viết tắt khó hiểu: `act`, `frag`, `adp`
4. ❌ Trùng tên với Java packages: `java.utils` (dùng `utils` thay vì `util`)

---

## 🎯 QUY TẮC ĐẶT TÊN CLASS

### Activities:
```java
✅ MainActivity.java
✅ SearchActivity.java
✅ SettingsActivity.java
❌ Main.java
❌ Search.java
```

### Fragments:
```java
✅ HomeFragment.java
✅ ForecastFragment.java
❌ HomeScreen.java
❌ Home.java
```

### Adapters:
```java
✅ CityWeatherAdapter.java
✅ OutfitSuggestionAdapter.java
❌ CityAdapter.java (too generic)
❌ WeatherList.java
```

### ViewHolders:
```java
✅ CityWeatherViewHolder.java
✅ OutfitSuggestionViewHolder.java
```

### Models (POJO):
```java
✅ Weather.java
✅ City.java
✅ ForecastItem.java
❌ WeatherModel.java (dư thừa)
❌ WeatherPOJO.java
```

### API Response Models:
```java
✅ WeatherResponse.java
✅ ForecastResponse.java
✅ ApiResponse.java
```

### Repositories:
```java
✅ WeatherRepository.java
✅ SettingsRepository.java
❌ WeatherRepo.java
```

### ViewModels:
```java
✅ MainViewModel.java
✅ WeatherViewModel.java
❌ MainVM.java
```

### Utils:
```java
✅ DateUtils.java
✅ StringUtils.java
✅ NetworkUtils.java
❌ DateHelper.java (dùng Utils thống nhất)
❌ Utility.java (too generic)
```

### Managers:
```java
✅ NetworkManager.java
✅ FavoriteCitiesManager.java
✅ NotificationManager.java
```

### Services:
```java
✅ WeatherApiService.java (interface)
✅ OutfitSuggestionService.java (logic service)
✅ LocationService.java
```

### Helpers:
```java
✅ LocaleHelper.java
✅ BlurHelper.java
✅ PermissionHelper.java
```

### Constants:
```java
✅ Constants.java
✅ ApiConstants.java
✅ AppConfig.java
❌ Const.java
```

---

## 🏛️ CLEAN ARCHITECTURE với Package Structure

### Cấu trúc 3 Layer chuẩn:

```
com.example.weatherapp/
├── presentation/               # UI Layer (Activity, Fragment, ViewModel)
│   ├── activities/
│   ├── fragments/
│   ├── viewmodels/
│   └── adapters/
│
├── domain/                     # Business Logic Layer (Use Cases)
│   ├── usecases/
│   │   ├── GetWeatherUseCase.java
│   │   ├── SaveCityUseCase.java
│   │   └── GetForecastUseCase.java
│   │
│   ├── models/                 # Domain models (pure Java)
│   │   ├── Weather.java
│   │   └── City.java
│   │
│   └── repositories/           # Repository interfaces
│       ├── IWeatherRepository.java
│       └── ICityRepository.java
│
└── data/                       # Data Layer (API, Database, Cache)
    ├── repositories/           # Repository implementations
    │   ├── WeatherRepositoryImpl.java
    │   └── CityRepositoryImpl.java
    │
    ├── remote/                 # Remote data source (API)
    │   ├── api/
    │   │   ├── WeatherApiService.java
    │   │   └── RetrofitClient.java
    │   │
    │   └── dto/                # Data Transfer Objects
    │       ├── WeatherResponse.java
    │       └── ForecastResponse.java
    │
    ├── local/                  # Local data source (Database, SharedPrefs)
    │   ├── database/
    │   │   ├── AppDatabase.java
    │   │   └── dao/
    │   │
    │   └── preferences/
    │       └── PreferenceManager.java
    │
    └── mapper/                 # DTO ↔ Domain Model mapping
        ├── WeatherMapper.java
        └── ForecastMapper.java
```

**📌 Lợi ích của Clean Architecture:**
- ✅ **Tách biệt hoàn toàn** giữa UI, Business Logic, và Data
- ✅ **Dễ test** (mock repository, test use cases độc lập)
- ✅ **Dễ thay đổi** data source (API → Database) mà không ảnh hưởng UI
- ✅ **Scalable** cho dự án lớn

---

## 💡 BEST PRACTICES

### 1. **Tách Base Classes**
```java
// ui/base/BaseActivity.java
public abstract class BaseActivity extends AppCompatActivity {
    protected abstract int getLayoutId();
    protected abstract void initViews();
    protected abstract void setupListeners();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initViews();
        setupListeners();
    }
}
```

### 2. **Sử dụng Constants Class**
```java
// utils/Constants.java
public class Constants {
    // API
    public static final String BASE_URL = "https://api.openweathermap.org/";
    public static final String API_KEY = "your_api_key";
    
    // SharedPreferences Keys
    public static final String PREFS_NAME = "WeatherAppPrefs";
    public static final String KEY_TEMPERATURE_UNIT = "temp_unit";
    
    // Request Codes
    public static final int REQUEST_LOCATION = 100;
    
    // Intent Extras
    public static final String EXTRA_CITY_NAME = "city_name";
    
    private Constants() {} // Prevent instantiation
}
```

### 3. **Sử dụng ViewHolder Pattern**
```java
// ui/viewholders/CityWeatherViewHolder.java
public class CityWeatherViewHolder extends RecyclerView.ViewHolder {
    private TextView tvCityName;
    private TextView tvTemperature;
    
    public CityWeatherViewHolder(View itemView) {
        super(itemView);
        tvCityName = itemView.findViewById(R.id.tvCityName);
        tvTemperature = itemView.findViewById(R.id.tvTemperature);
    }
    
    public void bind(City city) {
        tvCityName.setText(city.getName());
        tvTemperature.setText(city.getTemperature() + "°C");
    }
}
```

### 4. **Tách Response Models khỏi Domain Models**
```java
// data/responses/WeatherResponse.java (từ API)
public class WeatherResponse {
    @SerializedName("temp")
    private double temperature;
    @SerializedName("weather_desc")
    private String description;
    // ... API-specific fields
}

// data/models/Weather.java (domain model)
public class Weather {
    private double temperature;
    private String description;
    private String cityName;
    // ... clean business fields
}

// data/mapper/WeatherMapper.java
public class WeatherMapper {
    public static Weather fromResponse(WeatherResponse response) {
        Weather weather = new Weather();
        weather.setTemperature(response.getTemperature());
        weather.setDescription(response.getDescription());
        return weather;
    }
}
```

### 5. **Repository Pattern**
```java
// data/repository/WeatherRepository.java
public class WeatherRepository {
    private WeatherApiService apiService;
    private WeatherDao weatherDao;
    
    public WeatherRepository(WeatherApiService apiService, WeatherDao weatherDao) {
        this.apiService = apiService;
        this.weatherDao = weatherDao;
    }
    
    public LiveData<Weather> getWeather(String cityName) {
        // Try cache first
        LiveData<Weather> cached = weatherDao.getWeather(cityName);
        if (cached != null) return cached;
        
        // Fetch from API
        return fetchFromApi(cityName);
    }
    
    private LiveData<Weather> fetchFromApi(String cityName) {
        // API call logic
    }
}
```

---

## 🎨 PACKAGE STRUCTURE CHO CÁC LOẠI DỰ ÁN

### 📱 **Small App (1-5 screens)** - Dùng Layer-based đơn giản
```
com.example.app/
├── ui/
│   ├── MainActivity.java
│   ├── DetailsActivity.java
│   └── SimpleAdapter.java
├── models/
│   └── Item.java
└── utils/
    └── Constants.java
```

### 🏢 **Medium App (5-15 screens)** - Layer-based + Base classes
```
com.example.app/
├── ui/
│   ├── base/
│   ├── activities/
│   ├── fragments/
│   └── adapters/
├── data/
│   ├── models/
│   ├── api/
│   └── repository/
└── utils/
```

### 🏭 **Large App (15+ screens)** - Feature-based hoặc Clean Architecture
```
com.example.app/
├── features/
│   ├── feature1/
│   ├── feature2/
│   └── feature3/
├── core/
└── common/
```

---

## 🔍 CÁCH CHỌN CẤU TRÚC PHÀNH HỢP

### Chọn **Layer-based** khi:
- ✅ Dự án nhỏ/vừa (< 20 màn hình)
- ✅ Team nhỏ (1-3 developers)
- ✅ Thời gian tight (cần code nhanh)
- ✅ App đơn giản, không nhiều features độc lập

### Chọn **Feature-based** khi:
- ✅ Dự án lớn (20+ màn hình)
- ✅ Team đông (5+ developers)
- ✅ Nhiều features độc lập
- ✅ Có kế hoạch modularization

### Chọn **Clean Architecture** khi:
- ✅ Dự án dài hạn (2+ năm)
- ✅ Cần test coverage cao
- ✅ Nhiều platforms (Android, iOS, Web share business logic)
- ✅ Team experienced với Clean Architecture

---

## 📚 TÀI LIỆU THAM KHẢO

### Official Android Guidelines:
- [Android App Architecture Guide](https://developer.android.com/jetpack/guide)
- [Package by Feature, not Layer](https://phauer.com/2020/package-by-feature/)

### Clean Architecture:
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Clean Architecture Sample](https://github.com/android/architecture-samples)

---

## ✅ CHECKLIST KHI TỔ CHỨC PACKAGE

- [ ] Package names đều là chữ thường
- [ ] Không có package trống (empty package)
- [ ] Mỗi class đặt đúng package theo chức năng
- [ ] Có Base classes cho Activity, Fragment, Adapter
- [ ] Tách API Response models khỏi Domain models
- [ ] Constants được tổ chức tốt
- [ ] Utils không trở thành "god class" (quá nhiều methods)
- [ ] Có README.md giải thích cấu trúc
- [ ] IDE không warning về package structure

---

## 🎯 KẾT LUẬN

### Cho dự án Weather App của bạn:

**✅ KHUYẾN NGHỊ: Giữ Layer-based hiện tại + Cải tiến nhỏ**

**Lý do:**
1. Dự án vừa phải (7 activities)
2. Cấu trúc hiện tại đã tốt
3. Dễ bảo trì
4. Team nhỏ

**Cải tiến nên làm:**
1. ✅ Thêm `ui/base/` cho BaseActivity, BaseFragment
2. ✅ Thêm `ui/viewholders/` tách ViewHolder ra khỏi Adapter
3. ✅ Thêm `data/repository/` cho Repository pattern
4. ✅ Thêm `utils/Constants.java` cho các hằng số
5. ✅ Thêm `data/mapper/` cho mapping logic
6. ✅ Đổi tên `utils/OutfitSuggestionService.java` → `utils/helpers/`

---

**📌 Nhớ rằng:** Cấu trúc tốt nhất là cấu trúc phù hợp với **quy mô dự án** và **kỹ năng team**. Đừng over-engineer!

**Good luck with your project! 🚀**

