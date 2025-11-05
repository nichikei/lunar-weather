# PHÂN CÔNG CÔNG VIỆC DỰA TRÊN CODE THỰC TẾ - 4 NGƯỜI

## 📊 TRẠNG THÁI DỰ ÁN HIỆN TẠI
Dự án đã có **CẤU TRÚC HOÀN CHỈNH** với tất cả các file chính:
- ✅ 7 Activities đã tạo
- ✅ 2 Adapters đã có
- ✅ 3 API Services đã setup
- ✅ 6 Models đã định nghĩa
- ✅ 4 Utils helpers đã code
- ✅ 3 Notification files đã có
- ✅ 1 Widget đã tạo

**➡️ Công việc chính: HOÀN THIỆN LOGIC, TÍCH HỢP, TEST VÀ POLISH UI**

---

## 🎯 PHÂN LOẠI ĐỘ KHÓ

### 🟢 DỄ NHẤT (Người mới/ít kinh nghiệm)
- **Người 4**: Settings & UI Improvements
- Lý do: Chủ yếu làm việc với UI, SharedPreferences, không cần API phức tạp

### 🟡 VỪA PHẢI (Có chút kinh nghiệm)
- **Người 2**: Search & Favorites
- Lý do: Cần hiểu RecyclerView, GPS, nhưng logic đơn giản

### 🟠 NÂNG CAO (Kinh nghiệm tốt)
- **Người 1**: Main Weather & API Integration  
- Lý do: Phải hiểu API, Retrofit, parsing JSON, xử lý nhiều data

### 🔴 KHÓ NHẤT (Kinh nghiệm cao)
- **Người 3**: AI Outfit & Charts
- Lý do: Tích hợp OpenAI, xử lý prompt engineering, vẽ biểu đồ phức tạp

---

## 👤 NGƯỜI 4: SETTINGS, UI & NOTIFICATIONS (🟢 DỄ NHẤT - 30% độ khó)

### 🎯 Tại sao dễ nhất?
- ✅ Không cần gọi API external phức tạp
- ✅ Chủ yếu làm việc với UI components
- ✅ Logic đơn giản: lưu/đọc preferences
- ✅ Có sẵn Android components (WorkManager, NotificationManager)
- ✅ Ít bugs tiềm ẩn

### 📂 Files cần hoàn thiện

#### 1. SettingsActivity.java (ƯU TIÊN CAO) ⭐⭐⭐
**Trạng thái**: Đã có structure cơ bản, cần hoàn thiện logic

**Nhiệm vụ**:
```java
// Đã có: setupToolbar(), loadSettings() basics
// CẦN LÀM:
1. ✅ Hoàn thiện setupListeners() cho tất cả switches/radio buttons
   - switchNotifications -> bật/tắt WeatherNotificationWorker
   - radioGroupTemperature -> đổi °C/°F
   - radioGroupWindSpeed -> đổi km/h/mph  
   - radioGroupPressure -> đổi hPa/mmHg
   - radioGroupLanguage -> đổi ngôn ngữ

2. ✅ Implement saveSettings() method
   - Lưu tất cả settings vào SharedPreferences
   - Apply ngay lập tức (không cần restart app)

3. ✅ Implement applySettings() method
   - Broadcast intent để MainActivity cập nhật
   - Restart activity nếu đổi ngôn ngữ

4. ✅ Xử lý About section
   - Hiển thị version app (BuildConfig.VERSION_NAME)
   - Privacy Policy dialog
   - Rate app (open Play Store)
```

**Độ khó**: ⭐⭐ (2/5) - Chỉ cần hiểu SharedPreferences & Listeners

---

#### 2. WeatherNotificationManager.java (ƯU TIÊN TRUNG BÌNH) ⭐⭐
**Trạng thái**: File đã có, cần implement methods

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ createNotificationChannel()
   - Tạo channel cho Android 8.0+
   - Set importance = HIGH
   - Enable vibration, lights

2. ✅ showWeatherNotification(WeatherResponse weather)
   - Hiển thị notification với:
     + Icon thời tiết (sunny/rainy/cloudy...)
     + Nhiệt độ hiện tại
     + Mô tả thời tiết
     + Tên thành phố
   - Action buttons: "Làm mới", "Xem chi tiết"

3. ✅ cancelNotification()
   - Xóa notification khi user tắt trong settings

