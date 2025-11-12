# 🔔 Smart Weather Alerts Feature

## Tổng quan

Hệ thống **Smart Weather Alerts** cung cấp thông báo thời tiết thông minh, tự động theo dõi và cảnh báo người dùng về các điều kiện thời tiết quan trọng.

## ✨ Tính năng

### 1. 🌧️ Cảnh báo Mưa
- **Thời gian**: Cảnh báo trước 30-60 phút khi có mưa
- **Thông tin**: Xác suất mưa và thời gian dự kiến
- **Hành động**: Nhắc người dùng mang theo ô/áo mưa

### 2. ☀️ Cảnh báo UV Cao
- **Thời gian**: 10 AM - 3 PM (thời điểm UV cao nhất)
- **Mức độ**:
  - UV 6-7: Cảnh báo cao
  - UV 8-10: Rất cao
  - UV 11+: Cực kỳ nguy hiểm
- **Khuyến nghị**: Sử dụng kem chống nắng, mặc áo bảo vệ

### 3. 😷 Cảnh báo Chất Lượng Không Khí
- **AQI 101-150**: Không tốt - nhóm nhạy cảm nên hạn chế hoạt động ngoài trời
- **AQI 151-200**: Không lành mạnh - tránh hoạt động ngoài trời
- **AQI 201+**: Rất không lành mạnh - mọi người nên ở trong nhà

### 4. 🌡️ Cảnh báo Thay Đổi Thời Tiết Đột Ngột
- Nhiệt độ thay đổi >= 5°C trong thời gian ngắn
- Điều kiện thời tiết thay đổi đáng kể
- Khuyến nghị điều chỉnh trang phục

## 📱 Sử dụng

### Bật/Tắt Thông Báo

1. Mở **Settings** (Cài đặt)
2. Cuộn xuống phần **"Smart Weather Alerts"**
3. Bật/tắt từng loại thông báo:
   - 🌧️ Rain Warnings
   - ☀️ UV Index Warnings
   - 😷 Air Quality Alerts
   - 🌡️ Weather Changes

### Tần suất Kiểm tra

Mặc định: Kiểm tra mỗi **30 phút**

Có thể tùy chỉnh trong `WeatherAlertPreferences`:
```java
alertPrefs.setAlertFrequency(30); // phút
```

## 🔧 Cấu trúc Code

### 1. Models
- **`WeatherAlert.java`**: Data model cho các loại cảnh báo
  - AlertType: Loại cảnh báo
  - AlertSeverity: Mức độ nghiêm trọng
  - Timestamp và message

### 2. Notification Manager
- **`SmartWeatherNotificationManager.java`**: Quản lý thông báo
  - Tạo notification channels
  - Kiểm tra cooldown để tránh spam
  - Gửi thông báo với priority phù hợp

### 3. Background Worker
- **`SmartWeatherAlertWorker.java`**: WorkManager worker
  - Chạy định kỳ mỗi 30 phút
  - Fetch dữ liệu thời tiết
  - Phân tích và gửi cảnh báo

### 4. Preferences
- **`WeatherAlertPreferences.java`**: Lưu cài đặt người dùng
  - Bật/tắt từng loại alert
  - Tần suất kiểm tra

### 5. Scheduler
- **`WeatherAlertScheduler.java`**: Lên lịch cho worker
  - Schedule periodic work
  - Cancel khi không cần

### 6. Helper
- **`LocationHelper.java`**: Lấy vị trí hiện tại

## 🎯 Alert Logic

### Rain Warning
```java
if (rainProbability >= 60 && minutesUntilRain <= 60) {
    showRainAlert(probability, minutes);
}
```

### UV Index
```java
if (hour >= 10 && hour <= 15 && uvIndex >= 6) {
    showUVAlert(uvIndex, severity);
}
```

### Air Quality
```java
if (aqi >= 101) {
    showAirQualityAlert(aqi, pollutant, severity);
}
```

### Weather Change
```java
double tempChange = Math.abs(currentTemp - previousTemp);
if (tempChange >= 5) {
    showWeatherChangeAlert(tempChange);
}
```

## ⚙️ Notification Channels

### 1. Critical Alerts (High Priority)
- Storms, extreme weather
- Vibration pattern: Strong
- Sound: Loud

### 2. Weather Warnings (Default Priority)
- Rain, UV, Air Quality
- Vibration pattern: Medium
- Sound: Normal

### 3. Weather Info (Low Priority)
- General updates
- No vibration
- No sound

## 🔔 Cooldown System

Để tránh spam thông báo:
- **Cooldown time**: 30 phút
- Mỗi loại alert có cooldown riêng
- Lưu timestamp của alert cuối cùng

```java
private static final long ALERT_COOLDOWN = 30 * 60 * 1000; // 30 minutes
```

## 📊 Alert Severity Levels

1. **LOW**: Thông tin chung
2. **MEDIUM**: Cảnh báo cần chú ý
3. **HIGH**: Nguy hiểm, cần hành động
4. **CRITICAL**: Rất nguy hiểm, hành động ngay

## 🚀 Khởi động

Smart Weather Alerts được khởi động tự động khi:
1. App mở lần đầu (`MainActivity.onCreate()`)
2. Settings được thay đổi (`SettingsActivity`)

```java
// Initialize in MainActivity
initializeSmartWeatherAlerts();
```

## 📝 Permissions Required

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

## 🎨 UI Components

### Settings Layout
Thêm vào `activity_settings.xml`:
- Rain Alerts Switch
- UV Alerts Switch
- Air Quality Alerts Switch
- Weather Change Alerts Switch

## 🔮 Future Enhancements

1. **Custom Alert Times**: Cho phép người dùng chọn giờ nhận thông báo
2. **Location-based Alerts**: Alerts cho nhiều địa điểm
3. **Alert History**: Lịch sử các cảnh báo đã nhận
4. **Quiet Hours**: Tắt thông báo vào giờ ngủ
5. **Severity Threshold**: Tùy chỉnh ngưỡng cho từng loại alert
6. **Rich Notifications**: Thêm action buttons (View Details, Dismiss)
7. **Weather Radar Integration**: Hiển thị radar map trong notification
8. **Voice Alerts**: Đọc thông báo bằng giọng nói

## 🐛 Troubleshooting

### Không nhận được thông báo?
1. Kiểm tra notification permission
2. Kiểm tra battery optimization
3. Verify WorkManager đang chạy
4. Check log: `adb logcat | grep SmartWeather`

### Thông báo bị spam?
1. Kiểm tra cooldown settings
2. Điều chỉnh alert frequency
3. Tắt các alert không cần thiết

## 📚 References

- WorkManager: https://developer.android.com/topic/libraries/architecture/workmanager
- Notifications: https://developer.android.com/develop/ui/views/notifications
- Background Work: https://developer.android.com/guide/background

---

**Author**: Weather App Team  
**Version**: 1.0.0  
**Last Updated**: November 12, 2025
