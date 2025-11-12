package com.example.weatherapp.domain.model;

import java.io.Serializable;

/**
 * Domain model for Voice Weather Assistant queries
 * Represents a parsed voice command with intent and parameters
 */
public class VoiceQuery implements Serializable {
    
    public enum QueryType {
        CURRENT_WEATHER,      // "What's the weather like?"
        TEMPERATURE,          // "What's the temperature?"
        WILL_IT_RAIN,        // "Will it rain today?"
        SHOULD_BRING_UMBRELLA, // "Should I bring an umbrella?"
        OUTFIT_SUGGESTION,    // "What should I wear?"
        UV_INDEX,            // "What's the UV index?"
        AIR_QUALITY,         // "Is the air quality good?"
        FORECAST,            // "What's the forecast for tomorrow?"
        BEST_TIME,           // "When is the best time to go out?"
        SUNRISE_SUNSET,      // "When does the sun set?"
        WIND_SPEED,          // "How windy is it?"
        HUMIDITY,            // "What's the humidity?"
        FEELS_LIKE,          // "What's it feel like outside?"
        GENERAL              // General conversational query
    }
    
    private final String originalText;
    private final QueryType queryType;
    private final String cityName;      // Optional: "What's the weather in Hanoi?"
    private final String timeFrame;     // Optional: "today", "tomorrow", "this week"
    
    public VoiceQuery(String originalText, QueryType queryType, String cityName, String timeFrame) {
        this.originalText = originalText;
        this.queryType = queryType;
        this.cityName = cityName;
        this.timeFrame = timeFrame;
    }
    
    public VoiceQuery(String originalText, QueryType queryType) {
        this(originalText, queryType, null, "now");
    }
    
    // Getters
    public String getOriginalText() { return originalText; }
    public QueryType getQueryType() { return queryType; }
    public String getCityName() { return cityName; }
    public String getTimeFrame() { return timeFrame; }
    
    @Override
    public String toString() {
        return "VoiceQuery{" +
                "type=" + queryType +
                ", text='" + originalText + '\'' +
                ", city='" + cityName + '\'' +
                ", time='" + timeFrame + '\'' +
                '}';
    }
}