4. ✅ Xử lý click notification -> mở MainActivity
```

**Độ khó**: ⭐⭐ (2/5) - Android có NotificationCompat.Builder sẵn

---

#### 3. WeatherNotificationWorker.java (ƯU TIÊN CAO) ⭐⭐⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Override doWork() method
   - Kiểm tra settings xem notifications có bật không
   - Lấy location hiện tại từ SharedPreferences
   - Gọi WeatherApiService.getCurrentWeather()
   - Parse response và gọi WeatherNotificationManager.showWeatherNotification()

2. ✅ Schedule periodic work trong MainActivity
   - Mỗi 3 giờ update một lần
   - setRequiredNetworkType(NetworkType.CONNECTED)
   - setConstraints cho battery optimization

3. ✅ Xử lý error cases
   - Không có internet -> không crash
   - API fail -> log error, retry later
```

**Độ khó**: ⭐⭐⭐ (3/5) - Cần hiểu WorkManager

---

#### 4. NotificationReceiver.java (ƯU TIÊN THẤP) ⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Xử lý ACTION_REFRESH
   - User click "Làm mới" trên notification
   - Trigger WeatherNotificationWorker ngay lập tức

2. ✅ Xử lý ACTION_OPEN_APP
   - Mở MainActivity với intent

3. ✅ Xử lý BOOT_COMPLETED
   - Restart WorkManager khi device reboot
```

**Độ khó**: ⭐ (1/5) - Rất đơn giản

---

#### 5. UI Improvements (BONUS TASKS) 🎨
```
1. ✅ Polish activity_settings.xml layout
   - Đảm bảo glassmorphism effect
   - Icons đẹp cho mỗi section
   - Consistent spacing

2. ✅ Tạo dialog layouts
   - dialog_about.xml
   - dialog_privacy_policy.xml

3. ✅ Animation transitions
   - Smooth transitions giữa các screens
   - Ripple effects cho buttons

4. ✅ Dark overlay cho tất cả activities
   - Consistent với MainActivity
```

**Độ khó**: ⭐⭐ (2/5) - Chủ yếu XML, copy style từ MainActivity

---

### 📝 CHECKLIST NGƯỜI 4
- [ ] 1. SettingsActivity - Save/Load preferences (2 ngày)
- [ ] 2. SettingsActivity - Apply settings (1 ngày)
- [ ] 3. WeatherNotificationManager - Create & show notifications (2 ngày)
- [ ] 4. WeatherNotificationWorker - Periodic updates (2 ngày)
- [ ] 5. NotificationReceiver - Handle actions (1 ngày)
- [ ] 6. UI Polish - Settings layout (1 ngày)
- [ ] 7. Test all settings combinations (1 ngày)
- [ ] 8. BONUS: Widget configuration UI (nếu còn thời gian)

**Tổng thời gian ước tính**: 10-12 ngày

---

## 👤 NGƯỜI 2: SEARCH & FAVORITES MANAGEMENT (🟡 VỪA - 40% độ khó)

### 🎯 Tại sao vừa phải?
- ✅ Cần hiểu RecyclerView & Adapter
- ✅ Phải xử lý GPS permissions
- ✅ Cần học Geocoding API
- ⚠️ Phải xử lý async operations
- ⚠️ Cache management hơi phức tạp

### 📂 Files cần hoàn thiện

#### 1. SearchActivity.java (ƯU TIÊN CAO) ⭐⭐⭐
**Trạng thái**: Có structure cơ bản

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Implement tìm kiếm với debouncing (300ms)
   - User gõ -> đợi 300ms -> gọi API
   - Tránh gọi API mỗi khi gõ 1 chữ
   - Dùng Handler.postDelayed()

2. ✅ Tích hợp Geocoding API
   - OpenWeatherMap Geocoding API
   - Endpoint: /geo/1.0/direct?q={city}&limit=5
   - Parse response thành List<CityWeather>

3. ✅ Hiển thị search suggestions
   - Show RecyclerView với top 5 kết quả
   - Mỗi item: tên thành phố, quốc gia, icon cờ
   - Click item -> return về MainActivity

4. ✅ GPS Location Detection
   - Request location permissions
   - FusedLocationProviderClient.getLastLocation()
   - Reverse geocoding (lat/lon -> city name)

5. ✅ Search History
   - Lưu 10 thành phố gần nhất vào SharedPreferences
   - Hiển thị khi mở SearchActivity
   - Click history item -> search luôn
```

**Độ khó**: ⭐⭐⭐ (3/5) - Cần hiểu async, permissions

---

