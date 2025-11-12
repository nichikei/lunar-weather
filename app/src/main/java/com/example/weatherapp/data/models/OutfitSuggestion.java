package com.example.weatherapp.data.models;

import java.io.Serializable;

public class OutfitSuggestion implements Serializable {
    private String category;
    private String suggestion;
    private String reasoning;
    private String emoji;
    private Priority priority;
    private String fabricType;
    
    // Priority levels for outfit items
    public enum Priority {
        ESSENTIAL("Essential"),
        RECOMMENDED("Recommended"),
        OPTIONAL("Optional");
        
        private final String label;
        
        Priority(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }

    public OutfitSuggestion(String category, String suggestion, String reasoning, String emoji) {
        this.category = category;
        this.suggestion = suggestion;
        this.reasoning = reasoning;
        this.emoji = emoji;
        this.priority = Priority.RECOMMENDED;
        this.fabricType = "";
    }
    
    public OutfitSuggestion(String category, String suggestion, String reasoning, String emoji, Priority priority) {
        this.category = category;
        this.suggestion = suggestion;
        this.reasoning = reasoning;
        this.emoji = emoji;
        this.priority = priority;
        this.fabricType = "";
    }
    
    public OutfitSuggestion(String category, String suggestion, String reasoning, String emoji, Priority priority, String fabricType) {
        this.category = category;
        this.suggestion = suggestion;
        this.reasoning = reasoning;
        this.emoji = emoji;
        this.priority = priority;
        this.fabricType = fabricType;
    }

    public String getCategory() {
        return category;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getReasoning() {
        return reasoning;
    }

    public String getEmoji() {
        return emoji;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public String getFabricType() {
        return fabricType;
    }
    
    public void setFabricType(String fabricType) {
        this.fabricType = fabricType;
    }
    
    public String getPriorityBadge() {
        switch (priority) {
            case ESSENTIAL:
                return "⭐ ";
            case RECOMMENDED:
                return "✓ ";
            case OPTIONAL:
                return "• ";
            default:
                return "";
        }
    }
    
    @Override
    public String toString() {
        return "OutfitSuggestion{" +
                "category='" + category + '\'' +
                ", suggestion='" + suggestion + '\'' +
                ", priority=" + priority +
                '}';
    }
}


