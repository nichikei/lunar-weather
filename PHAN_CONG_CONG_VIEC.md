# PHÂN CÔNG CÔNG VIỆC DỰ ÁN WEATHER APP - 4 THÀNH VIÊN

## 📱 Tổng Quan Dự Án
Ứng dụng thời tiết Android với thiết kế glassmorphism, dữ liệu real-time và AI gợi ý trang phục.

---

## 👤 NGƯỜI 1: LUỒNG HIỂN THỊ THỜI TIẾT CHÍNH & TÍCH HỢP API

### 🎯 Trách nhiệm chính
Phụ trách màn hình chính, hiển thị thông tin thời tiết và tích hợp API lấy dữ liệu.

### 📂 Các file cần làm việc

#### 1. UI Activities
- **MainActivity.java** 
  - Màn hình chính hiển thị thời tiết hiện tại
  - Xử lý dự báo theo giờ và theo tuần
  - Hiển thị các thông số: nhiệt độ, độ ẩm, gió, áp suất, UV
  - Tích hợp nền động theo thời tiết
  - Xử lý cập nhật dữ liệu real-time

- **WeatherDetailsActivity.java**
  - Chi tiết thông số thời tiết mở rộng
  - Hiển thị giờ mặt trời mọc/lặn
  - Chỉ số chất lượng không khí (AQI)

#### 2. Data Layer - API & Models
- **data/api/WeatherApiService.java**
  - Định nghĩa các endpoint API OpenWeatherMap
  - getCurrentWeather(), getHourlyForecast(), getWeeklyForecast()
  - getUVIndex(), getAirQuality(), getWeatherAlerts()

- **data/api/RetrofitClient.java**
  - Cấu hình Retrofit client
  - Base URL, timeout, interceptors
  - Quản lý API key