#### 2. FavoriteCitiesActivity.java (ƯU TIÊN CAO) ⭐⭐⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Load danh sách favorites
   - Gọi FavoriteCitiesManager.getAllCities()
   - For each city: gọi WeatherApiService.getCurrentWeather()
   - Hiển thị trong RecyclerView với CityWeatherAdapter

2. ✅ Add to favorites từ MainActivity
   - FAB button "Add to Favorites"
   - Kiểm tra duplicate trước khi add
   - Toast "Đã thêm vào yêu thích"

3. ✅ Remove from favorites
   - Long press item -> show dialog "Xóa khỏi yêu thích?"
   - Hoặc swipe-to-delete (nếu có thời gian)

4. ✅ Click item -> Open MainActivity với city đó
   - Pass city data qua Intent
   - MainActivity load weather cho city đó

5. ✅ Pull-to-refresh
   - SwipeRefreshLayout
   - Refresh weather data cho tất cả cities
```

**Độ khó**: ⭐⭐⭐ (3/5) - Nhiều API calls song song

---

#### 3. CityWeatherAdapter.java (ƯU TIÊN CAO) ⭐⭐
**Trạng thái**: File đã có, cần hoàn thiện

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ ViewHolder class
   - Bind city name, country, temperature, weather icon
   - Current time của thành phố đó (timezone)

2. ✅ Click listener
   - Interface: OnCityClickListener
   - Pass CityWeather data back to Activity

3. ✅ Long click listener (cho delete)
   - Return true để show context menu

4. ✅ Update list dynamically
   - updateData(List<CityWeather> newList)
   - notifyDataSetChanged()
```

**Độ khó**: ⭐⭐ (2/5) - Standard RecyclerView pattern

---

#### 4. FavoriteCitiesManager.java (ƯU TIÊN TRUNG BÌNH) ⭐⭐
**Trạng thái**: File đã có

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ addCity(FavoriteCity city)
   - Check duplicate bằng lat/lon (không phải name)
   - Lưu vào JSON file hoặc SharedPreferences
   - Return true/false

2. ✅ removeCity(String cityId)
   - Xóa khỏi storage
   - Return true/false

3. ✅ getAllCities()
   - Parse JSON -> List<FavoriteCity>
   - Sort by addedTime (mới nhất lên đầu)

4. ✅ isFavorite(double lat, double lon)
   - Check xem city có trong favorites không
   - Dùng để show/hide star icon ở MainActivity

5. ✅ clearAll() (bonus)
   - Xóa tất cả favorites
```

**Độ khó**: ⭐⭐ (2/5) - JSON parsing, SharedPreferences

---

#### 5. Offline Cache (BONUS) ⭐⭐⭐
```java
// Tạo class mới: WeatherCacheManager.java

1. ✅ Cache weather data locally
   - Key: cityName_timestamp
   - Value: WeatherResponse JSON
   - Expire sau 1 giờ

2. ✅ getCachedWeather(String cityName)
   - Check cache trước khi gọi API
   - Return null nếu expired

3. ✅ saveToCache(String cityName, WeatherResponse data)
   - Save JSON to file
