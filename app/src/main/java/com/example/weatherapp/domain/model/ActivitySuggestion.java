package com.example.weatherapp.domain.model;

/**
 * Domain Model: Activity Suggestion
 * Represents weather-based activity recommendations
 */
public class ActivitySuggestion {
    private String id;
    private String title;
    private String description;
    private String category; // outdoor, indoor, sport, relaxation, social
    private String icon; // emoji or drawable name
    private int suitabilityScore; // 0-100
    private String reason; // Why this activity is recommended
    private String bestTime; // Best time to do this activity
    private String weatherCondition; // e.g., "sunny", "rainy", "cloudy"
    private boolean calendarSyncable; // Can be added to calendar
    private long timestamp;

    // Constructor
    public ActivitySuggestion() {
        this.timestamp = System.currentTimeMillis();
    }

    public ActivitySuggestion(String title, String description, String category, 
                            String icon, int suitabilityScore, String reason, 
                            String bestTime, boolean calendarSyncable) {
        this.id = generateId();
        this.title = title;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.suitabilityScore = suitabilityScore;
        this.reason = reason;
        this.bestTime = bestTime;
        this.calendarSyncable = calendarSyncable;
        this.timestamp = System.currentTimeMillis();
    }

    // Generate unique ID
    private String generateId() {
        return "activity_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getSuitabilityScore() {
        return suitabilityScore;
    }

    public void setSuitabilityScore(int suitabilityScore) {
        this.suitabilityScore = suitabilityScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBestTime() {
        return bestTime;
    }

    public void setBestTime(String bestTime) {
        this.bestTime = bestTime;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public boolean isCalendarSyncable() {
        return calendarSyncable;
    }

    public void setCalendarSyncable(boolean calendarSyncable) {
        this.calendarSyncable = calendarSyncable;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Helper method to get color based on suitability score
    public String getScoreColor() {
        if (suitabilityScore >= 80) return "#4CAF50"; // Green - Excellent
        if (suitabilityScore >= 60) return "#8BC34A"; // Light Green - Good
        if (suitabilityScore >= 40) return "#FFC107"; // Yellow - Fair
        if (suitabilityScore >= 20) return "#FF9800"; // Orange - Poor
        return "#F44336"; // Red - Not Recommended
    }

    // Helper method to get category icon
    public String getCategoryIcon() {
        switch (category.toLowerCase()) {
            case "outdoor": return "🏞️";
            case "indoor": return "🏠";
            case "sport": return "⚽";
            case "relaxation": return "🧘";
            case "social": return "👥";
            case "exercise": return "🏃";
            case "food": return "🍽️";
            case "entertainment": return "🎬";
            default: return "📍";
        }
    }

    @Override
    public String toString() {
        return "ActivitySuggestion{" +
                "title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", score=" + suitabilityScore +
                ", bestTime='" + bestTime + '\'' +
                '}';
    }
}
