package com.example.weatherapp.domain.services;

import android.content.Context;
import android.util.Log;

import com.example.weatherapp.domain.model.VoiceQuery;
import com.example.weatherapp.domain.model.VoiceResponse;
import com.example.weatherapp.domain.model.WeatherData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Voice Weather Assistant Service
 * Uses Gemini AI to understand natural language queries and generate spoken responses
 */
public class VoiceWeatherService {
    private static final String TAG = "VoiceWeatherService";
    
    private static final String GEMINI_API_KEY = "AIzaSyAPtCim4ke9C8SwsY2bXszsQotGfxE-XH4";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash-exp:generateContent?key=";
    
    private final Context context;
    private final OkHttpClient client;
    
    public VoiceWeatherService(Context context) {
        this.context = context;
        
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .callTimeout(45, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
    
    /**
     * Callback interface for async voice processing
     */
    public interface VoiceResponseCallback {
        void onSuccess(VoiceResponse response);
        void onError(String error);
    }
    
    /**
     * Parse user's voice input to determine query type
     */
    public VoiceQuery parseVoiceInput(String voiceText) {
        String lower = voiceText.toLowerCase().trim();
        
        // Temperature queries
        if (lower.contains("temperature") || lower.contains("how hot") || lower.contains("how cold") || 
            lower.contains("degrees") || lower.matches(".*what'?s the temp.*")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.TEMPERATURE);
        }
        
        // Rain queries
        if (lower.contains("rain") || lower.contains("raining") || lower.contains("precipitation")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.WILL_IT_RAIN);
        }
        
        // Umbrella queries
        if (lower.contains("umbrella") || lower.matches(".*should i bring.*")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.SHOULD_BRING_UMBRELLA);
        }
        