```

**Độ khó**: ⭐⭐⭐ (3/5) - File I/O, cache strategy

---

### 📝 CHECKLIST NGƯỜI 2
- [ ] 1. SearchActivity - Basic search UI (1 ngày)
- [ ] 2. SearchActivity - Geocoding API integration (2 ngày)
- [ ] 3. SearchActivity - GPS location detection (2 ngày)
- [ ] 4. SearchActivity - Search history (1 ngày)
- [ ] 5. FavoriteCitiesActivity - Load & display favorites (2 ngày)
- [ ] 6. FavoriteCitiesActivity - Add/Remove actions (1 ngày)
- [ ] 7. CityWeatherAdapter - Complete ViewHolder (1 ngày)
- [ ] 8. FavoriteCitiesManager - CRUD operations (2 ngày)
- [ ] 9. Test all search scenarios (1 ngày)
- [ ] 10. BONUS: Offline cache (nếu còn thời gian)

**Tổng thời gian ước tính**: 13-15 ngày

---

## 👤 NGƯỜI 1: MAIN WEATHER & API INTEGRATION (🟠 NÂNG CAO - 60% độ khó)

### 🎯 Tại sao nâng cao?
- ⚠️ Phải hiểu sâu về Retrofit & API
- ⚠️ Parse nhiều loại JSON responses khác nhau
- ⚠️ Xử lý nhiều API calls đồng thời
- ⚠️ Nhiều edge cases (no internet, API errors)
- ⚠️ Performance optimization quan trọng
- ✅ Core foundation cho toàn bộ app

### 📂 Files cần hoàn thiện

#### 1. MainActivity.java (ƯU TIÊN CỰC CAO) ⭐⭐⭐⭐⭐
**Trạng thái**: Đã có imports, cần implement logic chính

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ loadWeatherData() method - CORE FUNCTION
   - Input: city name hoặc lat/lon
   - Gọi WeatherApiService.getCurrentWeather()
   - Parse WeatherResponse
   - Update tất cả UI elements:
     + tvTemperature, tvFeelsLike
     + tvDescription, ivWeatherIcon
     + tvHumidity, tvWindSpeed, tvPressure
     + tvVisibility, tvCloudiness

2. ✅ loadHourlyForecast()
   - Gọi API forecast 48 hours
   - Parse thành List<HourlyForecast>
   - Update RecyclerView (horizontal scroll)

3. ✅ loadWeeklyForecast()
   - Gọi API forecast 7 days
   - Parse thành List<WeeklyForecast>
   - Update RecyclerView (vertical list)

4. ✅ loadAdditionalData()
   - Parallel calls:
     + getUVIndex() -> Update UV card
     + getAirQuality() -> Update AQI card với màu
     + getWeatherAlerts() -> Show alert banner nếu có

5. ✅ updateDynamicBackground()
   - Đổi background dựa trên:
     + Thời gian (sáng/trưa/tối/đêm)
     + Thời tiết (sunny/rainy/cloudy/snow)
   - Gradient animations mượt

6. ✅ updateSunriseSunset()
   - Parse Unix timestamp
   - Hiển thị giờ sunrise/sunset
   - Progress bar từ sunrise -> sunset
   - Animation icon mặt trời di chuyển

7. ✅ Pull-to-refresh
   - SwipeRefreshLayout
   - Reload tất cả data
   - Loading animation

8. ✅ Error handling
   - No internet -> Show cached data + toast
   - API error -> Show friendly error message
   - Location not found -> Suggest search
   - Timeout -> Retry mechanism
```

**Độ khó**: ⭐⭐⭐⭐⭐ (5/5) - Core của app, nhiều logic phức tạp

---

#### 2. WeatherDetailsActivity.java (ƯU TIÊN CAO) ⭐⭐⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Receive WeatherResponse từ MainActivity
   - Intent.getSerializableExtra()

2. ✅ Hiển thị chi tiết đầy đủ
   - Tất cả thông số từ MainActivity
   - THÊM: Dew Point, Cloud Coverage %
   - THÊM: Wind Direction (N, NE, E, SE...)
   - THÊM: Barometric Pressure Trend

3. ✅ AQI Details Section
   - PM2.5, PM10, O3, NO2, SO2 values
   - Color-coded indicators
   - Health recommendations

4. ✅ Sunrise/Sunset với animation đẹp
   - Arc progress từ sunrise -> current -> sunset
   - Golden hour calculation
   - Day length display

5. ✅ Share weather button
   - Format text đẹp
   - Share via Intent (SMS, Social...)
```

**Độ khó**: ⭐⭐⭐ (3/5) - Mainly UI work với data có sẵn

---

#### 3. WeatherApiService.java (ƯU TIÊN CỰC CAO) ⭐⭐⭐⭐
**Trạng thái**: File đã có, cần define endpoints

**Nhiệm vụ**:
```java
// CẦN LÀM - Define Retrofit endpoints:

@GET("weather")
Call<WeatherResponse> getCurrentWeather(
    @Query("q") String cityName,
    @Query("appid") String apiKey,
    @Query("units") String units,
    @Query("lang") String lang
);

@GET("weather")
Call<WeatherResponse> getCurrentWeatherByCoords(
    @Query("lat") double lat,
    @Query("lon") double lon,
    @Query("appid") String apiKey,
    @Query("units") String units,
    @Query("lang") String lang
);

@GET("forecast")
Call<HourlyForecastResponse> getHourlyForecast(
    @Query("lat") double lat,
    @Query("lon") double lon,
    @Query("appid") String apiKey,
    @Query("units") String units,
    @Query("cnt") int count // 48 hours
);

@GET("onecall")
Call<WeeklyForecastResponse> getWeeklyForecast(
    @Query("lat") double lat,
    @Query("lon") double lon,
    @Query("appid") String apiKey,
    @Query("units") String units,
    @Query("exclude") String exclude // "minutely,alerts"
);

