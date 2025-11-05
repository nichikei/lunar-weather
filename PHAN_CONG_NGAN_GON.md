# PHÂN CÔNG 4 NGƯỜI - NGẮN GỌN

## 🎯 TRẠNG THÁI: DỰ ÁN XONG 85% - CHỈ CẦN HỌC & TEST

---

## 👤 NGƯỜI 1: MAIN WEATHER FLOW (🟡 VỪA - 10 ngày)

### 📁 Files cần học (8 files):
1. **MainActivity.java** ⭐⭐⭐⭐⭐ (1000+ dòng - FILE QUAN TRỌNG NHẤT)
2. **SearchActivity.java**
3. **WeatherDetailsActivity.java** (chỉ đọc để hiểu cấu trúc)
4. **WeatherApiService.java**
5. **RetrofitClient.java**
6. **WeatherResponse.java**
7. **HourlyForecastResponse.java**
8. **UVIndexResponse.java**
9. **AirQualityResponse.java**

### 🔄 Luồng hoạt động:
```
1. USER MỞ APP
   → MainActivity.onCreate()
   → loadSettings() (đọc °C/°F, m/s/km/h...)
   → fetchAllWeatherData("Hanoi")

2. FETCH WEATHER DATA
   → WeatherApiService.getWeatherByCity()
   → Retrofit gọi OpenWeatherMap API
   → Parse WeatherResponse JSON
   → updateUI() (hiển thị nhiệt độ, icon, description...)

3. FETCH THÊM DATA
   → fetchHourlyForecast() → RecyclerView ngang 8 items
   → fetchUVIndex() → Card UV
   → fetchAirQuality() → Card AQI với màu

4. USER ACTIONS
   → Click search icon → SearchActivity
   → Click GPS → getCurrentLocation() → fetchWeatherByCoordinates()
   → Click Hourly/Weekly → toggle forecast view
   → Click Charts → ChartsActivity (với data)
   → Click Outfit → OutfitSuggestionActivity (với data)
   → Click Settings → SettingsActivity
   → Pull to refresh → reload tất cả data

5. SETTINGS CHANGE
   → User đổi °C→°F → recreate() MainActivity
   → Gọi lại API với units="imperial"
```

### ✅ Nhiệm vụ:
- [ ] Đọc hiểu MainActivity (3 ngày)
- [ ] Test tất cả features (3 ngày)
- [ ] Test API calls (1 ngày)
- [ ] Test SearchActivity (1 ngày)
- [ ] Report bugs (1 ngày)
- [ ] Viết document "User Guide" (1 ngày)

---

## 👤 NGƯỜI 2: FAVORITES & DETAILS (🟡 VỪA - 11 ngày)

### 📁 Files cần học (4 files):
1. **FavoriteCitiesActivity.java** ⭐⭐⭐
2. **CityWeatherAdapter.java**
3. **FavoriteCitiesManager.java**
4. **FavoriteCity.java**

### 🔄 Luồng hoạt động:
```
1. USER THÊM FAVORITE
   → MainActivity → Click "Add to Favorites"
   → FavoriteCitiesManager.addFavoriteCity(city)
   → Save to SharedPreferences (JSON với Gson)
   → Max 10 cities

2. XEM FAVORITES
   → Open FavoriteCitiesActivity
   → FavoriteCitiesManager.getFavoriteCities()
   → Parse JSON → List<FavoriteCity>
   → For each city: gọi WeatherApiService.getWeatherByCity()
   → Update adapter với weather data mới

3. XÓA FAVORITE
   → Long press city → Dialog confirm
   → FavoriteCitiesManager.removeFavoriteCity()
   → Update SharedPreferences
   → Refresh RecyclerView

4. CLICK CITY
   → Pass city name to MainActivity
   → MainActivity fetch weather cho city đó
```

### 🔨 CẦN LÀM MỚI: WeatherDetailsActivity.java
```
📁 Tạo file mới:
- WeatherDetailAdapter.java (file mới trong ui/adapters/)

📋 Implementation:
1. Nhận WeatherResponse từ Intent
2. Tạo List<WeatherMetric> với 12 items:
   - Feels Like, Humidity, Wind Speed, Pressure
   - Visibility, UV Index, Cloud Coverage, Dew Point
   - Wind Direction, Sunrise, Sunset, AQI
3. RecyclerView GridLayoutManager(2 columns)
4. Format units từ Settings (°C/°F, m/s/km/h...)
5. Back button
```

### ✅ Nhiệm vụ:
- [ ] Đọc hiểu FavoriteCitiesActivity (1 ngày)
- [ ] Test favorites features (2 ngày)
- [ ] **IMPLEMENT WeatherDetailsActivity** (5 ngày) ⭐
- [ ] Test WeatherDetails (2 ngày)
- [ ] Viết document "Developer Guide" (1 ngày)

---

## 👤 NGƯỜI 3: AI & CHARTS (🔴 KHÓ - 14 ngày)

### 📁 Files cần học (5 files):
1. **OutfitSuggestionActivity.java**
2. **ChartsActivity.java** ⭐⭐⭐
3. **OutfitSuggestionAdapter.java**
4. **OutfitSuggestionService.java** ⭐⭐⭐⭐⭐ (300+ dòng - FILE PHỨC TẠP NHẤT)
5. **OutfitSuggestion.java**

