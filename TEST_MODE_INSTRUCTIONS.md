# 🧪 Test Mode - Smart Weather Alerts

## ⚡ Cấu hình Test Mode đã bật

Để test nhanh thông báo thời tiết, tôi đã giảm các thời gian như sau:

### ⏱️ Thời gian đã thay đổi:

| Tham số | Production | Test Mode |
|---------|-----------|-----------|
| **Check Frequency** | 30 phút | **2 phút** ⚡ |
| **Alert Cooldown** | 30 phút | **1 phút** ⚡ |
| **UV Alert Time** | 10AM-3PM | **Bất kỳ lúc nào** ⚡ |
| **UV Threshold** | UV ≥ 6 | **UV ≥ 3** ⚡ |
| **AQI Threshold** | AQI ≥ 101 | **AQI ≥ 50** ⚡ |
| **Temp Change** | ≥ 5°C | **≥ 0.5°C** ⚡ |

### 📱 Cách Test:

#### 1. **Cài đặt App**
```bash
# Build và install APK
.\gradlew installDebug
```

#### 2. **Bật Permissions**
- Mở app lần đầu
- Cho phép **Location permission**
- Cho phép **Notification permission**
- App sẽ hiện toast: *"Weather alerts will check every 2 minutes (Test Mode)"*

#### 3. **Đợi Thông Báo**
- ⏰ **Thông báo đầu tiên**: Sau ~2 phút
- ⏰ **Các thông báo sau**: Mỗi 2 phút

#### 4. **Các loại thông báo sẽ test:**

**a) 🌡️ Weather Monitoring Started (Ngay lập tức)**
```
🌡️ Weather Monitoring Active! 
Current temperature: 25.0°C. 
You will receive alerts for weather changes.
```

**b) 😷 Air Quality Alert (Mỗi 2 phút nếu AQI > 50)**
```
😷 Good/Moderate Air Quality! 
AQI: 85 (PM2.5). 
Air quality is being monitored.
```

**c) ☀️ UV Index Warning (Nếu UV ≥ 3)**
```
☀️ High UV Index: 5. 
Apply sunscreen and wear protective clothing if outdoors.
```

**d) 🌧️ Rain Warning (Nếu forecast có mưa)**
```
🌧️ Rain Alert
Rain expected in 30 minutes! Probability: 70%. 
Don't forget your umbrella! ☂️
```

**e) 🌡️ Temperature Change (Nếu nhiệt độ thay đổi ≥ 0.5°C)**
```
🌡️ Sudden temperature change! 
From 25.0° to 25.7° (0.7° change). 
Adjust your clothing accordingly!
```

### 🔍 Debug & Monitoring

#### Xem Logs:
```bash
# Xem logs của Worker
adb logcat | findstr "SmartWeatherAlertWorker"

# Xem logs của Notification Manager
adb logcat | findstr "SmartWeatherNotif"

# Xem logs của MainActivity
adb logcat | findstr "Smart Weather Alerts"
```

#### Check WorkManager Status:
```bash
adb shell dumpsys jobscheduler | findstr weather
```

### 🎯 Expected Behavior:

**Timeline:**
- **T+0s**: App opens, scheduling starts
- **T+2m**: First alert check (Weather Monitoring Started)
- **T+3m**: Second alert (if conditions met, after 1min cooldown)
- **T+4m**: Third alert check
- **T+5m**: Fourth alert (if conditions met)
- ...continues every 2 minutes

### 📊 Test Checklist:

- [ ] App install thành công
- [ ] Toast message hiển thị "Test Mode"
- [ ] Location permission granted
- [ ] Notification permission granted
- [ ] Thông báo đầu tiên sau 2-3 phút
- [ ] Thông báo tiếp theo sau thêm 2 phút
- [ ] Notification channels được tạo
- [ ] Click notification mở app
- [ ] Settings switches hoạt động
- [ ] Tắt switch = không nhận notification loại đó

### ⚙️ Troubleshooting:

**Không nhận được thông báo?**

1. **Check Battery Optimization:**
   ```
   Settings → Apps → Weather App → Battery → Unrestricted
   ```

2. **Check Notification Settings:**
   ```
   Settings → Apps → Weather App → Notifications → All enabled
   ```

3. **Force trigger Worker:**
   ```bash
   adb shell cmd jobscheduler run -f com.example.weatherapp <job-id>
   ```

4. **Clear app data và thử lại:**
   ```bash
   adb shell pm clear com.example.weatherapp
   ```

### 🔄 Chuyển về Production Mode:

Khi test xong, thay đổi lại các giá trị:

**MainActivity.java:**
```java
// Change from:
int frequency = 2; // 2 minutes for testing

// To:
int frequency = alertPrefs.getAlertFrequency(); // 30 minutes
```

**SmartWeatherNotificationManager.java:**
```java
// Change cooldown from:
private static final long ALERT_COOLDOWN = 1 * 60 * 1000; // 1 minute

// To:
private static final long ALERT_COOLDOWN = 30 * 60 * 1000; // 30 minutes
```

**Restore thresholds:**
- UV: `if (hour >= 10 && hour <= 15 && uvIndex >= 6)`
- AQI: `if (aqi >= 101)`
- Temp: `if (tempChange >= 5)`

### 📝 Test Notes:

- Worker chạy **background** ngay cả khi app đóng
- Cần **internet connection** để fetch weather data
- **Location** phải bật để lấy coordinates
- Notifications có thể bị delay ~30s do Android scheduling

### 🎉 Success Indicators:

✅ Notification appears trong **2-3 phút**  
✅ Multiple alerts mỗi **2 phút**  
✅ **Different alert types** hiển thị  
✅ Click notification **mở app**  
✅ Settings **control** alerts  

---

**Ready to test!** 🚀 Install app và đợi 2 phút để xem thông báo đầu tiên!
