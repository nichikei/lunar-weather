package com.example.weatherapp.domain.services;

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

    private double toC(double kelvin) { return kelvin - 273.15; }

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
        void onSuccess(List<OutfitSuggestion> suggestions, String source);
        void onError(String error);
    }

    public void getOutfitSuggestions(WeatherResponse weatherData, OutfitSuggestionCallback callback) {
        // Kiểm tra API key - KHÔNG dùng API nếu key là placeholder
        if (GEMINI_API_KEY.isEmpty() || GEMINI_API_KEY.equals("YOUR_GEMINI_API_KEY_HERE")) {
            Log.d(TAG, "Using default offline suggestions - No API key");
            callback.onSuccess(getDefaultOutfitSuggestions(weatherData), "OFFLINE");
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
                    callback.onSuccess(suggestions, "AI");
                } else {
                    Log.w(TAG, "⚠️ Gemini API returned empty - Using default");
                    callback.onSuccess(getDefaultOutfitSuggestions(weatherData), "OFFLINE");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Gemini API ERROR: " + e.getMessage());
                // Fallback to default suggestions
                callback.onSuccess(getDefaultOutfitSuggestions(weatherData), "OFFLINE");
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
        double temp = toC(weatherData.getMain().getTemp());
        String condition = weatherData.getWeather().get(0).getMain();
        String description = weatherData.getWeather().get(0).getDescription();
        double windSpeed = weatherData.getWind().getSpeed();
        int humidity = weatherData.getMain().getHumidity();
        double feelsLike = toC(weatherData.getMain().getFeelsLike());
        int clouds = weatherData.getClouds() != null ? weatherData.getClouds().getAll() : 0;

        // Enhanced prompt with priority levels and fabric recommendations for Gemini AI
        return "You are a fashion and weather expert. Return ONLY a JSON array with EXACTLY 7 outfit suggestions.\n\n" +
                "Each item must have: category, suggestion (≤6 words), reasoning (≤15 words), emoji, priority, fabricType\n\n" +
                "Priority levels:\n" +
                "- \"ESSENTIAL\": Critical/必須 for current weather (e.g., rain jacket in rain, winter coat in freezing)\n" +
                "- \"RECOMMENDED\": Strongly advised for comfort\n" +
                "- \"OPTIONAL\": Nice to have but not necessary\n\n" +
                "Categories & appropriate emojis:\n" +
                "1. Base Layer (👕/🎽) - inner clothing\n" +
                "2. Outer Layer (🧥/🧥) - jacket/coat\n" +
                "3. Lower Body (👖/🩳) - pants/shorts\n" +
                "4. Footwear (👟/👢/👡) - shoes/boots\n" +
                "5. Head & Face (🧢/🕶️/🎩) - hat/sunglasses/cap\n" +
                "6. Accessories (☂️/🧣/🎒) - umbrella/scarf/bag\n" +
                "7. Extra Tips (💡/⚡/✨) - health/safety advice\n\n" +
                "Weather Context:\n" +
                "- Temperature: " + String.format("%.1f", temp) + "°C (feels like " + String.format("%.1f", feelsLike) + "°C)\n" +
                "- Condition: " + condition + " (" + description + ")\n" +
                "- Wind Speed: " + String.format("%.1f", windSpeed) + " m/s\n" +
                "- Humidity: " + humidity + "%\n" +
                "- Cloud Coverage: " + clouds + "%\n\n" +
                "Consider:\n" +
                "- Temperature comfort zones: <0°C(extreme cold), 0-10°C(cold), 10-20°C(cool), 20-28°C(pleasant), >28°C(hot)\n" +
                "- Weather protection: rain→waterproof, sun→UV protection, wind→wind-resistant\n" +
                "- Fabric recommendations: thermal(cold), moisture-wicking(hot/humid), waterproof(rain), breathable(warm)\n" +
                "- Health & safety: hydration, sun protection, hypothermia prevention\n\n" +
                "Example format:\n" +
                "[\n" +
                "  {\"category\":\"Base Layer\",\"suggestion\":\"Thermal long-sleeve shirt\",\"reasoning\":\"Cold temperature needs insulated foundation\",\"emoji\":\"👕\",\"priority\":\"ESSENTIAL\",\"fabricType\":\"Merino wool thermal\"},\n" +
                "  {\"category\":\"Outer Layer\",\"suggestion\":\"Waterproof rain jacket\",\"reasoning\":\"Rain protection critical to stay dry\",\"emoji\":\"🧥\",\"priority\":\"ESSENTIAL\",\"fabricType\":\"Gore-Tex waterproof\"},\n" +
                "  ...\n" +
                "]\n\n" +
                "Output ONLY the JSON array, no other text. Ensure valid JSON syntax.";
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
                    String priorityStr = obj.optString("priority", "RECOMMENDED");
                    String fabricType = obj.optString("fabricType", "");

                    if (!suggestion.isEmpty()) {
                        // Parse priority from string to enum
                        OutfitSuggestion.Priority priority;
                        try {
                            priority = OutfitSuggestion.Priority.valueOf(priorityStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            priority = OutfitSuggestion.Priority.RECOMMENDED;
                            Log.w(TAG, "Invalid priority '" + priorityStr + "', using RECOMMENDED");
                        }
                        
                        OutfitSuggestion outfitSuggestion = new OutfitSuggestion(
                                category,
                                suggestion,
                                reasoning,
                                emoji,
                                priority,
                                fabricType
                        );
                        suggestions.add(outfitSuggestion);
                        Log.d(TAG, "  ✓ " + (i+1) + ". [" + priority + "] " + category + ": " + suggestion);
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
        double temp = toC(weatherData.getMain().getTemp());
        double feelsLike = toC(weatherData.getMain().getFeelsLike());
        String condition = weatherData.getWeather().get(0).getMain().toLowerCase();
        double windSpeed = weatherData.getWind().getSpeed();
        int humidity = weatherData.getMain().getHumidity();
        
        Log.d(TAG, "Generating default suggestions for: " + temp + "°C, " + condition);

        // Base Layer (Inner clothing)
        if (temp < 0) {
            suggestions.add(new OutfitSuggestion(
                    "Base Layer",
                    "Thermal underwear + warm shirt",
                    "Extreme cold requires insulated base layers",
                    "🥶",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Merino wool or synthetic thermal"
            ));
        } else if (temp < 10) {
            suggestions.add(new OutfitSuggestion(
                    "Base Layer",
                    "Long-sleeve thermal or flannel",
                    "Cold weather needs warm foundation layer",
                    "👕",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Cotton blend or fleece"
            ));
        } else if (temp < 20) {
            suggestions.add(new OutfitSuggestion(
                    "Base Layer",
                    "Long-sleeve shirt or light sweater",
                    "Cool temperature, comfortable base layer needed",
                    "👕",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Cotton or cotton blend"
            ));
        } else if (temp < 28) {
            suggestions.add(new OutfitSuggestion(
                    "Base Layer",
                    "T-shirt or polo shirt",
                    "Pleasant temperature for standard clothing",
                    "👕",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Breathable cotton"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Base Layer",
                    "Light tank top or athletic shirt",
                    "Hot weather requires minimal, breathable base",
                    "🎽",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Moisture-wicking synthetic"
            ));
        }

        // Outer Layer (Jacket/Coat)
        if (condition.contains("rain") || condition.contains("drizzle")) {
            suggestions.add(new OutfitSuggestion(
                    "Outer Layer",
                    "Waterproof rain jacket",
                    "Rain protection essential, stay dry",
                    "🧥",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Waterproof nylon or Gore-Tex"
            ));
        } else if (temp < 5) {
            suggestions.add(new OutfitSuggestion(
                    "Outer Layer",
                    "Heavy winter coat or parka",
                    "Very cold, need maximum insulation",
                    "🧥",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Down or synthetic insulation"
            ));
        } else if (temp < 15) {
            suggestions.add(new OutfitSuggestion(
                    "Outer Layer",
                    "Medium jacket or hoodie",
                    "Cool weather, light insulation needed",
                    "🧥",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Fleece or light insulation"
            ));
        } else if (temp < 22 && windSpeed > 5) {
            suggestions.add(new OutfitSuggestion(
                    "Outer Layer",
                    "Windbreaker or light jacket",
                    "Windy conditions need wind protection",
                    "🧥",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Wind-resistant nylon"
            ));
        } else if (temp >= 22) {
            suggestions.add(new OutfitSuggestion(
                    "Outer Layer",
                    "No jacket needed",
                    "Warm enough without outer layer",
                    "☀️",
                    OutfitSuggestion.Priority.OPTIONAL,
                    "N/A"
            ));
        }

        // Lower Body
        if (temp < 0) {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Insulated pants or thermal leggings",
                    "Extreme cold requires insulated leg wear",
                    "🥾",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Thermal or insulated fabric"
            ));
        } else if (temp < 15) {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Jeans or warm pants",
                    "Cool weather, keep legs warm and comfortable",
                    "👖",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Denim or cotton pants"
            ));
        } else if (temp < 25) {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Chinos or casual pants",
                    "Moderate temperature, standard pants work well",
                    "👖",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Cotton or cotton blend"
            ));
        } else if (temp < 30) {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Shorts or light pants",
                    "Warm weather, stay cool with lighter options",
                    "🩳",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Cotton or linen"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Lower Body",
                    "Light shorts or breathable skirt",
                    "Hot weather requires minimal, airy clothing",
                    "🩳",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Linen or moisture-wicking"
            ));
        }

        // Footwear
        if (condition.contains("rain") || condition.contains("drizzle")) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Waterproof boots or rain shoes",
                    "Wet conditions require waterproof footwear",
                    "👢",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Waterproof rubber or treated leather"
            ));
        } else if (condition.contains("snow")) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Insulated winter boots",
                    "Snow requires warm, waterproof boots",
                    "🥾",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Insulated waterproof boots"
            ));
        } else if (temp < 10) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Closed shoes or ankle boots",
                    "Cold weather, keep feet warm and protected",
                    "👟",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Leather or insulated material"
            ));
        } else if (temp < 25) {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Sneakers or casual shoes",
                    "Comfortable shoes for moderate weather",
                    "👟",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Canvas or breathable mesh"
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Footwear",
                    "Sandals or breathable sneakers",
                    "Hot weather, prioritize ventilation and comfort",
                    "👡",
                    OutfitSuggestion.Priority.RECOMMENDED,
                    "Open-toe or mesh material"
            ));
        }

        // Head & Face Protection
        if (condition.contains("rain")) {
            suggestions.add(new OutfitSuggestion(
                    "Head & Face",
                    "Hood or waterproof hat",
                    "Keep head dry in rainy conditions",
                    "🧢",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (condition.contains("sun") || condition.contains("clear")) {
            if (temp > 25) {
                suggestions.add(new OutfitSuggestion(
                        "Head & Face",
                        "Sun hat and sunglasses",
                        "Strong sun requires face and eye protection",
                        "�️",
                        OutfitSuggestion.Priority.ESSENTIAL
                ));
            } else {
                suggestions.add(new OutfitSuggestion(
                        "Head & Face",
                        "Sunglasses recommended",
                        "Sunny weather, protect your eyes",
                        "🕶️",
                        OutfitSuggestion.Priority.RECOMMENDED
                ));
            }
        } else if (temp < 5) {
            suggestions.add(new OutfitSuggestion(
                    "Head & Face",
                    "Warm beanie or winter hat",
                    "Very cold, prevent heat loss from head",
                    "🧢",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (windSpeed > 8) {
            suggestions.add(new OutfitSuggestion(
                    "Head & Face",
                    "Cap or hat for wind protection",
                    "Windy conditions, secure headwear helps",
                    "🧢",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Head & Face",
                    "Optional cap or sunglasses",
                    "Comfortable conditions, headwear is optional",
                    "🧢",
                    OutfitSuggestion.Priority.OPTIONAL
            ));
        }

        // Accessories
        if (condition.contains("rain")) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Umbrella + waterproof bag",
                    "Essential rain protection for you and belongings",
                    "☂️",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (temp < 5) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Scarf, gloves, and hand warmers",
                    "Very cold, protect extremities from frostbite",
                    "🧣",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (temp < 15) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Light scarf and gloves",
                    "Cool weather, extra warmth accessories helpful",
                    "🧣",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        } else if (condition.contains("sun") || temp > 25) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Sunscreen SPF 30+ and water bottle",
                    "Sun/heat protection, stay hydrated",
                    "�",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (windSpeed > 6) {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Light scarf for wind",
                    "Windy day, scarf provides comfort",
                    "🧣",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Accessories",
                    "Backpack or bag for layers",
                    "Weather may change, bring extra layer",
                    "🎒",
                    OutfitSuggestion.Priority.OPTIONAL
            ));
        }

        // Extra Tips
        if (condition.contains("snow")) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Layer with thermal + fleece + coat",
                    "Snow requires 3-layer system for warmth",
                    "❄️",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (temp > 32) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Light colors + hydrate every 30min",
                    "Extreme heat, light colors reflect sun better",
                    "🥵",
                    OutfitSuggestion.Priority.ESSENTIAL
            ));
        } else if (feelsLike < temp - 3) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Dress for feels-like temperature",
                    "Wind chill makes it feel colder than actual temp",
                    "🌡️",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        } else if (humidity > 80 && temp > 20) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Moisture-wicking fabrics preferred",
                    "High humidity increases sweat, need breathable fabric",
                    "💧",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        } else if (humidity < 30) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Use moisturizer, lip balm",
                    "Low humidity dries skin, protect with moisturizer",
                    "💄",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        } else if (Math.abs(temp - 20) < 5) {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Perfect weather - enjoy outdoors!",
                    "Ideal temperature range for outdoor activities",
                    "✨",
                    OutfitSuggestion.Priority.OPTIONAL
            ));
        } else {
            suggestions.add(new OutfitSuggestion(
                    "Extra Tips",
                    "Bring extra layer, just in case",
                    "Weather can change, be prepared with backup",
                    "🎒",
                    OutfitSuggestion.Priority.RECOMMENDED
            ));
        }

        Log.d(TAG, "Generated " + suggestions.size() + " default outfit suggestions");
        return suggestions;
    }
}
