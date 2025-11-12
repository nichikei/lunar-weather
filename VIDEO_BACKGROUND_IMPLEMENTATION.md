# Tài Liệu: Tự Động Thay Đổi Video Nền Dựa Trên Điều Kiện Thời Tiết

## Tổng Quan
Tính năng này tự động thay đổi video nền của MainActivity dựa trên điều kiện thời tiết hiện tại và thời gian trong ngày (ngày/đêm).

## Các File Đã Được Sửa Đổi

### 1. `activity_main.xml`
- **Thêm mới:** VideoView để hiển thị video nền
```xml
<VideoView
    android:id="@+id/videoBackground"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_gravity="center" />
```

### 2. `MainActivity.java`

#### Biến Thành Viên Mới
```java
private android.widget.VideoView videoBackground;
```

#### Phương Thức Mới 1: `getVideoForWeather()`
```java
private int getVideoForWeather(String condition, boolean isNight)
```

**Mục đích:** Xác định video nào cần phát dựa trên điều kiện thời tiết và thời gian trong ngày.

**Tham số:**
- `condition` (String): Điều kiện thời tiết (ví dụ: "Rain", "Clear", "Clouds")
- `isNight` (boolean): Có phải ban đêm không

**Logic:**
1. **Ưu tiên 1 - Ban đêm:** Nếu `isNight = true`, trả về `R.raw.night`
2. **Ưu tiên 2 - Ban ngày:**
   - Nếu condition chứa "rain", "drizzle", hoặc "storm" → `R.raw.rain`
   - Nếu condition chứa "cloud", "fog", hoặc "haze" → `R.raw.cloudy`
   - Nếu condition chứa "clear" hoặc "sun" → `R.raw.sunset`
   - Mặc định → `R.raw.cloudy`

**Giá trị trả về:** Resource ID của video phù hợp

#### Phương Thức Mới 2: `setupVideoView()`
```java
private void setupVideoView(String condition, boolean isNight)
```

**Mục đích:** Thiết lập và phát video nền.

**Tham số:**
- `condition` (String): Điều kiện thời tiết
- `isNight` (boolean): Có phải ban đêm không

**Chức năng:**
1. Gọi `getVideoForWeather()` để lấy ID video phù hợp
2. Tạo URI cho video: `"android.resource://" + packageName + "/" + videoId`
3. Thiết lập VideoView:
   - Đặt video URI
   - Bật chế độ lặp (looping)
   - Tắt âm thanh (mute)
   - Xử lý lỗi
4. Bắt đầu phát video

#### Phương Thức Đã Sửa Đổi: `updateWeatherBackground()`
```java
private void updateWeatherBackground(WeatherData data)
```

**Cập nhật:**
- Trích xuất điều kiện thời tiết từ `data.getWeatherMain()`
- Lấy sunrise/sunset từ `data.getSunrise()` và `data.getSunset()`
- Tính toán `isNight` dựa trên thời gian sunrise/sunset
- Gọi `setupVideoView()` với các tham số phù hợp

#### Trong `onCreate()`
```java
// Initialize video background
videoBackground = binding.videoBackground;
setupVideoView("clear", false); // Default video
```
- Khởi tạo VideoView từ binding
- Thiết lập video mặc định (clear, ban ngày)

#### Trong `onDestroy()`
```java
// Stop and release video resources
if (videoBackground != null) {
    videoBackground.stopPlayback();
    videoBackground = null;
}
```
- Dọn dẹp tài nguyên video khi Activity bị hủy

## Các File Video
Vị trí: `app/src/main/res/raw/`

| File | Sử dụng cho |
|------|-------------|
| `night.mp4` | Ban đêm (bất kể điều kiện thời tiết) |
| `rain.mp4` | Mưa, mưa phùn, bão (ban ngày) |
| `cloudy.mp4` | Có mây, sương mù, sương khói (ban ngày) |
| `sunset.mp4` | Trời quang, có nắng (ban ngày) |

## Cách Hoạt Động

### Luồng Dữ Liệu
1. **API trả về dữ liệu thời tiết** → `WeatherData` object
2. **Observer cập nhật UI** → Gọi `updateWeatherBackground(data)`
3. **Trích xuất thông tin:**
   - Condition: `data.getWeather().get(0).getMain()`
   - Current time: `System.currentTimeMillis() / 1000`
   - Sunrise: `data.getSys().getSunrise()`
   - Sunset: `data.getSys().getSunset()`
