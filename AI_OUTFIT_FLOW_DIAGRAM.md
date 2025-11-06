# LUỒNG HOẠT ĐỘNG CHỨC NĂNG AI OUTFIT SUGGESTION

## 📋 TỔNG QUAN

Chức năng AI Outfit Suggestion sử dụng **Google Gemini API** (miễn phí) để gợi ý trang phục phù hợp dựa trên điều kiện thời tiết hiện tại.

---

## 🗂️ CÁC FILE LIÊN QUAN

### 1. **Model Layer** (Data)
- `OutfitSuggestion.java` - Model lưu trữ thông tin gợi ý trang phục

### 2. **Service Layer** (Business Logic)
- `OutfitSuggestionService.java` - Xử lý logic gọi API và parse dữ liệu

### 3. **View Layer** (UI)
- `MainActivity.java` - Màn hình chính, khởi tạo flow
- `OutfitSuggestionActivity.java` - Màn hình hiển thị gợi ý
- `OutfitSuggestionAdapter.java` - Adapter cho RecyclerView

### 4. **Layout Files**
- `activity_outfit_suggestion.xml` - Layout của OutfitSuggestionActivity
- `item_outfit_suggestion.xml` - Layout của từng item gợi ý

---

## 🔄 LUỒNG HOẠT ĐỘNG CHI TIẾT

### **BƯỚC 1: User Mở Chức Năng** 👆
```
MainActivity.java
├─ User bấm nút "Outfit Suggestion"
├─ Method: openOutfitSuggestionActivity()
│  ├─ Kiểm tra currentWeatherData != null
│  ├─ Tạo Intent để mở OutfitSuggestionActivity
│  └─ Truyền dữ liệu: intent.putExtra("weather_data", currentWeatherData)
└─ startActivity(intent) → Chuyển sang OutfitSuggestionActivity
```

**Code trong MainActivity:**
```java
private void openOutfitSuggestionActivity() {
    if (currentWeatherData == null) {
        Toast.makeText(this, "Weather data not available yet", Toast.LENGTH_SHORT).show();
        return;
    }
    Intent intent = new Intent(this, OutfitSuggestionActivity.class);
    intent.putExtra("weather_data", currentWeatherData);
    startActivity(intent);
}
```

---

### **BƯỚC 2: Khởi Tạo UI và Nhận Dữ Liệu** 🎨
```
OutfitSuggestionActivity.java - onCreate()
├─ setContentView(R.layout.activity_outfit_suggestion)
├─ Khởi tạo các View:
│  ├─ RecyclerView (rvOutfitSuggestions) - Hiển thị danh sách gợi ý
│  ├─ ProgressBar - Hiển thị khi đang load
│  ├─ TextView (tvWeatherInfo) - Thông tin thời tiết
│  └─ ImageView (ivWeatherIcon) - Icon thời tiết
├─ Nhận dữ liệu từ Intent:
│  └─ weatherData = getIntent().getSerializableExtra("weather_data")
├─ Setup RecyclerView:
│  ├─ LinearLayoutManager
│  └─ OutfitSuggestionAdapter (khởi tạo với list rỗng)
├─ displayWeatherInfo() - Hiển thị thông tin thời tiết
└─ fetchOutfitSuggestions(adapter) - Gọi AI
```

**displayWeatherInfo():**
```java
- Lấy: temp, condition, cityName từ weatherData
- Hiển thị: "Hanoi - 25°C, partly cloudy"
- Set icon thời tiết tương ứng (getWeatherIconResource)
```

---

### **BƯỚC 3: Gọi Service để Lấy Gợi Ý AI** 🤖
```
OutfitSuggestionActivity - fetchOutfitSuggestions()
├─ Hiển thị ProgressBar, ẩn content
└─ outfitService.getOutfitSuggestions(weatherData, callback)
    │
    ↓ Chuyển sang OutfitSuggestionService.java
```

---

### **BƯỚC 4: Service Xử Lý Logic AI** 💡
```
OutfitSuggestionService.java - getOutfitSuggestions()
├─ Kiểm tra API key:
│  ├─ Nếu không có API key → Trả về gợi ý mặc định (offline)
│  └─ Nếu có API key → Tiếp tục gọi AI
├─ Tạo background thread mới (new Thread)
└─ Gọi: callGeminiAPI(weatherData)
```

---

