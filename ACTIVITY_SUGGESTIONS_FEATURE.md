# 🎯 Weather-based Activity Suggestions Feature

## 📋 Tổng quan

Tính năng **Activity Suggestions** sử dụng **Gemini AI** để gợi ý các hoạt động phù hợp dựa trên điều kiện thời tiết hiện tại. Tính năng tích hợp với **Calendar** để người dùng có thể thêm hoạt động trực tiếp vào lịch.

## ✨ Tính năng chính

### 1. AI-Powered Suggestions
- 🤖 Sử dụng **Gemini 2.0 Flash** để phân tích thời tiết
- 📊 Xem xét nhiều yếu tố: nhiệt độ, độ ẩm, UV index, tốc độ gió, chất lượng không khí
- 🎯 Đề xuất 6-8 hoạt động với độ phù hợp (suitability score)

### 2. Smart Activity Categories
- 🏞️ **Outdoor**: Picnic, cycling, hiking
- 🏠 **Indoor**: Movie, reading, gaming
- ⚽ **Sport**: Running, swimming, gym
- 🧘 **Relaxation**: Yoga, meditation, spa
- 👥 **Social**: Coffee shop, restaurant, events
- 🏃 **Exercise**: Jogging, walking, fitness
- 🍽️ **Food**: Dining out, cooking
- 🎬 **Entertainment**: Cinema, museums, concerts

### 3. Calendar Integration
- 📅 **Add to Calendar**: Một chạm để thêm hoạt động vào lịch
- ⏰ **Smart Timing**: Tự động gợi ý thời gian tốt nhất
- 🔔 **Reminders**: Nhắc nhở trước khi hoạt động bắt đầu

### 4. Weather-aware Recommendations
- ☀️ **Sunny & Warm (20-28°C)**: Outdoor activities, sports, picnics
- 🥵 **Hot (>28°C)**: Swimming, indoor AC activities, evening outings
- 🌧️ **Rainy**: Indoor activities, movies, reading, cafes
- ❄️ **Cold (<10°C)**: Indoor activities, warm beverages, cozy activities
- ☀️ **High UV (>7)**: Indoor activities or sun protection required
- 💨 **Windy**: Avoid outdoor sports, recommend sheltered activities
- 🌫️ **Poor AQI (>150)**: Indoor activities, avoid outdoor exercise

## 🎨 UI Components

### Main Screen
```
┌─────────────────────────────────────┐
│  ← Activity Suggestions        🔄   │
├─────────────────────────────────────┤
│  📍 Hanoi                           │
│  25°C                               │
│  Partly Cloudy                      │
│  💧 65% • 💨 3.2 m/s • ☀️ UV 5     │
├─────────────────────────────────────┤
│  🎯 Recommended Activities          │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ 🏃 Morning Run          95%  │  │
│  │ ⚽ SPORT                      │  │
│  │ Perfect weather for outdoor  │  │
│  │ running. Low UV and comfy... │  │
│  │ 💡 Ideal temp & clear skies  │  │
│  │ ⏰ Best time: Morning 6-10 AM │  │
│  │ [📅 Add to Calendar]         │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ 🧺 Picnic in the Park   90%  │  │
│  │ 🏞️ OUTDOOR                    │  │
│  │ ...                          │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Activity Card Components
- **Icon**: Large emoji representing activity
- **Title**: Activity name
- **Category Badge**: Category icon + name
- **Score Badge**: Suitability percentage (color-coded)
- **Description**: Brief description
- **Reason**: Why it's recommended
- **Best Time**: Optimal time to do activity
- **Calendar Button**: Add to calendar

### Score Color Coding
- 🟢 **80-100%**: Excellent (Green) - Highly recommended
- 🟡 **60-79%**: Good (Light Green) - Recommended
- 🟠 **40-59%**: Fair (Yellow) - Acceptable
- 🔴 **20-39%**: Poor (Orange) - Not ideal
- ⛔ **0-19%**: Not Recommended (Red) - Avoid

## 🔧 Technical Implementation

### Architecture
```
┌─────────────────────────────────────────┐
│         ActivitySuggestionsActivity      │
│  (UI Layer - View)                       │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    ActivitySuggestionService             │
│  (Service Layer - Business Logic)       │
│  - Calls Gemini AI API                  │
│  - Generates prompts                    │
│  - Parses responses                     │
│  - Fallback to default suggestions      │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         Gemini 2.0 Flash API             │
│  (AI Model - Google)                    │
└─────────────────────────────────────────┘
```

### Key Classes

#### 1. `ActivitySuggestion.java` (Domain Model)
```java
public class ActivitySuggestion {
    private String title;
    private String description;
    private String category;
    private String icon;
    private int suitabilityScore;
    private String reason;
    private String bestTime;
    private boolean calendarSyncable;
    // ... getters & setters
}
```

#### 2. `ActivitySuggestionService.java` (Service)
```java
public class ActivitySuggestionService {
    // Singleton pattern
    public static ActivitySuggestionService getInstance();
    
