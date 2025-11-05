package com.example.weatherapp.data.models;

import java.io.Serializable;

public class OutfitSuggestion implements Serializable {
    private String category;
    private String suggestion;
    private String reasoning;
    private String emoji;

    public OutfitSuggestion(String category, String suggestion, String reasoning, String emoji) {
        this.category = category;
        this.suggestion = suggestion;
        this.reasoning = reasoning;
        this.emoji = emoji;
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
}