@GET("uvi")
Call<UVIndexResponse> getUVIndex(
    @Query("lat") double lat,
    @Query("lon") double lon,
    @Query("appid") String apiKey
);

@GET("air_pollution")
Call<AirQualityResponse> getAirQuality(
    @Query("lat") double lat,
    @Query("lon") double lon,
    @Query("appid") String apiKey
);

// Geocoding
@GET("geo/1.0/direct")
Call<List<GeocodeResponse>> searchCities(
    @Query("q") String query,
    @Query("limit") int limit,
    @Query("appid") String apiKey
);
```

**Độ khó**: ⭐⭐⭐⭐ (4/5) - Cần hiểu Retrofit, query parameters

---

#### 4. RetrofitClient.java (ƯU TIÊN CAO) ⭐⭐⭐
**Trạng thái**: File đã có, cần cấu hình

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Setup Retrofit instance
   - Base URL: "https://api.openweathermap.org/data/2.5/"
   - OkHttpClient với:
     + Connect timeout: 30s
     + Read timeout: 30s
     + Interceptor để log requests (debug mode)

2. ✅ API Key Management
   - Đọc từ BuildConfig.OPENWEATHER_API_KEY
   - Hoặc từ local.properties
   - Add vào mọi request tự động (Interceptor)

3. ✅ Error Interceptor
   - Catch network errors
   - Return meaningful error messages
   - Retry logic (3 attempts)

4. ✅ Response Converters
   - GsonConverterFactory
   - Handle null values
   - Date format parsing
```

**Độ khó**: ⭐⭐⭐ (3/5) - Standard Retrofit setup

---

#### 5. Response Models (ƯU TIÊN CAO) ⭐⭐⭐
**Files**: WeatherResponse.java, HourlyForecastResponse.java, UVIndexResponse.java, AirQualityResponse.java, WeatherAlertsResponse.java

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ WeatherResponse.java
   - Map tất cả fields từ OpenWeatherMap API
   - Nested classes: Main, Weather, Wind, Clouds, Sys
   - @SerializedName annotations

2. ✅ HourlyForecastResponse.java
   - List<Forecast> items
   - Each item: dt, temp, weather, etc.

3. ✅ Các response khác tương tự
   - Follow OpenWeatherMap API docs
   - Test với Postman trước

4. ✅ Helper methods
   - getWeatherIconResource() -> @DrawableRes int
   - getFormattedTemperature() -> String "25°C"
   - getWindSpeedInKmh() -> double
```

**Độ khó**: ⭐⭐⭐ (3/5) - JSON mapping, nhiều nested objects

---

#### 6. BlurHelper.java (ƯU TIÊN THẤP) ⭐⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ applyBlur(Bitmap input, int radius)
   - Dùng RenderScript hoặc library
   - Return blurred Bitmap

2. ✅ applyGlassmorphism(View view)
   - Set background với blur + opacity
   - Border gradient

3. ✅ Performance optimization
   - Cache blurred images
   - Async processing
```

**Độ khó**: ⭐⭐ (2/5) - Có libraries sẵn

---

### 📝 CHECKLIST NGƯỜI 1
- [ ] 1. RetrofitClient setup (1 ngày)
- [ ] 2. WeatherApiService endpoints (1 ngày)
- [ ] 3. Response models mapping (2 ngày)
- [ ] 4. MainActivity - loadWeatherData() (3 ngày)
- [ ] 5. MainActivity - loadHourlyForecast() (1 ngày)
- [ ] 6. MainActivity - loadWeeklyForecast() (1 ngày)
- [ ] 7. MainActivity - UV, AQI, Alerts (2 ngày)
- [ ] 8. MainActivity - Dynamic background (2 ngày)
- [ ] 9. MainActivity - Sunrise/Sunset (1 ngày)
- [ ] 10. WeatherDetailsActivity (2 ngày)
- [ ] 11. Error handling & retry logic (2 ngày)
- [ ] 12. BlurHelper (1 ngày)
- [ ] 13. Testing với nhiều cities & weather conditions (2 ngày)

**Tổng thời gian ước tính**: 21-25 ngày

---

## 👤 NGƯỜI 3: AI OUTFIT & CHARTS (🔴 KHÓ NHẤT - 80% độ khó)

