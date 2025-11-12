# Testing Gemini AI Outfit Suggestions

## Cách test tính năng mới

### 1. Kiểm tra Gemini API Key
File: `OutfitSuggestionService.java`
```java
private static final String GEMINI_API_KEY = "AIzaSyAPtCim4ke9C8SwsY2bXszsQotGfxE-XH4";
```
✅ API key đã được cấu hình

### 2. Test Scenarios

#### Scenario 1: Cold Weather (Thời tiết lạnh)
**Input:**
- Temperature: 5°C
- Condition: Clear
- Wind: 8 m/s
- Humidity: 70%

**Expected AI Output:**
```json
[
  {
    "category": "Base Layer",
    "suggestion": "Thermal long-sleeve shirt",
    "reasoning": "Cold temperature needs warm foundation layer",
    "emoji": "👕",
    "priority": "ESSENTIAL",
    "fabricType": "Merino wool or fleece"
  },
  {
    "category": "Outer Layer",
    "suggestion": "Heavy jacket or winter coat",
    "reasoning": "Strong wind requires wind-resistant outer layer",
    "emoji": "🧥",
    "priority": "ESSENTIAL",
    "fabricType": "Wind-resistant insulated"
  },
  ...
]
```

#### Scenario 2: Rainy Weather (Thời tiết mưa)
**Input:**
- Temperature: 18°C
- Condition: Rain
- Wind: 5 m/s
- Humidity: 90%

**Expected AI Output:**
```json
[
  {
    "category": "Outer Layer",
    "suggestion": "Waterproof rain jacket",
    "reasoning": "Rain protection essential to stay dry",
    "emoji": "🧥",
    "priority": "ESSENTIAL",
    "fabricType": "Gore-Tex waterproof nylon"
  },
  {
    "category": "Footwear",
    "suggestion": "Waterproof boots",
    "reasoning": "Wet conditions require waterproof footwear",
    "emoji": "👢",
    "priority": "ESSENTIAL",
    "fabricType": "Waterproof rubber or leather"
  },
  {
    "category": "Accessories",
    "suggestion": "Umbrella and waterproof bag",
    "reasoning": "Essential rain protection for belongings",
    "emoji": "☂️",
    "priority": "ESSENTIAL",
    "fabricType": ""
  },
  ...
]
```

#### Scenario 3: Hot Weather (Thời tiết nóng)
**Input:**
- Temperature: 32°C
- Condition: Clear/Sunny
- Wind: 2 m/s
- Humidity: 60%

**Expected AI Output:**
```json
[
  {
    "category": "Base Layer",
    "suggestion": "Light breathable tank top",
    "reasoning": "Hot weather requires minimal breathable clothing",
    "emoji": "🎽",
    "priority": "RECOMMENDED",
    "fabricType": "Moisture-wicking synthetic"
  },
  {
    "category": "Head & Face",
    "suggestion": "Sun hat and sunglasses",
    "reasoning": "Strong sun requires UV and eye protection",
    "emoji": "🕶️",
    "priority": "ESSENTIAL",
    "fabricType": ""
  },
  {
    "category": "Accessories",
    "suggestion": "Sunscreen SPF 50+ water bottle",
    "reasoning": "Sun protection and hydration critical in heat",
    "emoji": "🧴",
    "priority": "ESSENTIAL",
    "fabricType": ""
  },
  {
    "category": "Extra Tips",
    "suggestion": "Stay in shade, hydrate every 20min",
    "reasoning": "Extreme heat requires frequent water intake prevention",
    "emoji": "💡",
    "priority": "ESSENTIAL",
    "fabricType": ""
  }
]
```

### 3. Kiểm tra Log Output

Khi chạy app, kiểm tra Logcat với tag `OutfitSuggestionService`:

