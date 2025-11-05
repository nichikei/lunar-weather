package com.example.weatherapp.data.models;

public class WeatherAlert {
    private String senderName;
    private String event;
    private long start;
    private long end;
    private String description;
    private String[] tags;

    public String getSenderName() {
        return senderName;
    }

    public String getEvent() {
        return event;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    public String getSeverity() {
        // Determine severity based on event type
        String eventLower = event != null ? event.toLowerCase() : "";

        if (eventLower.contains("extreme") || eventLower.contains("tornado") ||
            eventLower.contains("hurricane") || eventLower.contains("severe thunderstorm")) {
            return "EXTREME";
        } else if (eventLower.contains("warning") || eventLower.contains("watch")) {
            return "HIGH";
        } else if (eventLower.contains("advisory") || eventLower.contains("statement")) {
            return "MODERATE";
        }
        return "LOW";
    }

    public int getSeverityColor() {
        switch (getSeverity()) {
            case "EXTREME":
                return 0xFFD32F2F; // Red
            case "HIGH":
                return 0xFFFF6F00; // Orange
            case "MODERATE":
                return 0xFFFDD835; // Yellow
            default:
                return 0xFF43A047; // Green
        }
    }
}