### 🔄 Luồng hoạt động AI:
```
1. USER CLICK "OUTFIT SUGGESTION"
   → MainActivity pass WeatherResponse
   → OutfitSuggestionActivity.onCreate()

2. CALL GEMINI AI
   → OutfitSuggestionService.getOutfitSuggestions()
   → createPrompt(weather) → Format prompt với temp, condition...
   → OkHttpClient.newCall() → POST to Gemini API
   → Đợi response (3-5 giây)

3. PARSE AI RESPONSE
   → parseGeminiResponse(JSON)
   → Extract JSON array từ AI text
   → Parse thành List<OutfitSuggestion>
   → Update RecyclerView

4. ERROR HANDLING
   → Timeout? → Retry 1 lần
   → MAX_TOKENS? → Tăng maxOutputTokens, retry
   → API fail? → getDefaultOutfitSuggestions() (fallback)

5. FALLBACK LOGIC (Offline)
   → if temp < 10°C → "Heavy jacket"
   → if temp 10-20°C → "Light jacket"
   → if temp 20-28°C → "T-shirt"
   → if temp > 28°C → "Tank top"
   → if raining → "Umbrella"
```

### 🔄 Luồng hoạt động Charts:
```
1. USER CLICK "VIEW CHARTS"
   → MainActivity pass HourlyForecastResponse + WeatherResponse
   → ChartsActivity.onCreate()

2. SETUP 5 CHARTS
   → setupTemperatureChart() → LineChart 12 giờ
   → setupWeatherStatsChart() → BarChart (humidity, wind, pressure, UV)
   → setupRainProbabilityChart() → LineChart xác suất mưa
   → setupWindSpeedChart() → LineChart tốc độ gió
   → setupHumidityChart() → LineChart độ ẩm

3. DATA PROCESSING
   → Loop through HourlyForecastResponse.list
   → Extract temp, humidity, wind, rain prob...
   → Convert units nếu cần (m/s → km/h)
   → Add to Entry list

4. CUSTOMIZE CHARTS
   → Colors, gradients, animations
   → Labels, grid lines
   → Touch interactions (zoom, scroll)
```

### ✅ Nhiệm vụ:
- [ ] **Đọc KỸ OutfitSuggestionService** (4 ngày) ⭐
- [ ] Test Gemini API (2 ngày)
- [ ] Test fallback logic (1 ngày)
- [ ] Học MPAndroidChart (2 ngày)
- [ ] Test 5 charts (2 ngày)
- [ ] Viết 2 documents (3 ngày)

---

## 👤 NGƯỜI 4: SETTINGS & NOTIFICATIONS (🟢 DỄ NHẤT - 17 ngày)

### 📁 Files cần học (6 files):
1. **SettingsActivity.java** ⭐⭐⭐
2. **WeatherNotificationManager.java**
3. **WeatherNotificationWorker.java**
4. **NotificationReceiver.java** (cần kiểm tra xem có code chưa)
5. **WeatherWidget.java**
6. **LocaleHelper.java**

### 🔄 Luồng hoạt động Settings:
```
1. USER OPEN SETTINGS
   → SettingsActivity.onCreate()
   → loadSettings() từ SharedPreferences

2. CHANGE TEMPERATURE UNIT
   → User toggle °C → °F
   → saveTemperatureUnit("fahrenheit")
   → setResult(RESULT_OK) → notify MainActivity
   → MainActivity.settingsLauncher → recreate()
   → Fetch lại data với units="imperial"

3. CHANGE LANGUAGE
   → User toggle English → Vietnamese
   → saveLanguage("vi")
   → LocaleHelper.setLocale(context, "vi")
   → recreate() SettingsActivity
   → Return RESULT_OK với flag "language_changed"
   → MainActivity recreate() → All text hiển thị tiếng Việt

4. TOGGLE NOTIFICATIONS
   → User bật/tắt notifications
   → saveNotifications(true/false)
   → scheduleNotifications() hoặc cancel
   → WorkManager schedule WeatherNotificationWorker
```

### 🔄 Luồng hoạt động Notifications:
```
1. SCHEDULE NOTIFICATION
   → MainActivity.onCreate() → scheduleWeatherNotifications()
   → WorkManager.enqueue(PeriodicWorkRequest)
   → Chạy mỗi 3 giờ (hoặc 2 phút test mode)

2. WORKER CHẠY
   → WeatherNotificationWorker.doWork()
   → Check notifications enabled? (SharedPreferences)
   → Get last city (SharedPreferences)
   → Gọi WeatherApiService.getWeatherByCity()
   → Parse weather data

3. SHOW NOTIFICATION
   → WeatherNotificationManager.showWeatherNotification()
   → NotificationCompat.Builder
   → Set icon, title, text, actions
   → notificationManager.notify()

4. CHECK WEATHER ALERTS
   → checkWeatherAlerts(weather)
   → if temp > 35°C → showWeatherAlert("High Temperature")
   → if temp < 0°C → showWeatherAlert("Freezing")
   → if thunderstorm → showWeatherAlert("Thunderstorm")

5. USER CLICK NOTIFICATION
   → PendingIntent → Open MainActivity

6. USER CLICK "REFRESH"
   → NotificationReceiver.onReceive()
   → Trigger WeatherNotificationWorker ngay lập tức
```