4. **Tính toán isNight:**
   ```java
   isNight = currentTime < sunrise || currentTime > sunset
   ```
5. **Gọi `setupVideoView(condition, isNight)`**
6. **Video được phát tự động**

### Ví Dụ Kịch Bản

#### Kịch Bản 1: Trời mưa ban ngày
- **Input:** condition = "Rain", isNight = false
- **Logic:** Chứa "rain" → Chọn `R.raw.rain`
- **Kết quả:** Video mưa được phát

#### Kịch Bản 2: Trời quang vào ban đêm
- **Input:** condition = "Clear", isNight = true
- **Logic:** isNight = true → Ưu tiên ban đêm
- **Kết quả:** Video ban đêm được phát (bỏ qua condition)

#### Kịch Bản 3: Có mây ban ngày
- **Input:** condition = "Clouds", isNight = false
- **Logic:** Chứa "cloud" → Chọn `R.raw.cloudy`
- **Kết quả:** Video có mây được phát

#### Kịch Bản 4: Điều kiện không xác định ban ngày
- **Input:** condition = "Mist", isNight = false
- **Logic:** Không khớp điều kiện nào → Mặc định
- **Kết quả:** Video có mây được phát (mặc định)

## Tích Hợp Với Dữ Liệu API

Khi bạn nhận được dữ liệu từ API:

```java
// Ví dụ trong Observer hoặc callback
WeatherData weatherData = ...; // Dữ liệu từ API

// Phương thức updateWeatherBackground đã tự động được gọi
// trong setupObservers() khi dữ liệu thay đổi
```

Bạn có thể cung cấp 2 biến:
- `currentCondition` (String): Từ API response
- `isCurrentlyNight` (boolean): Tính toán từ sunrise/sunset

Ví dụ gọi trực tiếp (nếu cần):
```java
String currentCondition = weatherData.getWeather().get(0).getMain();
boolean isCurrentlyNight = /* tính toán từ sunrise/sunset */;
setupVideoView(currentCondition, isCurrentlyNight);
```

## Lưu Ý Kỹ Thuật

1. **Performance:**
   - Video được loop vô hạn
   - Video bị mute để không làm phiền người dùng
   - Tài nguyên được giải phóng trong `onDestroy()`

2. **Error Handling:**
   - Xử lý lỗi khi VideoView không thể load video
   - Log chi tiết để debug

3. **Memory Management:**
   - VideoView được cleanup đúng cách trong lifecycle
   - Tránh memory leak

4. **Default Behavior:**
   - Video mặc định: "clear" (sunset), ban ngày
   - Đảm bảo luôn có video hiển thị ngay khi app khởi động

## Testing

### Test Cases Nên Kiểm Tra:
1. ✓ Video ban đêm hiển thị khi sau sunset
2. ✓ Video mưa hiển thị khi điều kiện = "Rain"
3. ✓ Video có mây hiển thị khi điều kiện = "Clouds"
4. ✓ Video sunset hiển thị khi điều kiện = "Clear" (ban ngày)
5. ✓ Video mặc định hiển thị khi điều kiện không xác định
6. ✓ Video ban đêm luôn có độ ưu tiên cao nhất
7. ✓ Video loop liên tục không dừng
8. ✓ Không có âm thanh phát ra
9. ✓ Tài nguyên được giải phóng khi thoát app

## Troubleshooting

### Video không phát:
- Kiểm tra file video tồn tại trong `res/raw/`
- Kiểm tra tên file khớp với code (night.mp4, rain.mp4, cloudy.mp4, sunset.mp4)
- Xem Log với tag "MainActivity" để debug

### Video bị lag hoặc giật:
- Tối ưu file video (giảm resolution hoặc bitrate)
- Kiểm tra kích thước file (nên < 5MB)

### Memory leak:
- Đảm bảo `videoBackground.stopPlayback()` được gọi trong `onDestroy()`
- Kiểm tra videoBackground = null sau khi cleanup

## Tác Giả & Thời Gian
- **Ngày triển khai:** 12/11/2025
- **Nền tảng:** Android Studio, Java
- **Yêu cầu tối thiểu:** Android API 21+
