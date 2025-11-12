package com.example.weatherapp.utils;

import com.example.weatherapp.data.models.OutfitSuggestion;
import com.example.weatherapp.data.responses.WeatherResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for outfit suggestions with activity-specific recommendations
 */
public class OutfitSuggestionUtils {
    
    public enum ActivityType {
        WORK("Work/Office", "Professional attire suitable for office"),
        CASUAL("Casual/Daily", "Comfortable everyday clothing"),
        SPORTS("Sports/Exercise", "Athletic wear for physical activity"),
        OUTDOOR("Outdoor Activities", "Durable clothing for outdoor adventures"),
        FORMAL("Formal Event", "Elegant attire for special occasions"),
        TRAVEL("Travel", "Versatile and comfortable travel wear");
        
        private final String displayName;
        private final String description;
        
        ActivityType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Adjust suggestions based on activity type
     */
    public static List<OutfitSuggestion> adjustForActivity(
            List<OutfitSuggestion> baseSuggestions,
            ActivityType activityType,
            WeatherResponse weather) {
        
        List<OutfitSuggestion> adjusted = new ArrayList<>(baseSuggestions);
        
        switch (activityType) {
            case WORK:
                return adjustForWork(adjusted, weather);
            case SPORTS:
                return adjustForSports(adjusted, weather);
            case OUTDOOR:
                return adjustForOutdoor(adjusted, weather);
            case FORMAL:
                return adjustForFormal(adjusted, weather);
            case TRAVEL:
                return adjustForTravel(adjusted, weather);
            case CASUAL:
            default:
                return adjusted;
        }
    }
    
    private static List<OutfitSuggestion> adjustForWork(List<OutfitSuggestion> suggestions, WeatherResponse weather) {
        List<OutfitSuggestion> workSuggestions = new ArrayList<>();
        
        for (OutfitSuggestion s : suggestions) {
            String category = s.getCategory();
            OutfitSuggestion adjusted = s;
            
            if (category.equals("Base Layer")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Dress shirt or blouse",
                    "Professional attire for office environment",
                    "👔",
                    s.getPriority(),
                    "Cotton or wrinkle-resistant"
                );
            } else if (category.equals("Lower Body")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Dress pants or pencil skirt",
                    "Professional lower wear for office",
                    "👖",
                    s.getPriority(),
                    "Wool blend or dress fabric"
                );
            } else if (category.equals("Footwear")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Dress shoes or loafers",
                    "Professional footwear appropriate for office",
                    "👞",
                    s.getPriority(),
                    "Leather or polished material"
                );
            }
            