### **BƯỚC 5: Tạo Prompt cho AI** 📝
```
OutfitSuggestionService - createPrompt()
├─ Lấy thông tin từ weatherData:
│  ├─ temp (nhiệt độ)
│  ├─ condition (Clear/Rain/Snow...)
│  ├─ description (partly cloudy, heavy rain...)
│  ├─ windSpeed (tốc độ gió)
│  └─ humidity (độ ẩm)
└─ Tạo prompt yêu cầu AI trả về JSON format:
    {
      "category": "Upper Body",
      "suggestion": "Light jacket",
      "reasoning": "Mild temperature, perfect for layers",
      "emoji": "🧥"
    }
```

**Ví dụ Prompt:**
```
Return ONLY this JSON array (EXACTLY 5 items):
[
  {"category":"Upper Body","suggestion":"","reasoning":"","emoji":"🧥"},
  {"category":"Lower Body","suggestion":"","reasoning":"","emoji":"👖"},
  {"category":"Footwear","suggestion":"","reasoning":"","emoji":"👟"},
  {"category":"Accessories","suggestion":"","reasoning":"","emoji":"🕶️"},
  {"category":"Extra Tips","suggestion":"","reasoning":"","emoji":"✨"}
]

Context: T=25°C; Clear(partly cloudy); Wind=3.5 m/s; Humidity=70%.
```

---

### **BƯỚC 6: Gọi Gemini API** 🌐
```
OutfitSuggestionService - callGeminiAPIWithConfig()
├─ Tạo JSON request:
│  ├─ contents: [{ role: "user", parts: [{ text: prompt }] }]
│  └─ generationConfig:
│      ├─ maxOutputTokens: 2048 (tối đa độ dài response)
│      ├─ temperature: 0.0 (output nhất quán)
│      └─ candidateCount: 1
├─ Tạo HTTP POST request với OkHttp:
│  ├─ URL: https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent
│  ├─ Header: Content-Type: application/json
│  └─ Body: JSON request
├─ Timeout settings:
│  ├─ connectTimeout: 20s
│  ├─ readTimeout: 45s (LLM cần thời gian xử lý)
│  └─ callTimeout: 60s
└─ Execute request → Nhận response
```

---

### **BƯỚC 7: Parse Response từ AI** 🔍
```
OutfitSuggestionService - parseGeminiResponse()
├─ Kiểm tra response có thành công không:
│  ├─ Check candidates[] có tồn tại
│  └─ Check finishReason (STOP = OK, MAX_TOKENS = retry)
├─ Lấy text từ response:
│  └─ candidates[0].content.parts[0].text
├─ Extract JSON array từ text:
│  ├─ Tìm vị trí '[' đầu tiên
│  ├─ Tìm vị trí ']' cuối cùng
│  └─ Substring để lấy JSON array
├─ Parse từng item trong array:
│  └─ Tạo OutfitSuggestion object:
│      ├─ category (e.g., "Upper Body")
│      ├─ suggestion (e.g., "Light jacket")
│      ├─ reasoning (e.g., "Perfect for mild weather")
│      └─ emoji (e.g., "🧥")
└─ Return List<OutfitSuggestion>
```

**Xử lý lỗi:**
- Nếu `finishReason = MAX_TOKENS` ��� Retry với maxOutputTokens tăng lên
- Nếu parse lỗi → Throw exception
- Nếu API lỗi → Fallback về gợi ý mặc định (offline)

---

### **BƯỚC 8: Trả Kết Quả về Activity** ✅
```
OutfitSuggestionService → callback
├─ Nếu thành công:
│  └─ callback.onSuccess(suggestions)
└─ Nếu lỗi:
   └─ callback.onError(errorMessage)
```

```
OutfitSuggestionActivity - callback
├─ onSuccess(suggestions):
│  ├─ runOnUiThread() (vì callback chạy trên background thread)
│  ├─ Ẩn ProgressBar
│  ├─ Hiện content
│  └─ adapter.updateSuggestions(suggestions)
│      └─ Adapter tự động cập nhật RecyclerView
└─ onError(error):
   ├─ Ẩn ProgressBar
   ├─ Hiện content
   └─ Toast.makeText(error) - Hiển thị lỗi
```

---

