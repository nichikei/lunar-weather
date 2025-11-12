package com.example.weatherapp.domain.services;

import android.util.Log;

import com.example.weatherapp.domain.model.ActivitySuggestion;
import com.example.weatherapp.domain.model.WeatherData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity Suggestion Service using Gemini AI
 * Generates weather-based activity recommendations
 */
public class ActivitySuggestionService {
    private static final String TAG = "ActivitySuggestionSvc";
    private static final String GEMINI_API_KEY = "AIzaSyAPtCim4ke9C8SwsY2bXszsQotGfxE-XH4";
    // Use same model as OutfitSuggestionService - gemini-2.5-flash with v1 API
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";
    private static final String MODEL_NAME = "gemini-2.5-flash";
    
    private static ActivitySuggestionService instance;
    private final OkHttpClient httpClient;

    private ActivitySuggestionService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized ActivitySuggestionService getInstance() {
        if (instance == null) {
            instance = new ActivitySuggestionService();
        }
        return instance;
    }

    /**
     * Get activity suggestions based on weather data
     */
    public List<ActivitySuggestion> getActivitySuggestions(WeatherData weatherData, int uvIndex, int aqi) {
        if (GEMINI_API_KEY.isEmpty() || GEMINI_API_KEY.equals("YOUR_GEMINI_API_KEY_HERE")) {
            Log.e(TAG, "❌ Gemini API Key not configured!");
            Log.e(TAG, "⚠️ USING DEFAULT SUGGESTIONS");
            return getDefaultSuggestions(weatherData, uvIndex);
        }

        Log.d(TAG, "╔════════════════════════════════════════════════╗");
        Log.d(TAG, "║  🤖 USING GEMINI AI FOR RECOMMENDATIONS      ║");
        Log.d(TAG, "╚════════════════════════════════════════════════╝");
        Log.d(TAG, "📍 City: " + weatherData.getCityName());
        Log.d(TAG, "🌡️  Temperature: " + weatherData.getTemperature() + "°C (Feels: " + weatherData.getFeelsLike() + "°C)");
        Log.d(TAG, "💧 Humidity: " + weatherData.getHumidity() + "%");
        Log.d(TAG, "💨 Wind: " + weatherData.getWindSpeed() + " m/s");
        Log.d(TAG, "☁️  Condition: " + weatherData.getWeatherDescription());
        Log.d(TAG, "☀️  UV Index: " + uvIndex);
        Log.d(TAG, "🌫️  AQI: " + aqi);
        Log.d(TAG, "🔧 Model: " + MODEL_NAME + " (v1 API - same as OutfitSuggestion)");
        Log.d(TAG, "════════════════════════════════════════════════");

        try {
            Log.d(TAG, "📡 Calling Gemini API...");
            long startTime = System.currentTimeMillis();
            
            List<ActivitySuggestion> suggestions = callGeminiAPI(weatherData, uvIndex, aqi);
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (suggestions != null && !suggestions.isEmpty()) {
                Log.d(TAG, "════════════════════════════════════════════════");
                Log.d(TAG, "✅ GEMINI API SUCCESS!");
                Log.d(TAG, "📊 Received: " + suggestions.size() + " AI-generated suggestions");
                Log.d(TAG, "⏱️  Response time: " + duration + "ms");
                Log.d(TAG, "🎯 Source: GEMINI AI (NOT DEFAULT)");
                Log.d(TAG, "════════════════════════════════════════════════");
                return suggestions;
            } else {
                Log.w(TAG, "════════════════════════════════════════════════");
                Log.w(TAG, "⚠️  Gemini API returned empty response");
                Log.w(TAG, "🔄 Falling back to DEFAULT SUGGESTIONS");
                Log.w(TAG, "════════════════════════════════════════════════");
                return getDefaultSuggestions(weatherData, uvIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "════════════════════════════════════════════════");
            Log.e(TAG, "❌ GEMINI API ERROR: " + e.getClass().getSimpleName());
            Log.e(TAG, "💬 Message: " + e.getMessage());
            Log.e(TAG, "🔄 Falling back to DEFAULT SUGGESTIONS");
            Log.e(TAG, "════════════════════════════════════════════════");
            e.printStackTrace();
            return getDefaultSuggestions(weatherData, uvIndex);
        }
    }

    /**
     * Call Gemini AI API
     */
    private List<ActivitySuggestion> callGeminiAPI(WeatherData weatherData, int uvIndex, int aqi) throws IOException, JSONException {
        String prompt = buildPrompt(weatherData, uvIndex, aqi);
        
        JSONObject requestJson = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject contentObj = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject partObj = new JSONObject();
        
        partObj.put("text", prompt);
        partsArray.put(partObj);
        contentObj.put("parts", partsArray);
        contentsArray.put(contentObj);
        requestJson.put("contents", contentsArray);

        // Generation config - increased maxOutputTokens to avoid MAX_TOKENS truncation
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 4096);  // Increased from 2048 to allow full responses
        generationConfig.put("topP", 0.9);
        generationConfig.put("topK", 40);
        requestJson.put("generationConfig", generationConfig);

        Log.d(TAG, "Request: " + requestJson.toString());

        RequestBody body = RequestBody.create(
                requestJson.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + GEMINI_API_KEY)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                if (response.body() != null) {
                    Log.e(TAG, "Error Body: " + response.body().string());
                }
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            Log.d(TAG, "Response: " + responseBody);

            return parseGeminiResponse(responseBody, weatherData);
        }
    }

    /**
     * Build prompt for Gemini AI
     */
    private String buildPrompt(WeatherData weatherData, int uvIndex, int aqi) {
        double temp = weatherData.getTemperature();
        double feelsLike = weatherData.getFeelsLike();
        int humidity = weatherData.getHumidity();
        double windSpeed = weatherData.getWindSpeed();
        String condition = weatherData.getWeatherDescription();
        // Use real UV and AQI from parameters

        return String.format(
            "You are a weather-based activity recommendation AI. Based on the current weather conditions, " +
            "suggest 6-8 suitable activities for today.\n\n" +
            
            "**Weather Conditions:**\n" +
            "- Temperature: %.1f°C (Feels like: %.1f°C)\n" +
            "- Condition: %s\n" +
            "- Humidity: %d%%\n" +
            "- Wind Speed: %.1f m/s\n" +
            "- UV Index: %d\n" +
            "- Air Quality Index (AQI): %d\n\n" +
            
            "**Instructions:**\n" +
            "1. Analyze the weather conditions carefully\n" +
            "2. Consider temperature, humidity, UV index, wind, and air quality\n" +
            "3. Suggest activities that are SAFE and COMFORTABLE for these conditions\n" +
            "4. Provide a mix of indoor and outdoor activities\n" +
            "5. Include different categories: sport, relaxation, social, entertainment\n" +
            "6. For each activity, provide:\n" +
            "   - Title (short, catchy)\n" +
            "   - Description (1-2 sentences)\n" +
            "   - Category (outdoor/indoor/sport/relaxation/social/exercise/food/entertainment)\n" +
            "   - Suitability Score (0-100)\n" +
            "   - Reason (why it's good for this weather)\n" +
            "   - Best Time (e.g., 'Morning 6-10 AM', 'Afternoon 2-6 PM', 'All day', 'Evening 6-9 PM')\n" +
            "   - Icon (single emoji)\n\n" +
            
            "**Important Guidelines:**\n" +
            "- High UV (>7): Recommend indoor activities or activities with sun protection\n" +
            "- High humidity (>80%%): Avoid intense outdoor activities\n" +
            "- Low temp (<10°C): Recommend warm indoor activities or proper clothing\n" +
            "- High temp (>35°C): Recommend cool indoor activities, swimming, or early morning/evening activities\n" +
            "- Poor AQI (>150): Prioritize indoor activities\n" +
            "- Good weather (20-28°C, low humidity): Encourage outdoor activities\n\n" +
            
            "**Output Format (JSON):**\n" +
            "Return ONLY a valid JSON array with this exact structure (no markdown, no extra text):\n" +
            "[\n" +
            "  {\n" +
            "    \"title\": \"Activity Name\",\n" +
            "    \"description\": \"Brief description\",\n" +
            "    \"category\": \"outdoor\",\n" +
            "    \"icon\": \"🏃\",\n" +
            "    \"suitabilityScore\": 85,\n" +
            "    \"reason\": \"Perfect temperature and low humidity\",\n" +
            "    \"bestTime\": \"Morning 6-10 AM\",\n" +
            "    \"calendarSyncable\": true\n" +
            "  }\n" +
            "]\n\n" +
            
            "Generate suggestions now:", 
            temp, feelsLike, condition, humidity, windSpeed, uvIndex, aqi
        );
    }

    /**
     * Parse Gemini API response
     */
    private List<ActivitySuggestion> parseGeminiResponse(String responseBody, WeatherData weatherData) {
        List<ActivitySuggestion> suggestions = new ArrayList<>();
        
        try {
            Log.d(TAG, "🔍 Parsing Gemini response...");
            Log.d(TAG, "Response length: " + responseBody.length() + " chars");
            
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            // Check for API errors
            if (jsonResponse.has("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                String errorMessage = error.optString("message", "Unknown error");
                Log.e(TAG, "❌ API returned error: " + errorMessage);
                return suggestions;
            }
            
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            
            // Check for MAX_TOKENS (response was truncated)
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                String finishReason = candidate.optString("finishReason", "STOP");
                
                if ("MAX_TOKENS".equals(finishReason)) {
                    Log.e(TAG, "⚠️ Response was truncated due to MAX_TOKENS!");
                    Log.e(TAG, "💡 Tip: Increase maxOutputTokens in generationConfig");
                    // Continue parsing partial response instead of failing
                }
            }
            Log.d(TAG, "Found " + candidates.length() + " candidates");
            
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                
                if (parts.length() > 0) {
                    String text = parts.getJSONObject(0).getString("text");
                    Log.d(TAG, "📝 Generated text preview: " + 
                        (text.length() > 200 ? text.substring(0, 200) + "..." : text));
                    
                    // Clean the text (remove markdown code blocks if present)
                    text = text.trim();
                    if (text.startsWith("```json")) {
                        Log.d(TAG, "Removing ```json wrapper");
                        text = text.substring(7);
                    }
                    if (text.startsWith("```")) {
                        Log.d(TAG, "Removing ``` wrapper");
                        text = text.substring(3);
                    }
                    if (text.endsWith("```")) {
                        text = text.substring(0, text.length() - 3);
                    }
                    text = text.trim();
                    
                    Log.d(TAG, "🧹 Cleaned text preview: " + 
                        (text.length() > 200 ? text.substring(0, 200) + "..." : text));
                    
                    // Handle truncated JSON - find last complete object
                    int lastCompleteObject = text.lastIndexOf("}");
                    if (lastCompleteObject > 0 && lastCompleteObject < text.length() - 1) {
                        // Response was truncated, try to salvage complete objects
                        String truncatedPart = text.substring(lastCompleteObject + 1).trim();
                        if (!truncatedPart.isEmpty() && !truncatedPart.equals("]")) {
                            Log.w(TAG, "⚠️ Truncated content detected: " + truncatedPart);
                            // Add closing bracket if missing
                            if (!text.trim().endsWith("]")) {
                                text = text.substring(0, lastCompleteObject + 1) + "\n]";
                                Log.d(TAG, "🔧 Fixed truncated JSON by adding closing bracket");
                            }
                        }
                    }
                    
                    // Parse JSON array
                    JSONArray activitiesArray = new JSONArray(text);
                    Log.d(TAG, "Found " + activitiesArray.length() + " activities in response");
                    
                    for (int i = 0; i < activitiesArray.length(); i++) {
                        JSONObject activityJson = activitiesArray.getJSONObject(i);
                        
                        ActivitySuggestion suggestion = new ActivitySuggestion();
                        suggestion.setTitle(activityJson.getString("title"));
                        suggestion.setDescription(activityJson.getString("description"));
                        suggestion.setCategory(activityJson.getString("category"));
                        suggestion.setIcon(activityJson.getString("icon"));
                        suggestion.setSuitabilityScore(activityJson.getInt("suitabilityScore"));
                        suggestion.setReason(activityJson.getString("reason"));
                        suggestion.setBestTime(activityJson.getString("bestTime"));
                        suggestion.setCalendarSyncable(activityJson.optBoolean("calendarSyncable", true));
                        suggestion.setWeatherCondition(weatherData.getWeatherDescription());
                        
                        suggestions.add(suggestion);
                        Log.d(TAG, "  ✓ Activity " + (i+1) + ": " + suggestion.getTitle() + 
                            " (score: " + suggestion.getSuitabilityScore() + ")");
                    }
                    
                    Log.d(TAG, "✅ Successfully parsed " + suggestions.size() + " AI-generated suggestions");
                } else {
                    Log.w(TAG, "⚠️ No parts found in content");
                }
            } else {
                Log.w(TAG, "⚠️ No candidates in response");
            }
        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON Parsing Error: " + e.getMessage());
            Log.e(TAG, "Response body: " + (responseBody.length() > 500 ? 
                responseBody.substring(0, 500) + "..." : responseBody));
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        Log.d(TAG, "Returning " + suggestions.size() + " suggestions from parseGeminiResponse");
        return suggestions;
    }

    /**
     * Get default suggestions (fallback)
     */
    private List<ActivitySuggestion> getDefaultSuggestions(WeatherData weatherData, int uvIndex) {
        List<ActivitySuggestion> suggestions = new ArrayList<>();
        double temp = weatherData.getTemperature();
        String condition = weatherData.getWeatherDescription().toLowerCase();
        // Use real UV index from parameter
        
        // Good weather (15-28°C, clear)
        if (temp >= 15 && temp <= 28 && (condition.contains("clear") || condition.contains("sun"))) {
            suggestions.add(new ActivitySuggestion(
                "Morning Run", 
                "Perfect weather for outdoor running. Low UV and comfortable temperature.",
                "sport", "🏃", 95, 
                "Ideal temperature and clear skies",
                "Morning 6-9 AM", true
            ));
            
            suggestions.add(new ActivitySuggestion(
                "Picnic in the Park", 
                "Enjoy a relaxing picnic with family or friends in perfect weather.",
                "outdoor", "🧺", 90, 
                "Pleasant temperature, perfect for outdoor activities",
                "Afternoon 11 AM - 3 PM", true
            ));
            
            suggestions.add(new ActivitySuggestion(
                "Cycling Adventure", 
                "Great day for a bike ride. Explore the city or countryside.",
                "sport", "🚴", 88, 
                "Cool breeze and comfortable temperature",
                "Morning or Evening", true
            ));
        }
        
        // Hot weather (>28°C)
        if (temp > 28) {
            suggestions.add(new ActivitySuggestion(
                "Swimming", 
                "Cool off at the pool or beach. Perfect activity for hot weather.",
                "sport", "🏊", 92, 
                "Hot weather, swimming helps cool down",
                "Afternoon 2-6 PM", true
            ));
            
            suggestions.add(new ActivitySuggestion(
                "Stay Indoors - AC", 
                "Visit a mall, cinema, or cafe with air conditioning.",
                "indoor", "❄️", 85, 
                "Temperature too high for outdoor activities",
                "Afternoon 12-4 PM", false
            ));
        }
        
        // High UV Index
        if (uvIndex > 7) {
            suggestions.add(new ActivitySuggestion(
                "Indoor Gym Workout", 
                "Avoid direct sunlight. Indoor exercise is safer.",
                "sport", "💪", 88, 
                "High UV index - stay indoors",
                "Any time", true
            ));
        }
        
        // Rainy weather
        if (condition.contains("rain")) {
            suggestions.add(new ActivitySuggestion(
                "Cozy Movie Marathon", 
                "Perfect rainy day for watching movies at home.",
                "entertainment", "🎬", 90, 
                "Rainy weather, best to stay indoors",
                "All day", false
            ));
            
            suggestions.add(new ActivitySuggestion(
                "Read a Book", 
                "Enjoy a good book with a cup of coffee or tea.",
                "relaxation", "📚", 88, 
                "Relaxing indoor activity for rainy days",
                "All day", false
            ));
        }
        
        // Always good activities
        suggestions.add(new ActivitySuggestion(
            "Yoga & Meditation", 
            "Indoor yoga session for physical and mental wellness.",
            "relaxation", "🧘", 82, 
            "Good for any weather condition",
            "Morning 7-9 AM or Evening 6-8 PM", true
        ));
        
        suggestions.add(new ActivitySuggestion(
            "Coffee Shop Visit", 
            "Meet friends at a cozy cafe. Socialize and relax.",
            "social", "☕", 80, 
            "Indoor social activity",
            "Afternoon 2-5 PM", true
        ));
        
        return suggestions;
    }
}