            workSuggestions.add(adjusted);
        }
        
        return workSuggestions;
    }
    
    private static List<OutfitSuggestion> adjustForSports(List<OutfitSuggestion> suggestions, WeatherResponse weather) {
        List<OutfitSuggestion> sportsSuggestions = new ArrayList<>();
        
        for (OutfitSuggestion s : suggestions) {
            String category = s.getCategory();
            OutfitSuggestion adjusted = s;
            
            if (category.equals("Base Layer")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Athletic shirt or tank",
                    "Breathable fabric for physical activity",
                    "🎽",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Moisture-wicking synthetic"
                );
            } else if (category.equals("Lower Body")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Athletic shorts or leggings",
                    "Flexible wear for exercise movement",
                    "🩳",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Stretchy synthetic blend"
                );
            } else if (category.equals("Footwear")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Running shoes or sports sneakers",
                    "Supportive footwear for athletic activity",
                    "👟",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Breathable mesh with cushioning"
                );
            } else if (category.equals("Accessories")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Sports watch + water bottle",
                    "Track performance and stay hydrated",
                    "⌚",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    ""
                );
            }
            
            sportsSuggestions.add(adjusted);
        }
        
        // Add sports-specific tip
        sportsSuggestions.add(new OutfitSuggestion(
            "Extra Tips",
            "Warm up properly before exercise",
            "Adjust intensity based on weather conditions",
            "💪",
            OutfitSuggestion.Priority.RECOMMENDED
        ));
        
        return sportsSuggestions;
    }
    
    private static List<OutfitSuggestion> adjustForOutdoor(List<OutfitSuggestion> suggestions, WeatherResponse weather) {
        List<OutfitSuggestion> outdoorSuggestions = new ArrayList<>();
        
        for (OutfitSuggestion s : suggestions) {
            String category = s.getCategory();
            
            // Prioritize durability and protection for outdoor activities
            if (category.equals("Outer Layer")) {
                OutfitSuggestion.Priority newPriority = OutfitSuggestion.Priority.ESSENTIAL;
                outdoorSuggestions.add(new OutfitSuggestion(
                    s.getCategory(),
                    s.getSuggestion(),
                    "Critical protection for outdoor exposure",
                    s.getEmoji(),
                    newPriority,
                    s.getFabricType()
                ));
            } else if (category.equals("Footwear")) {
                outdoorSuggestions.add(new OutfitSuggestion(
                    category,
                    "Hiking boots or trail shoes",
                    "Sturdy footwear with good traction",
                    "🥾",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Waterproof with ankle support"
                ));
            } else {
                outdoorSuggestions.add(s);
            }
        }
        
        // Add outdoor-specific gear
        outdoorSuggestions.add(new OutfitSuggestion(
            "Accessories",
            "Backpack with first aid kit",
            "Safety essentials for outdoor activities",
            "🎒",
            OutfitSuggestion.Priority.ESSENTIAL
        ));
        
        return outdoorSuggestions;
    }
    
    private static List<OutfitSuggestion> adjustForFormal(List<OutfitSuggestion> suggestions, WeatherResponse weather) {
        List<OutfitSuggestion> formalSuggestions = new ArrayList<>();
        
        for (OutfitSuggestion s : suggestions) {
            String category = s.getCategory();
            OutfitSuggestion adjusted = s;
            
            if (category.equals("Base Layer")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Dress shirt or elegant blouse",
                    "Formal attire for special occasions",
                    "👔",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Premium cotton or silk"
                );
            } else if (category.equals("Outer Layer")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Suit jacket or blazer",
                    "Elegant outer layer for formal events",
                    "🤵",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Wool or quality synthetic blend"
                );
            } else if (category.equals("Lower Body")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Dress pants or formal skirt",
                    "Elegant lower wear for formal setting",
                    "👔",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Wool or dress fabric"
                );
            } else if (category.equals("Footwear")) {
                adjusted = new OutfitSuggestion(
                    category,
                    "Dress shoes or heels",
                    "Formal footwear for elegant occasions",
                    "👠",
                    OutfitSuggestion.Priority.ESSENTIAL,
                    "Polished leather"
                );
            }
            
            formalSuggestions.add(adjusted);
        }
        
        return formalSuggestions;
    }
    
    private static List<OutfitSuggestion> adjustForTravel(List<OutfitSuggestion> suggestions, WeatherResponse weather) {
        List<OutfitSuggestion> travelSuggestions = new ArrayList<>();
        
        for (OutfitSuggestion s : suggestions) {
            String category = s.getCategory();
            
            // For travel, adjust reasoning to emphasize versatility and packability
            String travelReasoning = s.getReasoning() + " - Versatile for travel";
            travelSuggestions.add(new OutfitSuggestion(
                s.getCategory(),
                s.getSuggestion(),
                travelReasoning,
                s.getEmoji(),
                s.getPriority(),
                s.getFabricType() + " (wrinkle-resistant)"
            ));
        }
        
        // Add travel-specific tips
        travelSuggestions.add(new OutfitSuggestion(
            "Extra Tips",
            "Pack layers, not bulky items",
            "Layering saves space and adapts to weather changes",
            "🧳",
            OutfitSuggestion.Priority.RECOMMENDED
        ));
        
        travelSuggestions.add(new OutfitSuggestion(
            "Accessories",
            "Travel documents + portable charger",
            "Essential items for smooth travel experience",
            "📱",
            OutfitSuggestion.Priority.ESSENTIAL
        ));
        
        return travelSuggestions;
    }
    
    /**
     * Get outfit summary based on weather
     */
    public static String getOutfitSummary(WeatherResponse weather) {
        double temp = weather.getMain().getTemp() - 273.15; // Convert to Celsius
        String condition = weather.getWeather().get(0).getMain();
        
        StringBuilder summary = new StringBuilder();
        
        if (temp < 0) {
            summary.append("🥶 Extreme cold! Full winter gear essential.");
        } else if (temp < 10) {
            summary.append("🧥 Cold weather - Layer up warmly!");
        } else if (temp < 20) {
            summary.append("🍂 Cool weather - Light jacket recommended.");
        } else if (temp < 28) {
            summary.append("☀️ Pleasant weather - Comfortable clothing.");
        } else {
            summary.append("🌡️ Hot weather - Stay cool & hydrated!");
        }
        
        if (condition.toLowerCase().contains("rain")) {
            summary.append(" ☔ Don't forget rain gear!");
        } else if (condition.toLowerCase().contains("snow")) {
            summary.append(" ❄️ Snow gear necessary!");
        } else if (condition.toLowerCase().contains("clear")) {
            summary.append(" 😎 Perfect for outdoor activities!");
        }
        
        return summary.toString();
    }
}
