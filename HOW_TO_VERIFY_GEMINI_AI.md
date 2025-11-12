# 🔍 How to Verify Gemini AI is Being Used (NOT Default Suggestions)

## Tại sao cần verify?

Bạn thắc mắc đúng! App có thể đang dùng **Default Suggestions** thay vì **Gemini AI** nếu:
- ❌ API key không đúng
- ❌ Network error
- ❌ Response parsing lỗi
- ❌ Gemini API trả về empty

## 🎯 Cách kiểm tra 100% App đang dùng Gemini AI

### 1️⃣ **Xem Logcat trong Android Studio**

```bash
# Filter logs by tag
adb logcat ActivitySuggestions:D ActivitySuggestionService:D *:S
```

### 2️⃣ **Logs khi dùng GEMINI AI (THẬT):**

```
════════════════════════════════════════════════
║  🤖 USING GEMINI AI FOR RECOMMENDATIONS      ║
╚════════════════════════════════════════════════╝
📍 City: Hanoi
🌡️  Temperature: 28.5°C (Feels: 32.1°C)
💧 Humidity: 75%
💨 Wind: 3.2 m/s
☁️  Condition: Clear sky
☀️  UV Index: 8
🌫️  AQI: 65
🔧 Model: gemini-2.0-flash-exp
════════════════════════════════════════════════
📡 Calling Gemini API...
🔍 Parsing Gemini response...
Response length: 2345 chars
Found 1 candidates
📝 Generated text preview: [
  {
    "title": "Morning Jogging",
    ...
🧹 Cleaned text preview: [
  {
    "title": "Morning Jogging",
    ...
Found 8 activities in response
  ✓ Activity 1: Morning Jogging (score: 75)
  ✓ Activity 2: Indoor Yoga (score: 90)
  ✓ Activity 3: Swimming (score: 85)
  ...
✅ Successfully parsed 8 AI-generated suggestions
════════════════════════════════════════════════
✅ GEMINI API SUCCESS!
📊 Received: 8 AI-generated suggestions
⏱️  Response time: 1234ms
🎯 Source: GEMINI AI (NOT DEFAULT)
════════════════════════════════════════════════
```

### 3️⃣ **Logs khi dùng DEFAULT (Fallback):**

```
════════════════════════════════════════════════
❌ GEMINI API ERROR: IOException
💬 Message: Failed to connect to generativelanguage.googleapis.com
🔄 Falling back to DEFAULT SUGGESTIONS
════════════════════════════════════════════════
```

## 📊 So sánh: AI vs Default Suggestions

| Đặc điểm | Gemini AI | Default Suggestions |
|----------|-----------|---------------------|
| **Số lượng** | 6-10 activities | 6 activities (fixed) |
| **Nội dung** | Thay đổi theo thời tiết thực tế | Luôn giống nhau |
| **Scores** | Dựa trên weather data | Hardcoded |
| **Reasons** | Cụ thể (e.g., "High UV 8 - Stay hydrated") | Generic (e.g., "Good for health") |
| **Response time** | 1-3 seconds (API call) | Instant |
| **Internet** | Bắt buộc | Không cần |

## 🧪 Test Case để verify

### Test 1: So sánh 2 thành phố khác biệt

1. **Hanoi (28°C, UV 8, AQI 65)**
   ```
   Expected AI Suggestions:
   ✓ "Indoor Activities" (score 90+) - High UV warning
   ✓ "Swimming" (score 85+) - Hot weather cooling
   ✓ "Air-conditioned Mall" (score 88+) - Poor AQI
   ```

2. **Tokyo (15°C, UV 3, AQI 25)**
   ```
   Expected AI Suggestions:
   ✓ "Outdoor Walking" (score 90+) - Perfect temp
   ✓ "Park Picnic" (score 85+) - Good AQI
   ✓ "Cycling" (score 88+) - Low UV, cool weather
   ```

**❗ Nếu 2 thành phố cho suggestions GIỐNG NHAU → Đang dùng Default!**

### Test 2: Kiểm tra Response Time

- **Gemini AI**: 1-3 seconds (có loading indicator)
- **Default**: < 100ms (instant)

→ Click button "Activity Suggestions" và đo thời gian loading

### Test 3: Xem Toast Message

```
✨ 8 activities suggested (AI-powered)  ← Gemini AI
✨ 6 activities suggested              ← Default
```

## 🔧 Troubleshooting

### Vấn đề 1: Luôn thấy Default Suggestions

**Nguyên nhân:**
- API key không đúng hoặc expired
- Network bị chặn (firewall, proxy)
- Gemini API quota hết

**Giải pháp:**
```bash
# Check API key
grep "GEMINI_API_KEY" app/src/main/java/com/example/weatherapp/domain/services/ActivitySuggestionService.java

# Test API key manually
curl -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=YOUR_KEY" \
  -H "Content-Type: application/json" \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

### Vấn đề 2: Parsing Error

**Logs:**
```
❌ JSON Parsing Error: org.json.JSONException: Value [...] at 0 of type java.lang.String cannot be converted to JSONArray
Response body: ```json
[
  ...
]
```
```

**Nguyên nhân:** Gemini trả về markdown wrapper `\`\`\`json ... \`\`\``

**Giải pháp:** Code đã xử lý rồi! Kiểm tra logs có dòng:
```
🧹 Cleaned text preview: [
```

### Vấn đề 3: Empty Response

**Logs:**
```
⚠️  Gemini API returned empty response
🔄 Falling back to DEFAULT SUGGESTIONS
```

**Nguyên nhân:** Gemini từ chối generate (safety filters, content policy)

**Giải pháp:** Kiểm tra prompt có nội dung nhạy cảm không

## ✅ Checklist để Confirm đang dùng Gemini AI

- [ ] Logcat có dòng `🤖 USING GEMINI AI FOR RECOMMENDATIONS`
- [ ] Có dòng `✅ GEMINI API SUCCESS!`
- [ ] Có dòng `🎯 Source: GEMINI AI (NOT DEFAULT)`
- [ ] Response time 1-3 seconds (không phải instant)
- [ ] Suggestions thay đổi khi đổi thành phố
- [ ] Số lượng activities: 6-10 (không phải luôn 6)
- [ ] Reasons cụ thể (mention UV, AQI, temperature)
- [ ] Toast hiển thị số lượng activities

## 📱 Cách chạy test nhanh

```bash
# 1. Build and Install
cd d:\Codespace\weather-application\weather-app
.\gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Run app và xem logs
adb logcat -c  # Clear logs
adb logcat ActivitySuggestions:D ActivitySuggestionService:D *:S

# 3. Trong app:
# - Chọn Hanoi → Click "Activity Suggestions"
# - Back → Search Tokyo → Click again
# - Xem logs để verify AI được gọi 2 lần

# 4. Verify suggestions khác nhau
adb shell "screencap -p /sdcard/hanoi_suggestions.png"
# (Chuyển sang Tokyo)
adb shell "screencap -p /sdcard/tokyo_suggestions.png"
# So sánh 2 screenshots
```

## 🎓 Summary

**Để 100% chắc chắn app đang dùng Gemini AI:**

1. ✅ Check Logcat có `✅ GEMINI API SUCCESS!`
2. ✅ Suggestions thay đổi theo weather data thật
3. ✅ Response time 1-3 giây (có loading)
4. ✅ Reasons mention UV Index, AQI cụ thể
5. ✅ Số lượng activities biến đổi (6-10)

**Nếu thấy logs này → Bạn đang dùng Default:**
```
⚠️ USING DEFAULT SUGGESTIONS
```

---

Made with ❤️ by GitHub Copilot | Last updated: November 11, 2025
