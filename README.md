# Weather App 🌤️ - Material Design 3 Edition

> **Version 2.0.0-MD3** - Now with Material Design 3, Smooth Animations & Interactive Weather Maps! 🎉

Ứng dụng thời tiết Android hiện đại với **Material Design 3**, kiến trúc **MVVM + Clean Architecture**, **Interactive Weather Maps**, **Smooth Animations**, glassmorphism design, offline caching với Room Database, và dữ liệu thời tiết real-time.

## 🎉 What's New in v2.0

### ✨ Material Design 3
- Complete MD3 color system với light/dark themes
- Modern glassmorphism effects
- Dynamic gradient backgrounds
- Enhanced typography hierarchy
- Elevated card designs

### 🎬 Smooth Animations
- MaterialAnimationHelper với 15+ animation types
- Page transition animations
- Smooth scroll effects với parallax
- Staggered list animations
- Button interactions với bounce/pulse effects

### 🗺️ Interactive Weather Maps (NEW!)
- **Google Maps integration** với custom dark theme
- **5 Weather Layers**:
  - 🌡️ Temperature overlay
  - 🌧️ Precipitation/Rain overlay
  - ☁️ Clouds coverage
  - 💨 Wind speed visualization
  - 📊 Pressure patterns
- Real-time OpenWeatherMap tile layers
- Layer switching với Material Design chips
- Location markers và zoom controls

**📚 See detailed changes:** [CHANGELOG.md](CHANGELOG.md)

## ✨ Tính Năng

### Tính Năng Chính
- 🌡️ **Dữ Liệu Thời Tiết Real-time** - Nhiệt độ hiện tại, điều kiện thời tiết và dự báo
- 📍 **Thời Tiết Theo Vị Trí** - Tự động phát hiện vị trí hoặc tìm kiếm thành phố
- 📊 **Thông Số Thời Tiết Chi Tiết** - Chỉ số UV, độ ẩm, tốc độ gió, áp suất, tầm nhìn xa
- 🕐 **Dự Báo Theo Giờ & Tuần** - Dự đoán thời tiết toàn diện
- 🌅 **Thời Gian Mặt Trời Mọc/Lặn** - Hiển thị sunrise/sunset đẹp mắt
- 💨 **Chỉ Số Chất Lượng Không Khí** - Theo dõi AQI khu vực của bạn
- ⚠️ **Cảnh Báo Thời Tiết** - Thông báo về các cảnh báo thời tiết nguy hiểm
- 📶 **Offline Mode** - Cache thời tiết với Room Database, hoạt động không cần internet

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

## 🏗️ Kiến Trúc: MVVM + Clean Architecture