### 🔄 Luồng hoạt động Widget:
```
1. USER ADD WIDGET
   → WeatherWidget.onUpdate()
   → Get city từ SharedPreferences
   → Fetch weather từ API
   → updateWidgetWithWeatherData()
   → RemoteViews set text, image

2. WIDGET UPDATE
   → MainActivity update weather
   → Gọi WeatherWidget.updateWidget()
   → Update RemoteViews
   → appWidgetManager.updateAppWidget()

3. USER CLICK WIDGET
   → PendingIntent → Open MainActivity
```

### 🔨 CẦN LÀM MỚI: NotificationReceiver.java
```java
// Kiểm tra file có code chưa, nếu thiếu thì implement:

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.weatherapp.ACTION_REFRESH_WEATHER".equals(intent.getAction())) {
            WorkManager.getInstance(context)
                .enqueue(new OneTimeWorkRequest.Builder(
                    WeatherNotificationWorker.class).build());
        }
    }
}

// Đừng quên register trong AndroidManifest.xml
```

### ✅ Nhiệm vụ:
- [ ] Đọc hiểu SettingsActivity (2 ngày)
- [ ] Test tất cả settings (3 ngày)
- [ ] Đọc hiểu Notifications (2 ngày)
- [ ] Test notifications (3 ngày)
- [ ] **Check & implement NotificationReceiver** (2 ngày)
- [ ] Test widget (3 ngày)
- [ ] Test language switching (1 ngày)
- [ ] Viết document "Settings Guide" (1 ngày)

---

## 🔥 CÔNG VIỆC BẮT BUỘC

### ⚠️ PHẢI LÀM (2 items):
1. **NGƯỜI 2**: Implement WeatherDetailsActivity (5 ngày)
2. **NGƯỜI 4**: Check & implement NotificationReceiver (2 ngày)

### 🎯 QUAN TRỌNG:
3. **NGƯỜI 1**: Test MainActivity kỹ (3 ngày)
4. **NGƯỜI 3**: Hiểu OutfitSuggestionService (4 ngày)
5. **TẤT CẢ**: Test và report bugs

---

## 📅 TIMELINE 4 TUẦN

### TUẦN 1: ĐỌC CODE
- Mỗi người đọc files của mình
- Comment code để hiểu
- Hỏi nhau khi không hiểu

### TUẦN 2: IMPLEMENT & TEST
- **Người 2**: Code WeatherDetailsActivity
- **Người 4**: Code NotificationReceiver
- **Người 1, 3**: Test kỹ

### TUẦN 3: FIX BUGS & POLISH
- Fix bugs
- Polish UI
- Cross-test

### TUẦN 4: DOCUMENTATION
- Mỗi người viết 1 document
- Final testing
- Ready to release

---

## 🎯 ĐỘ KHÓ

| Người | Độ khó | Thời gian | Lý do |
|-------|--------|-----------|-------|
| **Người 4** | 🟢 DỄ NHẤT | 17 ngày | Code xong 100%, chỉ học & test |
| **Người 1** | 🟡 VỪA | 10 ngày | MainActivity phức tạp nhưng đã xong |
| **Người 2** | 🟡 VỪA | 11 ngày | Phải code mới nhưng logic đơn giản |
| **Người 3** | 🔴 KHÓ NHẤT | 14 ngày | AI & Charts phức tạp, cần research |

---

## 💡 TECHNOLOGY STACK

### Người 1 học:
- Retrofit, OkHttp
- RecyclerView
- ViewBinding
- SharedPreferences
- ActivityResultLauncher
- FusedLocationProviderClient

### Người 2 học:
- RecyclerView GridLayoutManager
- Custom Adapter
- Intent data passing
- Gson JSON parsing

### Người 3 học:
- Gemini API (Google Generative AI)
- Prompt Engineering
- OkHttp với logging
- MPAndroidChart library
- Error handling & retry logic

### Người 4 học:
- SharedPreferences
- NotificationCompat, NotificationChannel
- WorkManager, BroadcastReceiver
- AppWidgetProvider, RemoteViews
- PendingIntent

---

## 🎉 KẾT LUẬN

**DỰ ÁN XONG 85%**
- ✅ Code chất lượng cao
- ✅ Architecture tốt
- ⚠️ Còn 15% cần làm: test, hoàn thiện, document

**CÁI GÌ DỄ NHẤT?**
→ **NGƯỜI 4** (Settings & Notifications)
- Code đã xong 100%
- Chỉ cần học và test
- Concepts đơn giản

**XẾP HẠNG:**
1. 🟢 Người 4 - DỄ NHẤT
2. 🟡 Người 1 - VỪA
3. 🟡 Người 2 - VỪA (phải code mới)
4. 🔴 Người 3 - KHÓ NHẤT

---

*Cập nhật: 2025-01-05*