### 🎯 Tại sao khó nhất?
- 🔥 Phải học prompt engineering cho AI
- 🔥 Tích hợp OpenAI API (phức tạp hơn REST thông thường)
- 🔥 Parse unstructured AI response thành structured data
- 🔥 Vẽ biểu đồ với MPAndroidChart (nhiều config)
- 🔥 Cache strategy phức tạp (tiết kiệm API costs)
- 🔥 Error handling khó (AI có thể trả về gì cũng được)

### 📂 Files cần hoàn thiện

#### 1. OutfitSuggestionActivity.java (ƯU TIÊN CỰC CAO) ⭐⭐⭐⭐⭐
**Trạng thái**: Có structure, cần implement AI logic

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Receive WeatherResponse từ MainActivity

2. ✅ Generate outfit suggestions
   - Call OutfitSuggestionService.generateSuggestions()
   - Input: WeatherResponse + Gender + Activity Type
   - Show loading animation (AI takes 3-5 seconds)

3. ✅ Display suggestions
   - Parse AI response thành List<OutfitSuggestion>
   - Update RecyclerView với adapter
   - Each item: category, items[], description, icon

4. ✅ Filter options
   - Radio buttons: Nam / Nữ
   - Spinner: Hoạt động (Đi làm, Dạo phố, Thể thao, Hẹn hò)
   - Regenerate khi đổi filter

5. ✅ Cache mechanism
   - Key: weather_condition + temp_range + gender + activity
   - Save AI response để không gọi lại
   - Expire sau 24h

6. ✅ Error handling
   - API key invalid -> Show message
   - Rate limit exceeded -> Use fallback suggestions
   - Timeout -> Retry once
```

**Độ khó**: ⭐⭐⭐⭐⭐ (5/5) - AI integration phức tạp nhất

---

#### 2. OpenAIService.java (ƯU TIÊN CỰC CAO) ⭐⭐⭐⭐⭐
**Trạng thái**: File đã có, cần implement

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Setup Retrofit cho OpenAI API
   - Base URL: "https://api.openai.com/v1/"
   - Headers:
     + Authorization: "Bearer YOUR_API_KEY"
     + Content-Type: "application/json"

2. ✅ Define endpoint
@POST("chat/completions")
Call<OpenAIResponse> getChatCompletion(
    @Body OpenAIRequest request
);

3. ✅ OpenAIRequest.java
   - model: "gpt-3.5-turbo" (hoặc "gpt-4")
   - messages: [{"role": "user", "content": prompt}]
   - temperature: 0.7
   - max_tokens: 500

4. ✅ OpenAIResponse.java
   - choices[0].message.content
   - Parse JSON response
```

**Độ khó**: ⭐⭐⭐⭐⭐ (5/5) - API khác biệt so với REST thông thường

---

#### 3. OutfitSuggestionService.java (ƯU TIÊN CỰC CAO) ⭐⭐⭐⭐⭐
**Trạng thái**: File đã có, đây là BRAIN của feature

**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ generatePrompt() - QUAN TRỌNG NHẤT
   - Input: WeatherResponse, Gender, Activity
   - Output: String prompt cho AI

Example prompt:
"""
Bạn là chuyên gia tư vấn thời trang. Hãy gợi ý trang phục phù hợp cho:
- Thời tiết: Sunny, 28°C, độ ẩm 65%, gió nhẹ 10km/h
- Đối tượng: Nam
- Hoạt động: Đi làm văn phòng
- Địa điểm: Hà Nội, Việt Nam

Trả về JSON format:
{
  "suggestions": [
    {
      "category": "Áo",
      "items": ["Sơ mi trắng", "Polo trắng"],
      "description": "Vải mỏng, thoáng mát"
    },
    {
      "category": "Quần",
      "items": ["Quần âu ghi", "Kaki xanh navy"],
      "description": "Vải cotton thoáng"
    },
    ...
  ],
  "accessories": ["Kính râm", "Ô"],
  "notes": "Nên mang theo áo khoác nhẹ vì điều hòa lạnh"
}
"""

2. ✅ parseAIResponse()
   - Input: Raw AI response string
   - Try parse JSON first
   - If not JSON: use regex/string parsing
   - Return List<OutfitSuggestion>

3. ✅ generateFallbackSuggestions()
   - Khi API fail, dùng logic đơn giản:
   - If temp > 30°C -> Áo thun, quần shorts
   - If temp 20-30°C -> Áo sơ mi, quần dài
   - If temp < 20°C -> Áo khoác, áo len
   - If raining -> Áo mưa, ô

