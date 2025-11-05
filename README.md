# Weather App 🌤️

Ứng dụng thời tiết Android với thiết kế glassmorphism phong cách iOS, dữ liệu thời tiết real-time và gợi ý trang phục thông minh bằng AI.

## ✨ Tính Năng

### Tính Năng Chính
- 🌡️ **Dữ Liệu Thời Tiết Real-time** - Nhiệt độ hiện tại, điều kiện thời tiết và dự báo
- 📍 **Thời Tiết Theo Vị Trí** - Tự động phát hiện vị trí hoặc tìm kiếm thành phố
- 📊 **Thông Số Thời Tiết Chi Tiết** - Chỉ số UV, độ ẩm, tốc độ gió, áp suất, tầm nhìn xa
- 🕐 **Dự Báo Theo Giờ & Tuần** - Dự đoán thời tiết toàn diện
- 🌅 **Thời Gian Mặt Trời Mọc/Lặn** - Hiển thị sunrise/sunset đẹp mắt
- 💨 **Chỉ Số Chất Lượng Không Khí** - Theo dõi AQI khu vực của bạn
- ⚠️ **Cảnh Báo Thời Tiết** - Thông báo về các cảnh báo thời tiết nguy hiểm

### Tính Năng Premium
- 👔 **Gợi Ý Trang Phục AI** - Đề xuất quần áo thông minh dựa trên thời tiết
- 📈 **Biểu Đồ Thời Tiết** - Trực quan hóa xu hướng thời tiết
- 🎨 **Nền Động** - Hình nền thay đổi theo thời tiết
- ❤️ **Quản Lý Thành Phố Yêu Thích** - Lưu và theo dõi nhiều địa điểm
- 📱 **Widget** - Widget màn hình chính hiển thị thời tiết nhanh
- 🔔 **Thông Báo Thời Tiết** - Nhận cảnh báo và cập nhật thời tiết

## 🎨 Thiết Kế

### Điểm Nổi Bật UI/UX
- **Giao Diện Phong Cách iOS** - Thiết kế sạch sẽ, hiện đại và trực quan
- **Hiệu Ứng Glassmorphism** - Các thành phần UI kính mờ với backdrop blur
- **Typography Cao Cấp** - Phân cấp văn bản được thiết kế cẩn thận
- **Animation Mượt Mà** - Chuyển đổi và tương tác mượt mà
- **Dark Overlay Design** - Khả năng đọc được nâng cao với overlay thanh lịch

### Hệ Thống Thiết Kế
- Card glassmorphic tùy chỉnh
- Nền gradient đẹp mắt
- Chỉ báo thời tiết dựa trên icon
- Layout responsive cho mọi kích thước màn hình

## 🏗️ Kiến Trúc Dự Án

```
app/src/main/java/com/example/weatherapp/
├── ui/
│   ├── activities/          # Các Activity
│   │   ├── MainActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── SearchActivity.java
│   │   ├── OutfitSuggestionActivity.java
│   │   ├── WeatherDetailsActivity.java
│   │   ├── ChartsActivity.java
│   │   └── FavoriteCitiesActivity.java
│   └── adapters/           # Các Adapter
│       ├── OutfitSuggestionAdapter.java
│       └── CityWeatherAdapter.java
├── data/
│   ├── models/             # Data Models
│   │   ├── HourlyForecast.java
│   │   ├── WeeklyForecast.java
│   │   ├── WeatherAlert.java
│   │   ├── OutfitSuggestion.java
│   │   ├── FavoriteCity.java
│   │   └── CityWeather.java
│   ├── api/                # API Services
│   │   ├── WeatherApiService.java
│   │   ├── OpenAIService.java
│   │   └── RetrofitClient.java
│   └── responses/          # API Response Models
│       ├── WeatherResponse.java
│       ├── HourlyForecastResponse.java
│       ├── WeatherAlertsResponse.java
│       ├── UVIndexResponse.java
│       ├── AirQualityResponse.java
│       ├── OpenAIResponse.java
│       └── OpenAIRequest.java
├── utils/                  # Utility Classes
│   ├── LocaleHelper.java
│   ├── BlurHelper.java
│   ├── FavoriteCitiesManager.java
│   └── OutfitSuggestionService.java
├── notification/           # Notification System
│   ├── WeatherNotificationManager.java
│   ├── WeatherNotificationWorker.java
│   └── NotificationReceiver.java
└── widget/                 # Home Screen Widget
    └── WeatherWidget.java
```