```
D/OutfitSuggestionService: === USING GEMINI API ===
D/OutfitSuggestionService: Model: gemini-2.5-flash
D/OutfitSuggestionService: Calling Gemini API...
D/OutfitSuggestionService: Config: maxTokens=2048, isRetry=false
D/OutfitSuggestionService: ✅ Gemini response received (XXXX bytes)
D/OutfitSuggestionService: 📝 Parsing Gemini response...
D/OutfitSuggestionService: Finish reason: STOP
D/OutfitSuggestionService: Extracted text (XXX chars): [...]
D/OutfitSuggestionService: 📦 JSON array has 7 items
D/OutfitSuggestionService:   ✓ 1. [ESSENTIAL] Base Layer: Thermal long-sleeve shirt
D/OutfitSuggestionService:   ✓ 2. [ESSENTIAL] Outer Layer: Heavy winter coat
D/OutfitSuggestionService:   ✓ 3. [RECOMMENDED] Lower Body: Warm jeans
D/OutfitSuggestionService:   ✓ 4. [ESSENTIAL] Footwear: Insulated boots
D/OutfitSuggestionService:   ✓ 5. [ESSENTIAL] Head & Face: Winter beanie
D/OutfitSuggestionService:   ✓ 6. [ESSENTIAL] Accessories: Scarf and gloves
D/OutfitSuggestionService:   ✓ 7. [RECOMMENDED] Extra Tips: Layer up for warmth
D/OutfitSuggestionService: ✅ Successfully parsed 7 suggestions
D/OutfitSuggestionService: ✅ Gemini API SUCCESS - Got 7 suggestions
```

### 4. Kiểm tra UI Display

Trong RecyclerView, mỗi item nên hiển thị:

**Essential Items (Màu đỏ):**
```
⭐ Base Layer
🧥 Thermal long-sleeve shirt
Cold temperature needs warm foundation layer
🧵 Fabric: Merino wool or fleece
```

**Recommended Items (Màu xanh dương):**
```
✓ Lower Body
👖 Warm jeans or insulated pants
Keep legs warm in cold conditions
🧵 Fabric: Denim or thermal lined
```

**Optional Items (Màu xám):**
```
• Accessories
🎒 Backpack for extra layers
Weather may change during day
```

### 5. Fallback Test

Nếu Gemini API fails hoặc không khả dụng:

```
D/OutfitSuggestionService: ❌ Gemini API ERROR: [error message]
D/OutfitSuggestionService: Generated 7 default outfit suggestions
```

App sẽ tự động sử dụng default suggestions với logic thông minh đã nâng cấp.

### 6. Performance Metrics

- **API Call Time**: 2-5 giây (bình thường)
- **Timeout**: 45 giây read timeout
- **Retry Logic**: 1 retry nếu timeout
- **Fallback**: Instant với default suggestions

### 7. Error Handling

**Các trường hợp xử lý:**
1. ❌ API Key invalid → Fallback to default
2. ❌ Network timeout → Retry once → Fallback
3. ❌ Invalid JSON response → Fallback
4. ❌ MAX_TOKENS exceeded → Retry with +512 tokens
5. ⚠️ Invalid priority value → Use RECOMMENDED as default

## Debugging Tips

### Enable detailed logging
```java
HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
```

### Check Gemini response manually
```bash
# Use curl or Postman to test Gemini API directly
curl -X POST \
  "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "role": "user",
      "parts": [{"text": "Your prompt here"}]
    }],
    "generationConfig": {
      "maxOutputTokens": 2048,
      "temperature": 0.0
    }
  }'
```

### Logcat filters
```bash
# Filter for outfit suggestions
adb logcat | grep "OutfitSuggestion"

# Filter for Gemini API calls
adb logcat | grep "Gemini"
```

## Expected Improvements

✅ **With AI (Gemini):**
- Intelligent suggestions based on multiple weather factors
- Natural language reasoning
- Contextual fabric recommendations
- Dynamic priority assignment
- Cultural and regional awareness

✅ **Without AI (Fallback):**
- Still intelligent with 7 categories
- Priority levels based on weather severity
- Fabric type recommendations
- Temperature, wind, humidity, condition aware

## Success Criteria

✓ Gemini API returns 7 suggestions with all fields
✓ Priority levels correctly assigned (ESSENTIAL/RECOMMENDED/OPTIONAL)
✓ Fabric types included and relevant
✓ UI displays priority badges and colors
✓ Fallback works seamlessly when AI unavailable
✓ Response time under 5 seconds for 90% of requests
✓ No crashes or exceptions

