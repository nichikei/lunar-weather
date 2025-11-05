package com.example.weatherapp.utils;

import android.content.Context;
import android.util.Log;

import com.example.weatherapp.data.models.OutfitSuggestion;
import com.example.weatherapp.data.responses.WeatherResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OutfitSuggestionService {
    private static final String TAG = "OutfitSuggestionService";

    // Google Gemini API - MIỄN PHÍ 100%!
    // Lấy API key tại: https://makersuite.google.com/app/apikey
    private static final String GEMINI_API_KEY = "AIzaSyAPtCim4ke9C8SwsY2bXszsQotGfxE-XH4";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    private final Context context;
    private final OkHttpClient client;

    public OutfitSuggestionService(Context context) {
        this.context = context;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)      // ≥ 45s cho LLM
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)      // Timeout tổng
                .retryOnConnectionFailure(true)
                .build();
    }

    public interface OutfitSuggestionCallback {
        void onSuccess(List<OutfitSuggestion> suggestions);
        void onError(String error);
    }

    public void getOutfitSuggestions(WeatherResponse weatherData, OutfitSuggestionCallback callback) {
        // Kiểm tra API key - KHÔNG dùng API nếu key là placeholder
        if (GEMINI_API_KEY.isEmpty() || GEMINI_API_KEY.equals("YOUR_GEMINI_API_KEY_HERE")) {
            Log.d(TAG, "Using default offline suggestions - No API key");
            callback.onSuccess(getDefaultOutfitSuggestions(weatherData));
            return;
        }

        // Log để biết đang sử dụng Gemini API
        Log.d(TAG, "=== USING GEMINI API ===");
        Log.d(TAG, "Model: gemini-2.5-flash");

        // Gọi Gemini API trong background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Calling Gemini API...");
                List<OutfitSuggestion> suggestions = callGeminiAPI(weatherData);

                if (suggestions != null && !suggestions.isEmpty()) {
                    Log.d(TAG, "✅ Gemini API SUCCESS - Got " + suggestions.size() + " suggestions");
                    callback.onSuccess(suggestions);
                } else {
                    Log.w(TAG, "⚠️ Gemini API returned empty - Using default");
                    callback.onSuccess(getDefaultOutfitSuggestions(weatherData));
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Gemini API ERROR: " + e.getMessage());
                // Fallback to default suggestions
                callback.onSuccess(getDefaultOutfitSuggestions(weatherData));
            }
        }).start();
    }

    private List<OutfitSuggestion> callGeminiAPI(WeatherResponse weatherData) throws Exception {
        return callGeminiAPIWithConfig(weatherData, 2048, false);
    }

    private List<OutfitSuggestion> callGeminiAPIWithConfig(WeatherResponse weatherData, int maxTokens, boolean isRetry) throws Exception {
        String prompt = createPrompt(weatherData);

        // Payload tối giản (đúng cú pháp /v1)
        JSONObject requestJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();

        part.put("text", prompt);
        parts.put(part);
        content.put("role", "user");
        content.put("parts", parts);
        contents.put(content);
        requestJson.put("contents", contents);

        // generationConfig - tối ưu để tránh MAX_TOKENS
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("maxOutputTokens", maxTokens);
        generationConfig.put("temperature", 0.0);  // Giảm độ sáng tạo, output nhất quán
        generationConfig.put("candidateCount", 1);
        generationConfig.put("topP", 0.9);
        generationConfig.put("topK", 40);
        requestJson.put("generationConfig", generationConfig);

        Log.d(TAG, "Config: maxTokens=" + maxTokens + ", isRetry=" + isRetry);

        RequestBody body = RequestBody.create(
            requestJson.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + GEMINI_API_KEY)
                .post(body)
                .build();

        // Retry logic cho timeout
        int maxRetries = 1;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "✅ Gemini response received (" + responseBody.length() + " bytes)");
                    return parseGeminiResponse(responseBody, weatherData, maxTokens, isRetry);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e(TAG, "❌ API failed: " + response.code() + " - " + errorBody);
                    return null;
                }
            } catch (SocketTimeoutException e) {
                if (attempt < maxRetries) {
                    Log.w(TAG, "⏱️ Timeout, retrying in 500ms... (attempt " + (attempt + 1) + ")");
                    Thread.sleep(500);
                } else {
                    Log.e(TAG, "❌ Timeout after " + (attempt + 1) + " attempts");
                    throw e;
                }
            }
        }
        return null;
    }

    private String createPrompt(WeatherResponse weatherData) {
        double temp = weatherData.getMain().getTemp();
        String condition = weatherData.getWeather().get(0).getMain();
        String description = weatherData.getWeather().get(0).getDescription();
        double windSpeed = weatherData.getWind().getSpeed();
        int humidity = weatherData.getMain().getHumidity();

        // Prompt CỰC NGẮN + bó cứng độ dài từng field để tránh MAX_TOKENS
        return "Return ONLY this JSON array (EXACTLY 5 items). Keep each \"suggestion\" ≤ 4 words, \"reasoning\" ≤ 10 words.\n\n" +
                "[\n" +
                "  {\"category\":\"Upper Body\",\"suggestion\":\"\",\"reasoning\":\"\",\"emoji\":\"🧥\"},\n" +
                "  {\"category\":\"Lower Body\",\"suggestion\":\"\",\"reasoning\":\"\",\"emoji\":\"👖\"},\n" +
                "  {\"category\":\"Footwear\",\"suggestion\":\"\",\"reasoning\":\"\",\"emoji\":\"👟\"},\n" +
                "  {\"category\":\"Accessories\",\"suggestion\":\"\",\"reasoning\":\"\",\"emoji\":\"🕶️\"},\n" +
                "  {\"category\":\"Extra Tips\",\"suggestion\":\"\",\"reasoning\":\"\",\"emoji\":\"✨\"}\n" +
                "]\n\n" +
                "Context: T=" + Math.round(temp) + "°C; " + condition + "(" + description + "); " +
                "Wind=" + String.format("%.1f", windSpeed) + " m/s; Humidity=" + humidity + "%.\n" +
                "Output must be valid JSON array only.";
    }

    private List<OutfitSuggestion> parseGeminiResponse(String responseBody, WeatherResponse weatherData,
                                                        int currentMaxTokens, boolean isRetry) throws Exception {
        List<OutfitSuggestion> suggestions = new ArrayList<>();
        try {
            Log.d(TAG, "📝 Parsing Gemini response...");

            JSONObject res = new JSONObject(responseBody);

            // Parser chống "rỗng" & chống MAX_TOKENS
            JSONArray candidates = res.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new IOException("No candidates in response");
            }

            JSONObject c0 = candidates.getJSONObject(0);
            String finish = c0.optString("finishReason", "");
            Log.d(TAG, "Finish reason: " + (finish.isEmpty() ? "STOP" : finish));

            JSONObject content = c0.optJSONObject("content");
            JSONArray parts = (content != null) ? content.optJSONArray("parts") : null;

            // Nếu model chưa trả text (hoặc bị cắt do MAX_TOKENS) → RETRY 1 LẦN
            if (parts == null || parts.length() == 0 || "MAX_TOKENS".equals(finish)) {
                if (!isRetry && "MAX_TOKENS".equals(finish)) {
                    Log.w(TAG, "⚠️ MAX_TOKENS reached (" + currentMaxTokens + " tokens), retrying with " + (currentMaxTokens + 512) + " tokens...");
                    // Retry 1 lần với maxOutputTokens tăng thêm 512
                    return callGeminiAPIWithConfig(weatherData, currentMaxTokens + 512, true);
                } else {
                    throw new IOException("Empty content or MAX_TOKENS after retry");
                }
            }

            String text = parts.getJSONObject(0).optString("text", "");
            if (text.isEmpty()) {
                throw new IOException("Response text is empty");
            }

            Log.d(TAG, "Extracted text (" + text.length() + " chars): " +
                  text.substring(0, Math.min(100, text.length())) + "...");

            // Extract JSON array - tìm first '[' đến last ']'
            int startIndex = text.indexOf("[");
            int endIndex = text.lastIndexOf("]") + 1;

            if (startIndex >= 0 && endIndex > startIndex) {
                String jsonStr = text.substring(startIndex, endIndex);

                // Parse JSON array trực tiếp
                JSONArray outfits = new JSONArray(jsonStr);
                Log.d(TAG, "📦 JSON array has " + outfits.length() + " items");

                for (int i = 0; i < outfits.length(); i++) {
                    JSONObject obj = outfits.getJSONObject(i);

                    String category = obj.optString("category", "Unknown");
                    String suggestion = obj.optString("suggestion", "");
                    String reasoning = obj.optString("reasoning", "");
                    String emoji = obj.optString("emoji", "👔");

                    if (!suggestion.isEmpty()) {
                        suggestions.add(new OutfitSuggestion(
                                category,
                                suggestion,
                                reasoning,
                                emoji
                        ));
                        Log.d(TAG, "  ✓ " + (i+1) + ". " + category + ": " + suggestion);
                    }
                }

                Log.d(TAG, "✅ Successfully parsed " + suggestions.size() + " suggestions");
            } else {
                throw new IOException("Could not find JSON array in response text");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse error: " + e.getMessage());
            throw new IOException("Failed to parse Gemini response: " + e.getMessage());
        }
        return suggestions;
    }

    private List<OutfitSuggestion> getDefaultOutfitSuggestions(WeatherResponse weatherData) {
        List<OutfitSuggestion> suggestions = new ArrayList<>();
        double temp = weatherData.getMain().getTemp();
        String condition = weatherData.getWeather().get(0).getMain().toLowerCase();
        double windSpeed = weatherData.getWind().getSpeed();

        // Upper Body
        if (temp < 10) {
            suggestions.add(new OutfitSuggestion(
                    "Upper Body",
                    "Heavy jacket or winter coat",
                    "It's cold outside, you need warm layers",
                    "🧥"
            ));
        } else if (temp < 20) {
            suggestions.add(new OutfitSuggestion(
                    "Upper Body",
                    "Light jacket or sweater",
                    "Mild temperature, a light layer is perfect",
                    "🧥"
            ));
        } else if (temp < 28) {
            suggestions.add(new OutfitSuggestion(
                    "Upper Body",
                    "T-shirt or casual shirt",
                    "Comfortable temperature for light clothing",
                    "👕"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Upper Body",
                    "Breathable tank top or light shirt",
                    "It's hot, stay cool with minimal clothing",
                    "👕"
            ));
        }

        // Lower Body
        if (temp < 15) {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Jeans or warm pants",
                    "Keep your legs warm in cooler weather",
                    "👖"
            ));
        } else if (temp < 25) {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Casual pants or jeans",
                    "Comfortable for moderate temperatures",
                    "👖"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Shorts or light skirt",
                    "Stay cool in warm weather",
                    "🩳"
            ));
        }

        // Footwear
        if (condition.contains("rain") || condition.contains("drizzle")) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Waterproof boots or rain shoes",
                    "Keep your feet dry in wet conditions",
                    "👢"
            ));
        } else if (temp < 10) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Boots or closed-toe shoes",
                    "Warm footwear for cold weather",
                    "👟"
            ));
        } else if (temp > 25) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Sandals or breathable sneakers",
                    "Keep your feet cool and comfortable",
                    "👟"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Casual sneakers or shoes",
                    "Versatile footwear for pleasant weather",
                    "👟"
            ));
        }

        // Accessories
        if (condition.contains("rain")) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Umbrella and waterproof bag",
                    "Essential protection from rain",
                    "☂️"
            ));
        } else if (condition.contains("clear") || condition.contains("sun")) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Sunglasses and hat",
                    "Protect yourself from the sun",
                    "🕶️"
            ));
        } else if (temp < 10) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Scarf and gloves",
                    "Extra warmth for cold weather",
                    "🧣"
            ));
        } else if (windSpeed > 5) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Light scarf or windbreaker",
                    "Protect against wind",
                    "🧣"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Sunglasses",
                    "Optional but recommended for comfort",
                    "🕶️"
            ));
        }

        // Extra Tips
        if (condition.contains("snow")) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Layer up with thermal wear",
                    "Multiple layers trap heat better in snow",
                    "❄️"
            ));
        } else if (temp > 30) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Stay hydrated, use sunscreen",
                    "Protect your skin in hot weather",
                    "☀️"
            ));
        } else if (weatherData.getMain().getHumidity() > 80) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Choose moisture-wicking fabrics",
                    "High humidity makes you sweat more",
                    "💧"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Dress in layers for flexibility",
                    "Easy to adjust to temperature changes",
                    "✨"
            ));
        }

        return suggestions;
    }
}