## 🛠️ Công Nghệ

- **Ngôn Ngữ**: Java
- **Nền Tảng**: Android (API 24+)
- **Kiến Trúc**: Clean Architecture với package structure chuẩn
- **UI Framework**: Native Android XML layouts
- **Weather API**: OpenWeatherMap API
- **AI API**: OpenAI GPT cho gợi ý trang phục
- **Location Services**: Android Location API
- **Networking**: Retrofit 2
- **Background Tasks**: WorkManager
- **Data Persistence**: SharedPreferences & File Storage

## 📋 Yêu Cầu

- Android Studio Arctic Fox trở lên
- Android SDK API 24+ (Android 7.0)
- OpenWeatherMap API Key
- OpenAI API Key (cho tính năng AI)
- Kết nối Internet

## 🚀 Cài Đặt

1. **Clone repository**
   ```bash
   git clone https://github.com/nichikei/weather-app.git
   cd weather-app
   ```

2. **Mở project trong Android Studio**
   - File → Open → Chọn thư mục project

3. **Cấu hình API Keys**
   - Tạo file `local.properties` (nếu chưa có)
   - Thêm API keys của bạn:
   ```properties
   OPENWEATHER_API_KEY=your_openweather_api_key_here
   OPENAI_API_KEY=your_openai_api_key_here
   ```

4. **Build project**
   - Build → Clean Project
   - Build → Rebuild Project

5. **Chạy ứng dụng**
   - Chọn device/emulator
   - Run → Run 'app'

## 📱 Cách Sử Dụng

1. **Lần Đầu Mở App**
   - Cấp quyền truy cập vị trí
   - App sẽ tự động lấy thời tiết vị trí hiện tại

2. **Tìm Kiếm Thành Phố**
   - Nhấn icon tìm kiếm ở top bar
   - Nhập tên thành phố
   - Chọn từ kết quả tìm kiếm

3. **Xem Dự Báo**
   - Vuốt cards để xem dự báo theo giờ/tuần
   - Nhấn vào card để xem chi tiết

4. **Gợi Ý Trang Phục**
   - Nhấn icon trang phục để xem gợi ý AI
   - Dựa trên nhiệt độ, thời tiết và hoạt động

5. **Cài Đặt**
   - Nhấn icon cài đặt
   - Tùy chỉnh đơn vị (°C/°F)
   - Bật/tắt thông báo
   - Quản lý thành phố yêu thích

## 🎯 Roadmap

- [ ] Thêm nhiều ngôn ngữ (tiếng Anh, v.v.)
- [ ] Tích hợp Material You dynamic colors
- [ ] Chế độ offline với cache
- [ ] Radar thời tiết
- [ ] Chia sẻ thông tin thời tiết lên social media
- [ ] Widget nâng cao với nhiều layout

## 🤝 Đóng Góp

Mọi đóng góp đều được chào đón!

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push lên branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

## 📄 License

Project này được phân phối dưới MIT License. Xem file `LICENSE` để biết thêm chi tiết.

## 👤 Tác Giả

**Nichikei**
- GitHub: [@nichikei](https://github.com/nichikei)
- Repository: [weather-app](https://github.com/nichikei/weather-app)

## 🙏 Cảm Ơn

- [OpenWeatherMap](https://openweathermap.org/) - Weather data API
- [OpenAI](https://openai.com/) - AI outfit suggestions
- [Flaticon](https://www.flaticon.com/) - Weather icons
- [Unsplash](https://unsplash.com/) - Background images

---

⭐ Nếu bạn thấy project này hữu ích, hãy cho một star nhé!