    // Main method
    public List<ActivitySuggestion> getActivitySuggestions(WeatherData weather);
    
    // Internal methods
    private List<ActivitySuggestion> callGeminiAPI(WeatherData weather);
    private String buildPrompt(WeatherData weather);
    private List<ActivitySuggestion> parseGeminiResponse(String response);
    private List<ActivitySuggestion> getDefaultSuggestions(WeatherData weather);
}
```

#### 3. `ActivitySuggestionsActivity.java` (UI)
```java
public class ActivitySuggestionsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize components
        // Setup RecyclerView
        // Load weather data
        // Load suggestions (async)
    }
    
    @Override
    public void onActivityClick(ActivitySuggestion suggestion) {
        // Show details
    }
    
    @Override
    public void onAddToCalendarClick(ActivitySuggestion suggestion) {
        // Add to device calendar
    }
}
```

#### 4. `CalendarHelper.java` (Utility)
```java
public class CalendarHelper {
    public static void addActivityToCalendar(Context context, ActivitySuggestion activity);
    public static boolean hasCalendarPermission(Context context);
    public static void openCalendar(Context context);
}
```

### Gemini AI Prompt Structure

**Input to Gemini:**
```
You are a weather-based activity recommendation AI.

Weather Conditions:
- Temperature: 25.0°C (Feels like: 27.0°C)
- Condition: Partly Cloudy
- Humidity: 65%
- Wind Speed: 3.2 m/s
- UV Index: 5
- AQI: 45

Instructions:
1. Analyze weather conditions
2. Consider safety and comfort
3. Suggest 6-8 activities
4. Provide mix of indoor/outdoor
5. Include different categories

Output Format (JSON):
[
  {
    "title": "Morning Run",
    "description": "Perfect weather for outdoor running...",
    "category": "sport",
    "icon": "🏃",
    "suitabilityScore": 95,
    "reason": "Ideal temperature and clear skies",
    "bestTime": "Morning 6-10 AM",
    "calendarSyncable": true
  }
]
```

**Output from Gemini:**
```json
[
  {
    "title": "Morning Run",
    "description": "Perfect weather for outdoor running. Low UV and comfortable temperature.",
    "category": "sport",
    "icon": "🏃",
    "suitabilityScore": 95,
    "reason": "Ideal temperature and clear skies",
    "bestTime": "Morning 6-10 AM",
    "calendarSyncable": true
  },
  {
    "title": "Picnic in the Park",
    "description": "Enjoy a relaxing picnic with family or friends.",
    "category": "outdoor",
    "icon": "🧺",
    "suitabilityScore": 90,
    "reason": "Pleasant temperature, perfect for outdoor activities",
    "bestTime": "Afternoon 11 AM - 3 PM",
    "calendarSyncable": true
  }
]
```

## 📱 User Flow

### Opening Activity Suggestions
1. User opens Weather App
2. Scrolls to "🎯 Activity Suggestions" button
3. Taps button
4. Activity Suggestions screen opens
5. Loading indicator shows
6. Gemini AI generates suggestions (3-5 seconds)
7. Suggestions display with animations

### Adding to Calendar
1. User sees activity card
2. Reviews details (title, description, score, reason, time)
3. Taps "📅 Add to Calendar" button
4. System calendar picker opens
5. Event details pre-filled:
   - Title: "🏃 Morning Run"
   - Description: Full details + reason + score
   - Start time: Based on "bestTime" (e.g., 8:00 AM)
   - Duration: 1 hour (default)
6. User can modify and save
7. Event added to calendar
8. Toast confirmation shown

## 🔐 Permissions Required

### Calendar Permission
```xml
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
```

### Permission Handling
- **Android 6.0+**: Runtime permission request
- **Fallback**: Uses Intent-based calendar insertion (no permission needed)
- **User-friendly**: Shows explanation if permission denied

## 🎯 API Integration

### Gemini AI Configuration
```java
private static final String GEMINI_API_KEY = "AIzaSy..."; // Your key
private static final String GEMINI_API_URL = 
    "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash-exp:generateContent";