### **BƯỚC 9: Adapter Hiển Thị Dữ Liệu** 📱
```
OutfitSuggestionAdapter
├─ updateSuggestions(newSuggestions):
│  ├─ this.suggestions = newSuggestions
│  └─ notifyDataSetChanged() → RecyclerView refresh
├─ onCreateViewHolder():
│  └─ Inflate layout: item_outfit_suggestion.xml
└─ onBindViewHolder():
   └─ ViewHolder.bind(suggestion):
      ├─ tvEmoji.setText(suggestion.getEmoji()) → 🧥
      ├─ tvCategory.setText(suggestion.getCategory()) → "Upper Body"
      ├─ tvSuggestion.setText(suggestion.getSuggestion()) → "Light jacket"
      └─ tvReasoning.setText(suggestion.getReasoning()) → "Perfect for mild weather"
```

---

## 📊 FLOW DIAGRAM (Text)

```
┌─────────────────┐
│  MainActivity   │
│                 │
│ [Outfit Button] │
└────────┬────────┘
         │ Intent + weatherData
         ↓
┌────────────────────────────┐
│ OutfitSuggestionActivity   │
│                            │
│ 1. onCreate()              │
│ 2. displayWeatherInfo()    │
│ 3. fetchOutfitSuggestions()│
└────────┬───────────────────┘
         │ weatherData + callback
         ↓
┌────────────────────────────────────┐
│ OutfitSuggestionService            │
│                                    │
│ 1. getOutfitSuggestions()          │
│    ├─ Check API key                │
│    └─ new Thread { ... }           │
│                                    │
│ 2. createPrompt(weatherData)       │
│    └─ Build AI prompt              │
│                                    │
│ 3. callGeminiAPI()                 │
│    ├─ Create JSON request          │
│    ├─ HTTP POST with OkHttp        │
│    └─ Execute & get response       │
│                                    │
│ 4. parseGeminiResponse()           │
│    ├─ Extract JSON array           │
│    ├─ Parse each item              │
│    └─ Create OutfitSuggestion list │
└────────┬───────────────────────────┘
         │ callback.onSuccess(suggestions)
         ↓
┌────────────────────────────┐
│ OutfitSuggestionActivity   │
│                            │
│ runOnUiThread {            │
│   adapter.updateSuggestions│
│ }                          │
└────────┬───────────────────┘
         │ notifyDataSetChanged()
         ↓
┌────────────────────────────┐
│ OutfitSuggestionAdapter    │
│                            │
│ onBindViewHolder {         │
│   bind(suggestion)         │
│ }                          │
└────────┬───────────────────┘
         │
         ↓
┌────────────────────────────┐
│ RecyclerView (UI)          │
│                            │
│ ┌──────────────────────┐   │
│ │ 🧥 Upper Body        │   │
│ │ Light jacket         │   │
│ │ Perfect for mild...  │   │
│ └──────────────────────┘   │
│ ┌──────────────────────┐   │
│ │ 👖 Lower Body        │   │
│ │ Jeans                │   │
│ │ Comfortable and...   │   │
│ └──────────────────────┘   │
│ ...                        │
└────────────────────────────┘
```

---

## 🔐 DATA MODEL

### OutfitSuggestion.java
```java
public class OutfitSuggestion implements Serializable {
    private String category;    // "Upper Body", "Lower Body", "Footwear"...
    private String suggestion;  // "Light jacket", "Jeans", "Sneakers"...
    private String reasoning;   // "Perfect for mild weather"
    private String emoji;       // "🧥", "👖", "👟"...
    
    // Constructor + Getters
}
```

---

## 🎯 CÁC TÌNH HUỐNG XỬ LÝ

### ✅ **Trường hợp THÀNH CÔNG:**
```
User click → Intent → Activity → Service → AI API → Parse → Callback → UI Update → Hiển thị gợi ý
```

### ⚠️ **Trường hợp KHÔNG CÓ API KEY:**
```
Service check API key → Trả về gợi ý mặc định (offline) dựa trên logic if-else
```

### ❌ **Trường hợp LỖI API:**
```
API call failed → Fallback → Gợi ý mặc định → Hiển thị cho user
```

### 🔄 **Trường hợp MAX_TOKENS:**
```
Response bị cắt → Phát hiện finishReason = "MAX_TOKENS" → Retry với maxOutputTokens tăng lên → Success
```

### 🌐 **Trường hợp TIMEOUT:**
```
Request timeout → Retry 1 lần (max 2 attempts) → Nếu vẫn fail → Fallback gợi ý mặc định
```