### Architecture Overview
```
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                      │
│  ┌──────────────┐         ┌────────────────────┐       │
│  │   Activity   │◄────────│     ViewModel      │       │
│  │  (UI/View)   │ observe │  (Business Logic)  │       │
│  └──────────────┘         └────────────────────┘       │
└────────────────────────┬────────────────────────────────┘
                         │ uses
┌────────────────────────▼────────────────────────────────┐
│                    DOMAIN LAYER                          │
│  ┌──────────────┐    ┌─────────────┐   ┌────────────┐ │
│  │   UseCase    │───►│ Repository  │   │   Models   │ │
│  │  (Business   │    │ (Interface) │   │  (Entities)│ │
│  │    Logic)    │    └─────────────┘   └────────────┘ │
└────────────────────────┬────────────────────────────────┘
                         │ implements
┌────────────────────────▼────────────────────────────────┐
│                     DATA LAYER                           │
│  ┌─────────────────┐  ┌──────────┐  ┌──────────────┐  │
│  │  Repository     │──│  Mapper  │  │ API Service  │  │
│  │ Implementation  │  └──────────┘  └──────────────┘  │
│  └────────┬────────┘                                    │
│           │                                              │
│  ┌────────▼─────────┐         ┌──────────────────┐    │
│  │  Room Database   │         │  Network (API)   │    │
│  │  (Local Cache)   │         │   (Retrofit)     │    │
│  └──────────────────┘         └──────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### Project Structure
```
app/src/main/java/com/example/weatherapp/
├── presentation/               # Presentation Layer (MVVM)
│   ├── viewmodel/
│   │   ├── MainViewModel.java
│   │   └── MainViewModelFactory.java
│   └── state/
│       └── UIState.java       # Sealed class for UI states
│
├── domain/                    # Domain Layer (Business Logic)
│   ├── model/
│   │   ├── WeatherData.java
│   │   ├── ForecastData.java
│   │   └── AirQualityData.java
│   ├── repository/
│   │   └── WeatherRepository.java  # Interface
│   └── usecase/
│       ├── GetWeatherByCityUseCase.java
│       ├── GetWeatherByCoordinatesUseCase.java
│       ├── GetForecastUseCase.java
│       ├── GetUVIndexUseCase.java
│       └── GetAirQualityUseCase.java
│
├── data/                      # Data Layer (Implementation)
│   ├── repository/
│   │   └── implementation/
│   │       └── WeatherRepositoryImpl.java
│   ├── local/                 # Room Database
│   │   ├── database/
│   │   │   └── WeatherDatabase.java
│   │   ├── dao/
│   │   │   └── WeatherDao.java
│   │   ├── entity/
│   │   │   └── WeatherCacheEntity.java
│   │   └── mapper/
│   │       └── CacheMapper.java
│   ├── api/                   # Network Layer
│   │   ├── WeatherApiService.java
│   │   └── RetrofitClient.java
│   ├── responses/             # API Response Models
│   │   ├── WeatherResponse.java
│   │   ├── ForecastResponse.java
│   │   └── AirQualityResponse.java
│   └── mapper/
│       └── DomainMapper.java  # API → Domain conversion
│
└── ui/                        # UI Components
    ├── activities/
    │   ├── MainActivity.java
    │   ├── SettingsActivity.java
    │   ├── SearchActivity.java
    │   ├── OutfitSuggestionActivity.java
    │   ├── WeatherDetailsActivity.java
    │   ├── ChartsActivity.java
    │   └── FavoriteCitiesActivity.java
    ├── adapters/
    │   ├── OutfitSuggestionAdapter.java
    │   └── CityWeatherAdapter.java
    └── helpers/
        ├── UIUpdateHelper.java
        ├── UISetupHelper.java
        └── ForecastViewManager.java
```

### Key Architecture Components

#### 1. Presentation Layer (MVVM)
- **ViewModel**: Quản lý UI state và business logic
- **LiveData**: Observable data holder cho reactive UI
- **UIState**: Sealed class cho type-safe state management (Loading, Success, Error)

#### 2. Domain Layer (Pure Business Logic)
- **Models**: Domain entities không phụ thuộc framework
- **Repository Interface**: Contract cho data operations
- **UseCases**: Encapsulate business rules

#### 3. Data Layer (Implementation Details)
- **Repository Implementation**: Implement domain repository
- **Room Database**: Local caching với cache-first strategy
- **Retrofit**: Network API calls
- **Mappers**: Convert giữa layers (API → Domain, Entity → Domain)

### Cache Strategy
- ✅ **Cache-first**: Check cache trước, network sau
- ✅ **Auto-invalidation**: Cache expire sau 10 phút
- ✅ **Offline fallback**: Trả về expired cache khi network fail
- ✅ **Background operations**: Tất cả DB ops chạy background thread

## 📚 Tech Stack

### Architecture & Patterns
- **Architecture**: MVVM + Clean Architecture (3 layers)
- **Language**: Java 11
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36

### Core Libraries
- **Lifecycle**: AndroidX Lifecycle 2.7.0 (ViewModel, LiveData)
- **Room**: 2.6.1 (Local database caching)
- **Coroutines**: Kotlin Coroutines 1.7.3 (Async operations)
- **Retrofit**: 2.9.0 (Network calls)
- **Gson**: 2.10.1 (JSON parsing)

### Testing
- **JUnit**: Unit testing framework
- **Mockito**: 5.7.0 (Mocking for tests)
- **AndroidX Arch Core Testing**: 2.2.0 (LiveData testing)
- **Coroutines Test**: 1.7.3 (Async testing)

### APIs
- **OpenWeatherMap API**: Weather, Forecast, UV, Air Quality data
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