4. ✅ Cache management
   - saveToCache(key, List<OutfitSuggestion>)
   - getFromCache(key)
   - isCacheValid(key)
```

**Độ khó**: ⭐⭐⭐⭐⭐ (5/5) - Prompt engineering + parsing AI response

---

#### 4. ChartsActivity.java (ƯU TIÊN CAO) ⭐⭐⭐⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ Setup MPAndroidChart library
   - Add dependency vào build.gradle
   - Import LineChart, BarChart classes

2. ✅ Temperature Line Chart
   - X-axis: Hours (24h or 7 days)
   - Y-axis: Temperature (°C)
   - Line color: Gradient từ blue -> orange -> red
   - Data points from HourlyForecast/WeeklyForecast
   - Enable touch interactions (zoom, scroll)

3. ✅ Humidity Bar Chart
   - X-axis: Hours
   - Y-axis: Humidity %
   - Bar color: Blue gradient
   - Values on top of bars

4. ✅ Wind Speed Line Chart
   - Multi-line: Speed + Gust
   - Legend labels

5. ✅ Chart customization
   - Glassmorphism background
   - Custom grid lines
   - Smooth animations
   - Labels in Vietnamese

6. ✅ Switch between views
   - Tabs: 24h / 7 days / 30 days (if available)
   - ViewPager hoặc Tabs

7. ✅ Export chart
   - Save to image (PNG)
   - Share via Intent
```

**Độ khó**: ⭐⭐⭐⭐ (4/5) - Library phức tạp, nhiều config

---

#### 5. OutfitSuggestionAdapter.java (ƯU TIÊN TRUNG BÌNH) ⭐⭐⭐
**Nhiệm vụ**:
```java
// CẦN LÀM:
1. ✅ ViewHolder class
   - tvCategory: "Áo"
   - tvItems: "Sơ mi trắng, Polo"
   - tvDescription: "Vải mỏng..."
   - ivIcon: Icon tương ứng

2. ✅ Icon mapping
   - Map category -> drawable resource
   - "Áo" -> R.drawable.ic_shirt
   - "Quần" -> R.drawable.ic_pants
   - "Giày" -> R.drawable.ic_shoes
   - "Phụ kiện" -> R.drawable.ic_accessories

3. ✅ Expand/Collapse functionality
   - Click item -> expand to show full description
   - Animation smooth

4. ✅ Copy to clipboard
   - Long press -> copy outfit list
   - Toast "Đã copy"
```

**Độ khó**: ⭐⭐⭐ (3/5) - Standard adapter với animation

---

### 📝 CHECKLIST NGƯỜI 3
- [ ] 1. OpenAI API setup & testing (2 ngày)
- [ ] 2. OpenAIRequest & Response models (1 ngày)
- [ ] 3. OutfitSuggestionService - Prompt engineering (3 ngày)
- [ ] 4. OutfitSuggestionService - Parse AI response (2 ngày)
- [ ] 5. OutfitSuggestionService - Fallback logic (1 ngày)
- [ ] 6. OutfitSuggestionActivity - UI & filters (2 ngày)
- [ ] 7. OutfitSuggestionActivity - Cache mechanism (2 ngày)
- [ ] 8. OutfitSuggestionAdapter - ViewHolder (1 ngày)
- [ ] 9. MPAndroidChart library setup (1 ngày)
- [ ] 10. ChartsActivity - Temperature chart (2 ngày)
- [ ] 11. ChartsActivity - Humidity & Wind charts (2 ngày)
- [ ] 12. ChartsActivity - Customization & polish (2 ngày)
- [ ] 13. Test AI với nhiều weather scenarios (2 ngày)
- [ ] 14. Cost optimization (cache, rate limiting) (1 ngày)

**Tổng thời gian ước tính**: 24-28 ngày

---

## 🔗 THỨ TỰ LÀM VIỆC ĐỀ XUẤT

### TUẦN 1-2: FOUNDATION
**Người 1 LÀM TRƯỚC** (vì người khác phụ thuộc vào API)
1. Người 1: Setup Retrofit + API endpoints + Response models
2. Người 1: MainActivity basic weather display
3. Người 4: SettingsActivity basic
4. Người 2: SearchActivity UI setup

### TUẦN 3-4: CORE FEATURES
1. Người 1: Hoàn thiện MainActivity với full features
2. Người 2: Search + GPS + Favorites
3. Người 3: OpenAI integration + Basic outfit UI
4. Người 4: Notifications setup