- **data/responses/** (Các response models)
  - WeatherResponse.java
  - HourlyForecastResponse.java
  - UVIndexResponse.java
  - AirQualityResponse.java
  - WeatherAlertsResponse.java

- **data/models/** (Models cho UI)
  - HourlyForecast.java
  - WeeklyForecast.java
  - WeatherAlert.java

#### 3. Utils
- **utils/BlurHelper.java**
  - Xử lý hiệu ứng blur cho glassmorphism
  - Tạo backdrop blur cho các card

### 📝 Nhiệm vụ cụ thể
1. ✅ Tích hợp OpenWeatherMap API
2. ✅ Xử lý parse JSON response
3. ✅ Hiển thị thời tiết hiện tại với đầy đủ thông số
4. ✅ Implement dự báo theo giờ (horizontal RecyclerView)
5. ✅ Implement dự báo theo tuần (vertical list)
6. ✅ Hiển thị sunrise/sunset với animation
7. ✅ Hiển thị AQI với màu sắc theo mức độ
8. ✅ Xử lý loading states và error handling
9. ✅ Implement pull-to-refresh
10. ✅ Tối ưu hiệu năng khi load dữ liệu

### 🎨 Giao diện cần xử lý
- Layout: activity_main.xml
- Layout: activity_weather_details.xml
- Các item layouts: item_hourly_forecast.xml, item_weekly_forecast.xml

---

## 👤 NGƯỜI 2: LUỒNG TÌM KIẾM & QUẢN LÝ ĐỊA ĐIỂM

### 🎯 Trách nhiệm chính
Phụ trách tìm kiếm thành phố, phát hiện vị trí, quản lý danh sách yêu thích.

### 📂 Các file cần làm việc

#### 1. UI Activities
- **SearchActivity.java**
  - Tìm kiếm thành phố theo tên
  - Hiển thị suggestions khi gõ
  - Hiển thị lịch sử tìm kiếm
  - Tích hợp location detection (GPS)
  - Xử lý permissions (Location)

- **FavoriteCitiesActivity.java**
  - Hiển thị danh sách thành phố yêu thích
  - Thêm/xóa thành phố khỏi favorites
  - Hiển thị thời tiết tóm tắt của mỗi thành phố
  - Sắp xếp và quản lý thứ tự

#### 2. UI Adapters
- **ui/adapters/CityWeatherAdapter.java**
  - Adapter cho RecyclerView hiển thị danh sách thành phố
  - ViewHolder với thông tin thời tiết cơ bản
  - Click listener để xem chi tiết
  - Swipe to delete

#### 3. Data Models
- **data/models/FavoriteCity.java**
  - Model lưu thông tin thành phố yêu thích
  - id, name, country, lat, lon, addedTime

- **data/models/CityWeather.java**
  - Model chứa thông tin thời tiết tóm tắt của thành phố
  - Dùng cho hiển thị trong list

#### 4. Utils
- **utils/FavoriteCitiesManager.java**
  - Quản lý lưu trữ danh sách favorites (SharedPreferences/File)
  - addCity(), removeCity(), getAllCities()
  - Kiểm tra thành phố đã tồn tại
  - Export/Import favorites

### 📝 Nhiệm vụ cụ thể
1. ✅ Implement search với debouncing
2. ✅ Tích hợp geocoding API (tìm tọa độ từ tên thành phố)
3. ✅ Xử lý GPS location detection
4. ✅ Request và handle location permissions
5. ✅ Lưu/đọc danh sách favorites từ storage
6. ✅ Hiển thị thời tiết của nhiều thành phố cùng lúc
7. ✅ Implement swipe-to-delete trong favorites
8. ✅ Lưu lịch sử tìm kiếm
9. ✅ Auto-complete suggestions
10. ✅ Xử lý offline mode (cache data)

### 🎨 Giao diện cần xử lý
- Layout: activity_search.xml
- Layout: activity_favorite_cities.xml
- Item layout: item_city_weather.xml
- Item layout: item_search_suggestion.xml

---

## 👤 NGƯỜI 3: LUỒNG AI GỢI Ý TRANG PHỤC & BIỂU ĐỒ

### 🎯 Trách nhiệm chính
Phụ trách tính năng premium: AI gợi ý trang phục và hiển thị biểu đồ thời tiết.

### 📂 Các file cần làm việc

#### 1. UI Activities
- **OutfitSuggestionActivity.java**
  - Hiển thị gợi ý trang phục dựa trên thời tiết
  - Tích hợp OpenAI API để generate gợi ý
  - Hiển thị danh sách outfit với icon và mô tả
  - Lọc theo giới tính, hoạt động (đi làm, thể thao, dạo phố...)
  - Loading animation khi đang generate

- **ChartsActivity.java**
  - Hiển thị biểu đồ nhiệt độ theo giờ/ngày
  - Biểu đồ độ ẩm, tốc độ gió
  - Biểu đồ xu hướng thời tiết
  - Tích hợp thư viện chart (MPAndroidChart)

#### 2. UI Adapters
- **ui/adapters/OutfitSuggestionAdapter.java**
  - Adapter hiển thị danh sách outfit suggestions
  - ViewHolder với icon, title, description
  - Click to expand chi tiết

#### 3. Data Layer
- **data/api/OpenAIService.java**
  - Tích hợp OpenAI API
  - Generate outfit suggestions bằng GPT
  - Parse AI response

- **data/responses/OpenAIRequest.java**
  - Request model cho OpenAI API
  - Format prompt với weather data

- **data/responses/OpenAIResponse.java**
  - Response model từ OpenAI
  - Parse choices và content

- **data/models/OutfitSuggestion.java**
  - Model cho gợi ý trang phục
  - category, items[], description, weatherCondition

#### 4. Utils
- **utils/OutfitSuggestionService.java**
  - Business logic cho outfit suggestions
  - Generate prompt dựa trên thời tiết
  - Cache suggestions để tiết kiệm API calls
  - Format output từ AI

### 📝 Nhiệm vụ cụ thể
1. ✅ Tích hợp OpenAI API (GPT-3.5/4)
2. ✅ Design prompt engineering cho gợi ý outfit phù hợp
3. ✅ Parse AI response thành structured data
4. ✅ Hiển thị outfit với icon và description
5. ✅ Implement filter: nam/nữ, loại hoạt động
6. ✅ Cache suggestions (tránh call API nhiều lần)
7. ✅ Tích hợp thư viện biểu đồ (MPAndroidChart hoặc tương tự)
8. ✅ Tạo line chart cho nhiệt độ
9. ✅ Tạo bar chart cho độ ẩm/gió
10. ✅ Customize chart appearance (colors, animations)
11. ✅ Export/share chart images

### 🎨 Giao diện cần xử lý
- Layout: activity_outfit_suggestion.xml
- Layout: activity_charts.xml
- Item layout: item_outfit_suggestion.xml
- Custom chart views

### 📚 Thư viện cần thêm
```gradle
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
```

---

## 👤 NGƯỜI 4: LUỒNG SETTINGS, NOTIFICATIONS & WIDGET

### 🎯 Trách nhiệm chính
Phụ trách cài đặt, thông báo thời tiết và widget màn hình chính.

### 📂 Các file cần làm việc

#### 1. UI Activities
- **SettingsActivity.java**
  - Cài đặt đơn vị (°C/°F, km/h, mph)
  - Bật/tắt thông báo thời tiết
  - Chọn tần suất cập nhật
  - Cài đặt ngôn ngữ (Tiếng Việt)
  - Theme settings (nếu có dark mode)
  - Quản lý premium features
  - About app, version, privacy policy

#### 2. Notification System
- **notification/WeatherNotificationManager.java**
  - Tạo và hiển thị notification
  - Notification channels (Android O+)
  - Rich notification với weather info
  - Action buttons (Refresh, View Details)
  - Notification icons theo thời tiết

- **notification/WeatherNotificationWorker.java**
  - Background worker để cập nhật thời tiết định kỳ
  - Sử dụng WorkManager
  - Schedule periodic updates
  - Kiểm tra điều kiện (WiFi, battery)

- **notification/NotificationReceiver.java**
  - BroadcastReceiver xử lý action buttons
  - Boot completed receiver (khởi động lại worker)
  - Xử lý click vào notification

#### 3. Widget
- **widget/WeatherWidget.java**
  - Home screen widget hiển thị thời tiết
  - Update widget định kỳ
  - Click widget mở app
  - Configure widget (chọn thành phố)
  - Layout cho các size khác nhau (2x2, 4x2, 4x4)

#### 4. Utils
- **utils/LocaleHelper.java**
  - Quản lý đa ngôn ngữ
  - Change locale programmatically
  - Lưu language preference
  - Format date/time theo locale

### 📝 Nhiệm vụ cụ thể

#### Settings
1. ✅ Implement PreferenceScreen với các options
2. ✅ Lưu/đọc settings từ SharedPreferences
3. ✅ Đổi đơn vị nhiệt độ và cập nhật toàn app
4. ✅ Implement language switcher
5. ✅ Premium features toggle

#### Notifications
6. ✅ Tạo notification channels
7. ✅ Design notification layout với custom view
8. ✅ Implement periodic update với WorkManager
9. ✅ Xử lý click actions
10. ✅ Weather alerts notification (cảnh báo khẩn cấp)
11. ✅ Notification preferences (frequency, time)

#### Widget
12. ✅ Tạo widget provider và layout
13. ✅ Update widget với dữ liệu thời tiết
14. ✅ Xử lý click events
15. ✅ Widget configuration activity
16. ✅ Support multiple widget instances
17. ✅ Responsive layouts cho các size

### 🎨 Giao diện cần xử lý
- Layout: activity_settings.xml
- XML: preferences.xml (PreferenceScreen)
- Layout: notification_weather.xml
- Layout: widget_weather_small.xml, widget_weather_large.xml
- Layout: activity_widget_config.xml

### 📚 Dependencies cần thêm
```gradle
implementation 'androidx.work:work-runtime:2.8.1'
implementation 'androidx.preference:preference:1.2.1'
```

### ⚙️ AndroidManifest.xml updates
- Khai báo receivers cho notification và widget
- Permissions: POST_NOTIFICATIONS (Android 13+)
- WorkManager initialization

---

## 🤝 CÔNG VIỆC CHUNG (TẤT CẢ 4 NGƯỜI)

### 1. Testing
- Mỗi người test kỹ luồng của mình
- Integration testing giữa các modules
- Test trên nhiều devices (kích thước màn hình khác nhau)
- Test offline scenarios

### 2. UI/UX
- Đảm bảo glassmorphism effect đồng nhất
- Animations mượt mà
- Loading states và error messages
- Responsive design

### 3. Code Quality
- Follow coding conventions
- Comment code rõ ràng (tiếng Việt OK)
- Xử lý exceptions đầy đủ
- Memory leak prevention

### 4. Documentation
- Document các API keys cần thiết
- Hướng dẫn setup môi trường
- Ghi chú các edge cases

---

## 📊 TIMELINE ĐỀ XUẤT

### Sprint 1 (Tuần 1-2): Core Features
- **Người 1**: Tích hợp API + MainActivity cơ bản
- **Người 2**: Search + Location detection cơ bản
- **Người 3**: Setup OpenAI + Outfit UI cơ bản
- **Người 4**: Settings cơ bản + Notification setup

### Sprint 2 (Tuần 3-4): Advanced Features
- **Người 1**: Weather details + Polish UI
- **Người 2**: Favorites management + Cache
- **Người 3**: Charts implementation + AI optimization
- **Người 4**: Widget + Advanced notifications

### Sprint 3 (Tuần 5): Integration & Polish
- Integration testing toàn bộ app
- UI/UX refinements
- Bug fixes
- Performance optimization

### Sprint 4 (Tuần 6): Final Testing & Release
- User acceptance testing
- Final bug fixes
- Documentation
- Prepare for release

---

## 🔗 ĐIỂM TÍCH HỢP GIỮA CÁC NGƯỜI

### Người 1 ↔ Người 2
- Người 2 chọn thành phố → gọi API của Người 1 để lấy thời tiết
- SharedPreferences cho current location

### Người 1 ↔ Người 3
- Người 3 lấy weather data từ Người 1 để generate outfit
- Weather data cho charts

### Người 1 ↔ Người 4
- Người 4 dùng API service của Người 1 cho notifications/widget
- Settings của Người 4 ảnh hưởng đến display của Người 1

### Người 2 ↔ Người 4
- Widget configuration chọn thành phố từ favorites của Người 2
- Settings ngôn ngữ ảnh hưởng search

### Người 3 ↔ Người 4
- Settings đơn vị ảnh hưởng charts
- Premium features toggle ảnh hưởng outfit suggestions

---

## 📱 API KEYS CẦN THIẾT

### Cho tất cả
```
OPENWEATHER_API_KEY=your_key_here (Người 1 setup, all share)
```

### Riêng cho Người 3
```
OPENAI_API_KEY=your_key_here
```

### File: local.properties
```properties
openweather.api.key=YOUR_OPENWEATHER_KEY
openai.api.key=YOUR_OPENAI_KEY
```

---

## ✅ CHECKLIST HOÀN THÀNH

### Người 1
- [ ] OpenWeatherMap API integration
- [ ] MainActivity với full weather info
- [ ] Hourly forecast
- [ ] Weekly forecast
- [ ] WeatherDetailsActivity
- [ ] AQI display
- [ ] Sunrise/sunset animation
- [ ] Pull to refresh
- [ ] Error handling

### Người 2
- [ ] SearchActivity
- [ ] GPS location detection
- [ ] Location permissions
- [ ] FavoriteCitiesActivity
- [ ] Add/remove favorites
- [ ] CityWeatherAdapter
- [ ] Search history
- [ ] Offline cache

### Người 3
- [ ] OpenAI integration
- [ ] OutfitSuggestionActivity
- [ ] Outfit suggestions display
- [ ] Filters (gender, activity)
- [ ] ChartsActivity
- [ ] Temperature charts
- [ ] Humidity/wind charts
- [ ] Chart customization
- [ ] Caching logic

### Người 4
- [ ] SettingsActivity
- [ ] Unit conversions
- [ ] Language support
- [ ] WeatherNotificationManager
- [ ] WeatherNotificationWorker
- [ ] Notification channels
- [ ] WeatherWidget (multiple sizes)
- [ ] Widget configuration
- [ ] Boot receiver

---

## 🎯 LƯU Ý QUAN TRỌNG

1. **Communication**: Họp daily standup 15 phút để sync tiến độ
2. **Git workflow**: 
   - Mỗi người làm trên branch riêng
   - Naming: feature/person1-main-weather, feature/person2-search...
   - Pull request và code review trước khi merge
3. **Dependencies**: Người 1 nên hoàn thành API integration trước để người khác dùng
4. **Testing**: Test luồng của mình trước khi integrate
5. **Code style**: Thống nhất naming convention và code structure

## 📞 HỖ TRỢ

- Gặp vấn đề? Hỏi nhóm trước khi tự fix
- Shared document cho notes và issues
- Code review lẫn nhau để học hỏi

---

**Good luck! 🚀 Happy coding! 💻**