```

### API Request
```java
POST {GEMINI_API_URL}?key={API_KEY}
Content-Type: application/json

{
  "contents": [{
    "parts": [{
      "text": "Your detailed prompt here..."
    }]
  }],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 2048
  }
}
```

### Error Handling
1. **API Failure**: Falls back to default suggestions
2. **Network Error**: Shows cached data or defaults
3. **Invalid Response**: Uses rule-based suggestions
4. **Empty Results**: Displays friendly message

## 🎨 Customization Options

### Activity Categories (Extendable)
```java
- "outdoor" → 🏞️
- "indoor" → 🏠
- "sport" → ⚽
- "relaxation" → 🧘
- "social" → 👥
- "exercise" → 🏃
- "food" → 🍽️
- "entertainment" → 🎬
```

### Score Thresholds (Configurable)
```java
public String getScoreColor() {
    if (score >= 80) return "#4CAF50"; // Excellent
    if (score >= 60) return "#8BC34A"; // Good
    if (score >= 40) return "#FFC107"; // Fair
    if (score >= 20) return "#FF9800"; // Poor
    return "#F44336"; // Not Recommended
}
```

## 📊 Future Enhancements

### Planned Features
1. **User Preferences**
   - Save favorite activity types
   - Block unwanted categories
   - Set preferred times

2. **Historical Tracking**
   - Track completed activities
   - Statistics and insights
   - Activity streaks

3. **Social Features**
   - Share activities with friends
   - Group activity planning
   - Activity challenges

4. **Smart Notifications**
   - "Perfect time for running!"
   - "Weather changed - update plan?"
   - Daily activity reminders

5. **Advanced AI**
   - Learn from user behavior
   - Personalized suggestions
   - Seasonal recommendations

6. **Integration**
   - Fitness apps (Strava, Google Fit)
   - Navigation (Google Maps)
   - Booking services

## 🐛 Troubleshooting

### Common Issues

**1. No Suggestions Showing**
- Check internet connection
- Verify Gemini API key
- Check API quota limits
- Review Logcat for errors

**2. Calendar Not Opening**
- Check calendar app installed
- Verify permission granted
- Try different calendar app

**3. Slow Loading**
- API call takes 3-5 seconds (normal)
- Check network speed
- Consider caching strategies

**4. Wrong Suggestions**
- Verify weather data accuracy
- Check API prompt logic
- Review AI response parsing

### Debug Mode
```java
// Enable detailed logging
Log.d("ActivitySuggestion", "Weather data: " + weatherData);
Log.d("ActivitySuggestion", "API Request: " + requestBody);
Log.d("ActivitySuggestion", "API Response: " + responseBody);
Log.d("ActivitySuggestion", "Parsed suggestions: " + suggestions.size());
```

## 📖 Code Examples

### Opening Activity Suggestions from MainActivity
```java
WeatherData currentWeather = viewModel.getCurrentWeatherData();
if (currentWeather != null) {
    Intent intent = new Intent(MainActivity.this, ActivitySuggestionsActivity.class);
    intent.putExtra("weather_data", new Gson().toJson(currentWeather));
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
}
```

### Custom Activity Suggestion
```java
ActivitySuggestion custom = new ActivitySuggestion(
    "Beach Volleyball",
    "Perfect beach weather for sports",
    "sport",
    "🏐",
    88,
    "Warm temperature and low wind",
    "Afternoon 2-5 PM",
    true
);
```

### Calendar Event Creation
```java
CalendarHelper.addActivityToCalendar(context, activitySuggestion);
```

## 🎉 Kết luận

Tính năng **Activity Suggestions** mang lại trải nghiệm thông minh và hữu ích cho người dùng, giúp họ tận dụng tốt nhất điều kiện thời tiết hiện tại. Với sự kết hợp của **Gemini AI** và **Calendar Integration**, ứng dụng trở nên proactive và personalized hơn.

### Key Benefits
- 🤖 **AI-Powered**: Smart, context-aware suggestions
- 📅 **Calendar Integration**: Seamless planning
- 🎯 **Weather-Aware**: Safety and comfort first
- 🎨 **Beautiful UI**: Modern, intuitive design
- ⚡ **Fast & Reliable**: With fallback mechanisms

---

**Version**: 1.0.0
**Date**: November 11, 2025
**Author**: Weather App Team
**AI Model**: Gemini 2.0 Flash (Google)