### TUẦN 5-6: ADVANCED & POLISH
1. Người 3: Charts + AI optimization
2. Người 2: Cache + Offline mode
3. Người 4: Widget + Polish settings
4. Người 1: WeatherDetailsActivity + Error handling

### TUẦN 7: INTEGRATION & TESTING
- Tất cả: Merge code, fix conflicts
- Integration testing
- Bug fixes
- UI polish

---

## 📱 DEPENDENCIES GIỮA CÁC NGƯỜI

```
NGƯỜI 1 (Foundation)
    ↓
    ├─→ NGƯỜI 2 (cần WeatherApiService để search)
    ├─→ NGƯỜI 3 (cần WeatherResponse để generate outfit)
    └─→ NGƯỜI 4 (cần API cho notifications/widget)

NGƯỜI 2 (Favorites)
    ↓
    └─→ NGƯỜI 4 (Widget config cần favorites list)

TẤT CẢ
    ↓
    └─→ NGƯỜI 4 (Settings ảnh hưởng tất cả)
```

**➡️ NGƯỜI 1 PHẢI LÀM TRƯỚC 2 TUẦN**

---

## 🎓 HỌC GÌ CHO TỪNG NGƯỜI?

### Người 1 (Nâng cao)
- Retrofit deep dive
- Coroutines/AsyncTask
- JSON parsing với Gson
- Error handling patterns
- Android Architecture Components

### Người 2 (Vừa)
- RecyclerView & Adapters
- Location APIs
- Runtime permissions
- SharedPreferences/File I/O
- Geocoding concepts

### Người 3 (Khó nhất)
- AI/GPT APIs
- Prompt engineering
- MPAndroidChart documentation
- String parsing & regex
- Cache strategies
- Cost optimization

### Người 4 (Dễ nhất)
- SharedPreferences
- WorkManager
- Notification APIs
- BroadcastReceivers
- XML layouts

---

## 💰 CHI PHÍ API

### OpenWeatherMap (FREE tier)
- ✅ 1,000 calls/day free
- ✅ Đủ cho development & testing
- ⚠️ Production cần upgrade ($40/month for 100k calls)

### OpenAI (PAID)
- ⚠️ GPT-3.5-turbo: $0.002/1K tokens (~$0.01/request)
- ⚠️ GPT-4: $0.03/1K tokens (~$0.15/request)
- 💡 **Solution**: Cache heavily, dùng GPT-3.5, limit requests
- 💡 Budget: $10 credit đủ cho 1000 outfit suggestions

---

## 🎯 KẾT LUẬN & KHUYẾN NGHỊ

### ✅ Phân công theo độ khó:
1. **Người 4 (Dễ nhất)** → Người mới học Android, chưa nhiều kinh nghiệm
2. **Người 2 (Vừa)** → Đã làm Android 3-6 tháng
3. **Người 1 (Nâng cao)** → Kinh nghiệm 6-12 tháng, hiểu API
4. **Người 3 (Khó nhất)** → Kinh nghiệm 1+ năm, có khả năng research

### 🎯 Timeline thực tế:
- **Người 4**: 10-12 ngày (2 tuần)
- **Người 2**: 13-15 ngày (3 tuần)
- **Người 1**: 21-25 ngày (5 tuần)
- **Người 3**: 24-28 ngày (6 tuần)

**➡️ Tổng cộng: 6-7 tuần nếu làm song song**

### 📞 Support Plan:
- Người 1 làm xong 2 tuần đầu → support người khác
- Daily standup 15 phút online
- Shared Notion/Trello board
- Code review mandatory trước khi merge

---

## 🚀 BẮT ĐẦU NHƯ THẾ NÀO?

### Ngày 1:
1. ✅ Setup API keys (OpenWeather + OpenAI)
2. ✅ Mỗi người fork/clone repo
3. ✅ Tạo branch riêng: `feature/person1-weather-api`
4. ✅ Đọc docs:
   - Người 1: OpenWeatherMap API docs
   - Người 2: Android Location docs
   - Người 3: OpenAI API docs + MPAndroidChart
   - Người 4: WorkManager + Notifications docs

### Ngày 2-3:
- Mỗi người implement file ưu tiên cao nhất
- Commit thường xuyên
- Test trên emulator

### Ngày 4-5:
- Code review lẫn nhau
- Fix issues
- Continue với files tiếp theo

---

**Good luck team! 🎉 Hãy bắt đầu từ Người 1 trước nhé! 💪**

