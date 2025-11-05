# HƯỚNG DẪN REFACTOR DỰ ÁN

## Cấu trúc mới của dự án:

```
com.example.weatherapp/
├── ui/
│   ├── activities/          # Tất cả Activities
│   │   ├── MainActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── SearchActivity.java
│   │   ├── OutfitSuggestionActivity.java
│   │   ├── WeatherDetailsActivity.java
│   │   ├── ChartsActivity.java
│   │   └── FavoriteCitiesActivity.java
│   └── adapters/            # Tất cả Adapters
│       ├── OutfitSuggestionAdapter.java
│       └── CityWeatherAdapter.java
│
├── data/
│   ├── models/              # Data models
│   │   ├── HourlyForecast.java
│   │   ├── WeeklyForecast.java
│   │   ├── WeatherAlert.java
│   │   ├── OutfitSuggestion.java
│   │   ├── FavoriteCity.java
│   │   └── CityWeather.java
│   ├── api/                 # API Services
│   │   ├── WeatherApiService.java
│   │   ├── OpenAIService.java
│   │   └── RetrofitClient.java
│   └── responses/           # API Response models
│       ├── WeatherResponse.java
│       ├── HourlyForecastResponse.java
│       ├── WeatherAlertsResponse.java
│       ├── UVIndexResponse.java
│       ├── AirQualityResponse.java
│       ├── OpenAIResponse.java
│       └── OpenAIRequest.java
│
├── utils/                   # Helper classes
│   ├── LocaleHelper.java
│   ├── BlurHelper.java
│   ├── FavoriteCitiesManager.java
│   └── OutfitSuggestionService.java
│
├── notification/            # Notification related
│   ├── WeatherNotificationManager.java
│   ├── WeatherNotificationWorker.java
│   └── NotificationReceiver.java
│
└── widget/                  # Widget classes
    └── WeatherWidget.java
```

## Cách chạy script:

1. Mở PowerShell **AS ADMINISTRATOR** (quan trọng!)
2. Điều hướng đến thư mục dự án:
   ```powershell
   cd C:\Users\DELL\AndroidStudioProjects\WeatherApp
   ```

3. Cho phép chạy script (chỉ cần làm 1 lần):
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   ```

4. Chạy script:
   ```powershell
   .\refactor_project.ps1
   ```

## Sau khi chạy script:

1. Mở Android Studio
2. Chọn **File → Invalidate Caches / Restart**
3. Sau khi restart, chọn **Build → Clean Project**
4. Sau đó chọn **Build → Rebuild Project**
5. Android Studio sẽ tự động update AndroidManifest.xml

## Nếu có lỗi import:

- Right-click vào file bị lỗi → **Show Context Actions** (Alt+Enter)
- Chọn **Optimize Imports** hoặc **Import class**
- Hoặc dùng **Code → Optimize Imports** (Ctrl+Alt+O)

## Lợi ích của cấu trúc mới:

✅ **Dễ tìm kiếm**: Mỗi loại file ở một nơi
✅ **Dễ bảo trì**: Tách biệt UI, Data, và Logic
✅ **Dễ mở rộng**: Thêm features mới dễ dàng hơn
✅ **Theo chuẩn Android**: Clean Architecture pattern
✅ **Team work**: Nhiều người làm việc không conflict

## Backup:

Script đã tự động:
- Tạo thư mục mới
- Copy file vào vị trí mới
- Update package declarations
- Update tất cả imports
- Xóa file cũ sau khi move thành công

