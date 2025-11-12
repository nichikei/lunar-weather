# iOS Style Weather App UI/UX

## Tổng quan
Giao diện mới được thiết kế dựa trên Apple Weather App của iOS, mang lại trải nghiệm người dùng hiện đại và trực quan.

## Các Tính năng Thiết kế

### 1. Layout Chính (activity_main_ios_style.xml)
- **Gradient Background**: Nền gradient đậm chất iOS với màu tối (#121923 → #2B3142)
- **Scrollable Content**: Toàn bộ nội dung có thể cuộn mượt mà
- **Edge-to-Edge Display**: Giao diện trải dài toàn màn hình

### 2. Top Section - Thông tin Chính
```xml
- City Name: 37sp, Sans-serif Light, màu trắng
- Main Temperature: 102sp, Sans-serif Thin, hiển thị nổi bật
- Weather Description: 24sp, Sans-serif Regular
- High/Low Temp: 21sp, Sans-serif Medium, format "H:29° L:15°"
```

### 3. Hourly Forecast Card
**Đặc điểm:**
- Background: Glassmorphism (trong suốt với blur effect)
- Corner Radius: 15dp
- Padding: 15dp
- Horizontal Scrolling RecyclerView

**Cấu trúc item (item_hourly_forecast_ios.xml):**
- Time label (Now, 10PM, 11PM...)
- Weather icon (32dp x 32dp)
- Temperature (22sp)

### 4. 10-Day Forecast Card
**Đặc điểm:**
- Background: Glassmorphism giống Hourly Card
- Header với icon và title "10-DAY FORECAST"
- Vertical RecyclerView với các divider line

**Cấu trúc item (item_daily_forecast_ios.xml):**
```
Day | Icon | Low Temp | [Temperature Bar] | High Temp | Rain %
```

**Temperature Bar:**
- Background: Màu đen với opacity 15%
- Gradient Fill: Green → Yellow → Orange (#96D0A8 → #F8D74A → #EF8835)
- Border Radius: 44dp (pill shape)
- Dynamic width dựa trên nhiệt độ thực tế

### 5. Bottom Navigation Bar
**iOS Style Design:**
- Background: Solid color (#2A3040)
- Height: 78dp
- Home Indicator: Đường trắng 134dp x 5dp ở trên cùng

**3 Buttons:**
- Location (􀙊): Bên trái
- Weather (􀋒): Giữa, có dot indicator màu trắng
- Menu (􀋲): Bên phải

## Drawable Resources

### 1. ios_weather_gradient_bg.xml
```xml
Gradient từ #121923 → #2B3142
Góc: 180 độ (từ trên xuống)
```

### 2. ios_card_background.xml
```xml
Background: #33FFFFFF (trắng với opacity 20%)
Border: 0.5dp, #40FFFFFF (trắng với opacity 25%)
Corner Radius: 15dp
Blur Effect: Tích hợp backdrop blur
```

### 3. ios_temp_bar_gradient.xml
```xml
Gradient ngang:
Start: #96D0A8 (Green)
Center: #F8D74A (Yellow)
End: #EF8835 (Orange)
```

### 4. Weather Icons
- `ic_weather_sunny.xml`: Icon mặt trời với tia sáng
- `ic_weather_cloudy.xml`: Icon đám mây
- `ic_weather_rainy.xml`: Icon mây và mưa

## Java Classes

### 1. IOSWeatherActivity.java
**Main Activity** cho giao diện iOS Style

**Features:**
- Edge-to-edge display
- Transparent status bar
- Light status bar icons (false = dark theme)
- Sample data initialization
- RecyclerView setup

**Key Methods:**
```java
setupEdgeToEdge()     // Cấu hình fullscreen
initViews()           // Khởi tạo views
setupAdapters()       // Setup RecyclerView adapters
loadSampleData()      // Load dữ liệu mẫu
```

### 2. IOSHourlyForecastAdapter.java
Adapter cho hourly forecast với horizontal scrolling

**Data Model:**
```java
HourlyForecast {
    String time;
    String icon;
    int temperature;
}
```

### 3. IOSDailyForecastAdapter.java
Adapter cho 10-day forecast

**Data Model:**
```java
DailyForecast {
    String day;
    String icon;
    int lowTemp;
    int highTemp;
    int rainProbability;
    boolean isToday;
}
```

**Dynamic Temperature Bar:**
- Tính toán margin dựa trên nhiệt độ min/max
- Range: 10°C - 35°C
- Bar width: 100dp

## Color Palette

### Primary Colors
```
Background Gradient:
- Dark: #121923
- Medium: #242F3E  
- Light: #2B3142

Text Colors:
- Primary: #FFFFFF (white)
- Secondary: #FFFFFF with 30% opacity
- Accent: #81CFFA (blue for rain %)

Card Background:
- Glass: #33FFFFFF (white 20% opacity)
- Border: #40FFFFFF (white 25% opacity)

Bottom Nav:
- Background: #2A3040
```

### Temperature Gradient
```
Cold → Warm:
#96D0A8 (Green) → #F8D74A (Yellow) → #EF8835 (Orange)
```

## Typography

### Font Families
- **Sans-serif-thin**: Main temperature (102sp)
- **Sans-serif-light**: City name (37sp)
- **Sans-serif**: Weather description (24sp)
- **Sans-serif-medium**: Most other text (15sp-22sp)

### Text Sizes
```
102sp - Main temperature
37sp  - City name
24sp  - Weather description
22sp  - Forecast temperatures, day names
21sp  - High/Low summary
18sp  - Card descriptions
17sp  - Hourly time labels
15sp  - Card headers, rain probability
11sp  - Small icons
```

## Spacing Guidelines

### Margins
```
Horizontal margins: 21dp (cards)
Top margin: 30dp-34dp (spacing between sections)
Card padding: 15dp
Item padding: 7dp-8dp
```

### Element Spacing
```
Status bar height: 44dp
Bottom nav height: 78dp
Home indicator top margin: 58dp
Temperature bar margin: 8dp between temps
Icon spacing: 12dp vertical
```

## Cách Sử dụng

### 1. Chạy Activity mới
Thêm vào AndroidManifest.xml:
```xml
<activity
    android:name=".ui.activities.IOSWeatherActivity"
    android:exported="true"
    android:theme="@style/Theme.WeatherApp.NoActionBar" />
```

### 2. Launch từ MainActivity
```java
Intent intent = new Intent(this, IOSWeatherActivity.class);
startActivity(intent);
```

### 3. Tích hợp với API thực
Thay thế `loadSampleData()` với data từ WeatherAPI:
```java
private void loadWeatherData() {
    // Fetch from API
    weatherService.getCurrentWeather(cityName)
        .observe(this, weather -> {
            updateUI(weather);
        });
}
```

### 4. Tùy chỉnh màu sắc
Sửa đổi trong `values/colors.xml`:
```xml
<color name="ios_gradient_start">#121923</color>
<color name="ios_gradient_end">#2B3142</color>
<color name="ios_card_bg">#33FFFFFF</color>
```

## Best Practices

### 1. Performance
- Sử dụng `setNestedScrollingEnabled(false)` cho inner RecyclerView
- Cache view references trong ViewHolder
- Tránh nested weights trong layouts

### 2. Accessibility
- Thêm `contentDescription` cho tất cả ImageView
- Sử dụng minimum touch target 48dp
- Hỗ trợ dark mode

### 3. Responsive Design
- Sử dụng `wrap_content` và `match_parent` hợp lý
- Test trên nhiều kích thước màn hình
- Sử dụng ConstraintLayout cho complex layouts

## Tương lai Enhancements

### 1. Animations
- Smooth scroll reveal
- Temperature number counter animation
- Card fade-in effects
- Pull-to-refresh

### 2. Interactive Elements
- Swipe between cities
- Long press for details
- Haptic feedback
- Interactive weather animations

### 3. Advanced Features
- Real-time weather updates
- Location-based auto-refresh
- Weather alerts
- Share weather info
- Widget support

## Troubleshooting

### Issue: Cards không hiển thị đúng
**Solution:** Kiểm tra backdrop blur support trên device

### Issue: Temperature bar không align đúng
**Solution:** Verify margin calculation trong adapter

### Issue: Status bar overlap nội dung
**Solution:** Adjust top padding trong layout

### Issue: RecyclerView không scroll
**Solution:** Đảm bảo `nestedScrollingEnabled` được set đúng

## Resources References

### Figma Design
- Main Screen: node-id=1-8
- List View: node-id=105-208
- Colors from iOS Weather App guidelines

### Assets cần thêm
- SF Pro Display font (optional)
- High-res weather icons
- Background gradient images
- Blur effect overlays

## Kết luận

Giao diện iOS Style Weather App mang lại:
- ✅ Trải nghiệm người dùng hiện đại
- ✅ Thiết kế sạch sẽ, tối giản
- ✅ Animations mượt mà
- ✅ Dễ dàng tùy chỉnh
- ✅ Performance tối ưu

Để có trải nghiệm tốt nhất, nên test trên device thật với Android 10+.