---

## 🧩 CALLBACK INTERFACE

```java
public interface OutfitSuggestionCallback {
    void onSuccess(List<OutfitSuggestion> suggestions);
    void onError(String error);
}
```

**Sử dụng:**
```java
outfitService.getOutfitSuggestions(weatherData, new OutfitSuggestionCallback() {
    @Override
    public void onSuccess(List<OutfitSuggestion> suggestions) {
        // Cập nhật UI với danh sách gợi ý
    }
    
    @Override
    public void onError(String error) {
        // Hiển thị lỗi cho user
    }
});
```

---

## 📦 DEPENDENCIES

### OkHttp (HTTP Client)
```gradle
implementation 'com.squareup.okhttp3:okhttp:4.x.x'
implementation 'com.squareup.okhttp3:logging-interceptor:4.x.x'
```

### JSON Parsing
```java
import org.json.JSONArray;
import org.json.JSONObject;
```

---

## 🚀 API CONFIGURATION

**Gemini API:**
- Model: `gemini-2.5-flash`
- Endpoint: `https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent`
- Method: `POST`
- API Key: Trong code (nên chuyển sang BuildConfig hoặc local.properties)

**Timeout Settings:**
```java
connectTimeout: 20s
readTimeout: 45s  (LLM cần thời gian xử lý)
writeTimeout: 30s
callTimeout: 60s  (Timeout tổng)
```

---

## 💡 LOGIC GỢI Ý MẶC ĐỊNH (Offline)

Khi không có API key hoặc API lỗi, service sẽ dùng logic dựa trên nhiệt độ:

```
temp < 10°C  → Heavy jacket, Warm pants, Boots
temp < 20°C  → Light jacket, Jeans, Sneakers
temp < 28°C  → T-shirt, Casual pants, Comfortable shoes
temp >= 28°C → Tank top, Shorts, Sandals

condition = "rain" → Thêm: Umbrella, Waterproof jacket
windSpeed > 5 m/s → Thêm: Windbreaker
```

---

## 🎨 UI COMPONENTS

### activity_outfit_suggestion.xml
- **Header:** tvWeatherInfo + ivWeatherIcon
- **Content:** RecyclerView (rvOutfitSuggestions)
- **Loading:** ProgressBar
- **Navigation:** btnBack (ImageView)

### item_outfit_suggestion.xml
- **tvEmoji:** Emoji icon (🧥, 👖, 👟...)
- **tvCategory:** Category name (Upper Body, Lower Body...)
- **tvSuggestion:** Suggestion text (Light jacket, Jeans...)
- **tvReasoning:** Reasoning text (Perfect for mild weather...)

---

## 🔧 ĐIỂM CẦN CẢI THIỆN

1. **Security:** Chuyển API key ra BuildConfig hoặc local.properties
2. **Caching:** Cache gợi ý để tránh gọi API lại khi điều kiện tương tự
3. **Error handling:** Hiển thị error message chi tiết hơn cho user
4. **Testing:** Thêm unit tests cho parsing logic
5. **Localization:** Support đa ngôn ngữ (hiện tại chỉ tiếng Anh)

---

## 📝 TÓM TẮT LUỒNG CHÍNH

1. **MainActivity** → User click button → Intent + weatherData
2. **OutfitSuggestionActivity** → onCreate() → Hiển thị weather info → Gọi service
3. **OutfitSuggestionService** → Background thread → Tạo prompt → Gọi Gemini API
4. **Gemini API** → Phân tích thời tiết → Trả về JSON gợi ý
5. **Service** → Parse JSON → Tạo List<OutfitSuggestion> → Callback
6. **Activity** → Nhận callback → Update adapter → RecyclerView refresh
7. **RecyclerView** → Hiển thị danh sách gợi ý cho user

**Thời gian xử lý:** ~2-5 giây (tùy network và AI response time)

---

## 🎯 KẾT LUẬN

Chức năng AI Outfit Suggestion là một **async workflow** hoàn chỉnh với:
- ✅ Separation of concerns (Model-View-Service)
- ✅ Background processing (Thread)
- ✅ Callback pattern (async communication)
- ✅ Error handling & fallback
- ✅ UI thread safety (runOnUiThread)
- ✅ RecyclerView pattern (Adapter + ViewHolder)

Đây là một ví dụ tốt về cách tích hợp AI API vào Android app!