        // Outfit/clothing queries
        if (lower.contains("wear") || lower.contains("outfit") || lower.contains("cloth") || 
            lower.contains("dress")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.OUTFIT_SUGGESTION);
        }
        
        // UV queries
        if (lower.contains("uv") || lower.contains("sun index") || lower.contains("sunburn")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.UV_INDEX);
        }
        
        // Air quality queries
        if (lower.contains("air quality") || lower.contains("aqi") || lower.contains("pollution")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.AIR_QUALITY);
        }
        
        // Wind queries
        if (lower.contains("wind") || lower.contains("windy") || lower.contains("breeze")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.WIND_SPEED);
        }
        
        // Humidity queries
        if (lower.contains("humid") || lower.contains("moisture") || lower.contains("damp")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.HUMIDITY);
        }
        
        // Feels like queries
        if (lower.contains("feel") && (lower.contains("like") || lower.contains("outside"))) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.FEELS_LIKE);
        }
        
        // Sunrise/sunset queries
        if (lower.contains("sunrise") || lower.contains("sunset") || lower.contains("sun rise") || 
            lower.contains("sun set")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.SUNRISE_SUNSET);
        }
        
        // Forecast queries
        if (lower.contains("forecast") || lower.contains("tomorrow") || lower.contains("this week") ||
            lower.contains("next few days")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.FORECAST);
        }
        
        // Best time queries
        if (lower.contains("best time") || lower.contains("when should")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.BEST_TIME);
        }
        
        // Current weather (default for most queries)
        if (lower.contains("weather") || lower.contains("outside") || lower.matches(".*how'?s.*") ||
            lower.matches(".*what'?s.*")) {
            return new VoiceQuery(voiceText, VoiceQuery.QueryType.CURRENT_WEATHER);
        }
        
        // General fallback
        return new VoiceQuery(voiceText, VoiceQuery.QueryType.GENERAL);
    }
    
    /**
     * Generate voice response using weather data
     * First tries to generate quick local response, then enhances with Gemini AI if needed
     */
    public void generateVoiceResponse(VoiceQuery query, WeatherData weatherData, VoiceResponseCallback callback) {
        if (weatherData == null) {
            callback.onError("Weather data not available");
            return;
        }
        
        // For simple queries, generate instant response without AI
        VoiceResponse quickResponse = generateQuickResponse(query, weatherData);
        if (quickResponse != null) {
            callback.onSuccess(quickResponse);
            return;
        }
        
        // For complex queries, use Gemini AI in background thread
        new Thread(() -> {
            try {
                VoiceResponse aiResponse = generateAIResponse(query, weatherData);
                callback.onSuccess(aiResponse);
            } catch (Exception e) {
                Log.e(TAG, "AI generation failed, using fallback", e);
                // Fallback to basic response
                VoiceResponse fallback = generateFallbackResponse(query, weatherData);
                callback.onSuccess(fallback);
            }
        }).start();
    }
    
    /**
     * Generate quick local response for simple queries (no AI needed)
     */
    private VoiceResponse generateQuickResponse(VoiceQuery query, WeatherData weatherData) {
        double temp = weatherData.getTemperature();
        int humidity = weatherData.getHumidity();
        double windSpeed = weatherData.getWindSpeed();
        String condition = weatherData.getWeatherMain();
        
        switch (query.getQueryType()) {
            case TEMPERATURE:
                return new VoiceResponse(
                    String.format(Locale.US, "The current temperature is %.0f degrees Celsius.", temp),
                    String.format(Locale.US, "🌡️ Temperature: %.1f°C", temp)
                );
                
            case WILL_IT_RAIN:
                boolean isRaining = condition.toLowerCase().contains("rain");
                String rainResponse = isRaining ? 
                    "Yes, it's currently raining outside." : 
                    "No, it's not raining right now.";
                return new VoiceResponse(rainResponse);
                
            case SHOULD_BRING_UMBRELLA:
                boolean needUmbrella = condition.toLowerCase().contains("rain") || 
                                     condition.toLowerCase().contains("drizzle");
                String umbrellaResponse = needUmbrella ?
                    "Yes, you should bring an umbrella. It's raining outside." :
                    "No, you don't need an umbrella. The weather looks clear.";
                return new VoiceResponse(umbrellaResponse);
                
            case WIND_SPEED:
                return new VoiceResponse(
                    String.format(Locale.US, "The wind speed is %.1f meters per second.", windSpeed),
                    String.format(Locale.US, "💨 Wind: %.1f m/s", windSpeed)
                );
                
            case HUMIDITY:
                return new VoiceResponse(
                    String.format(Locale.US, "The humidity is %d percent.", humidity),
                    String.format(Locale.US, "💧 Humidity: %d%%", humidity)
                );
                
            case FEELS_LIKE:
                double feelsLike = weatherData.getFeelsLike();
                return new VoiceResponse(
                    String.format(Locale.US, "It feels like %.0f degrees outside.", feelsLike),
                    String.format(Locale.US, "🌡️ Feels like: %.1f°C", feelsLike)
                );
        }
        
        return null; // Need AI for this query
    }
    
    /**
     * Generate AI-enhanced response using Gemini
     */
    private VoiceResponse generateAIResponse(VoiceQuery query, WeatherData weatherData) throws Exception {
        String prompt = createVoicePrompt(query, weatherData);
        
        JSONObject requestJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestJson.put("contents", contents);
        
        // Generation config for concise voice responses
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 150); // Keep responses short for voice
        requestJson.put("generationConfig", generationConfig);
        
        RequestBody body = RequestBody.create(
            requestJson.toString(),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
                .url(GEMINI_API_URL + GEMINI_API_KEY)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API call failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            return parseAIResponse(responseBody);
        }
    }
    
    /**
     * Create optimized prompt for voice assistant
     */
    private String createVoicePrompt(VoiceQuery query, WeatherData weatherData) {
        double temp = weatherData.getTemperature();
        double feelsLike = weatherData.getFeelsLike();
        int humidity = weatherData.getHumidity();
        double windSpeed = weatherData.getWindSpeed();
        String condition = weatherData.getWeatherDescription();
        String city = weatherData.getCityName();
        
        return "You are a helpful voice weather assistant. Answer the user's question naturally and concisely in 1-2 sentences.\n\n" +
                "Current Weather in " + city + ":\n" +
                "- Temperature: " + String.format("%.1f", temp) + "°C (feels like " + String.format("%.1f", feelsLike) + "°C)\n" +
                "- Condition: " + condition + "\n" +
                "- Humidity: " + humidity + "%\n" +
                "- Wind: " + String.format("%.1f", windSpeed) + " m/s\n\n" +
                "User asked: \"" + query.getOriginalText() + "\"\n\n" +
                "Give a friendly, conversational response as if speaking to the user. Keep it under 30 words.";
    }
    
    /**
     * Parse AI response from Gemini API
     */
    private VoiceResponse parseAIResponse(String responseBody) throws Exception {
        JSONObject json = new JSONObject(responseBody);
        JSONArray candidates = json.optJSONArray("candidates");
        
        if (candidates == null || candidates.length() == 0) {
            throw new IOException("No response from AI");
        }
        
        JSONObject candidate = candidates.getJSONObject(0);
        JSONObject content = candidate.getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");
        String text = parts.getJSONObject(0).getString("text").trim();
        
        return new VoiceResponse(text);
    }
    
    /**
     * Fallback response if AI fails
     */
    private VoiceResponse generateFallbackResponse(VoiceQuery query, WeatherData weatherData) {
        double temp = weatherData.getTemperature();
        String condition = weatherData.getWeatherDescription();
        String city = weatherData.getCityName();
        
        String response = String.format(Locale.US, 
            "The weather in %s is %s with a temperature of %.0f degrees Celsius.",
            city, condition, temp);
        
        return new VoiceResponse(response);
    }
}
