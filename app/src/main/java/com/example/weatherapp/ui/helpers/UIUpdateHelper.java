package com.example.weatherapp.ui.helpers;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.WeatherData;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Helper class to update UI elements in MainActivity
 * Handles all UI update logic including cards, temperature, weather details
 */
public class UIUpdateHelper {

    private final ActivityMainBinding binding;
    private final String temperatureUnit;
    private final String windSpeedUnit;
    private final String pressureUnit;

    public UIUpdateHelper(ActivityMainBinding binding, String temperatureUnit, 
                         String windSpeedUnit, String pressureUnit) {
        this.binding = binding;
        this.temperatureUnit = temperatureUnit;
        this.windSpeedUnit = windSpeedUnit;
        this.pressureUnit = pressureUnit;
    }

    /**
     * Update main weather information (city, temp, description)
     * iOS Style: Large temperature (102sp in XML), city name prominent
     */
    public void updateMainWeatherInfo(WeatherData weatherData) {
        // Update city name
        binding.tvCityName.setText(weatherData.getCityName());
        binding.tvTopBarCityName.setText(weatherData.getCityName());

        // Update weather description with iOS formatting
        String description = weatherData.getWeatherDescription();
        if (description != null && !description.isEmpty()) {
            binding.tvWeatherDescription.setText(capitalizeWords(description));
        }

        // Update main temperature (iOS style - just the degree symbol)
        double temp = weatherData.getTemperature();
        String tempSymbol = "°";
        binding.tvTemperature.setText(String.format(Locale.getDefault(), "%.0f%s", temp, tempSymbol));

        // Update temperature range (iOS format: H:29° L:15°)
        double tempMax = weatherData.getMaxTemperature();
        double tempMin = weatherData.getMinTemperature();
        binding.tvTempRange.setText(String.format(Locale.getDefault(),
                "H:%.0f%s  L:%.0f%s", tempMax, tempSymbol, tempMin, tempSymbol));
    }

    /**
     * Update all weather detail cards (iOS Style)
     * Larger values, descriptive text matching iOS Weather
     */
    public void updateWeatherDetailsCards(WeatherData weatherData) {
        String tempSymbol = "°";
        
        // Feels Like (iOS: "73%", "Similar to the actual temperature")
        double feelsLike = weatherData.getFeelsLike();
        double actualTemp = weatherData.getTemperature();
        String feelsLikeDesc;
        if (Math.abs(feelsLike - actualTemp) < 2) {
            feelsLikeDesc = "Similar to the actual temperature";
        } else if (feelsLike > actualTemp) {
            feelsLikeDesc = String.format(Locale.getDefault(), "Feels %.0f%s warmer due to humidity", 
                    feelsLike - actualTemp, tempSymbol);
        } else {
            feelsLikeDesc = String.format(Locale.getDefault(), "Wind is making it feel %.0f%s cooler", 
                    actualTemp - feelsLike, tempSymbol);
        }
        updateCard(R.id.cardFeelsLike, "🌡️", "FEELS LIKE",
                String.format(Locale.getDefault(), "%.0f%s", feelsLike, tempSymbol), feelsLikeDesc);

        // Humidity (iOS: "73%", "The dew point is 16° right now.")
        int humidity = weatherData.getHumidity();
        double dewPoint = calculateDewPoint(weatherData.getTemperature(), humidity);
        String humidityDesc = String.format(Locale.getDefault(), 
                "The dew point is %.0f%s right now.", dewPoint, tempSymbol);
        updateCard(R.id.cardHumidity, "💧", "HUMIDITY",
                String.format(Locale.getDefault(), "%d%%", humidity), humidityDesc);

        // Wind (iOS: "1 m/s" with direction)
        updateWindCard(weatherData.getWindSpeed(), weatherData.getWindDegree());

        // Pressure
        int pressure = (int) weatherData.getPressure();
        String pressureDesc = pressure > 1013 ? "High pressure" : pressure > 1000 ? "Normal pressure" : "Low pressure";
        updateCard(R.id.cardPressure, "📊", "PRESSURE",
                String.format(Locale.getDefault(), "%d hPa", pressure), pressureDesc);

        // Sunrise/Sunset (iOS: "6:28 AM", "Sunset: 6:10 PM")
        long sunrise = weatherData.getSunrise();
        long sunset = weatherData.getSunset();
        String sunriseTime = formatTimeIOS(sunrise);
        String sunsetTime = formatTimeIOS(sunset);
        updateCard(R.id.cardSunrise, "🌅", "SUNRISE", sunriseTime, "Sunset: " + sunsetTime);

        // Visibility (iOS: "10 km", "Perfectly clear view")
        int visibility = (int) (weatherData.getVisibility() / 1000);
        String visDesc = visibility >= 10 ? "Perfectly clear view" : 
                         visibility >= 5 ? "Good visibility" : "Limited visibility";
        updateCard(R.id.cardVisibility, "👁️", "VISIBILITY", 
                String.format(Locale.getDefault(), "%d km", visibility), visDesc);

        // Rainfall (iOS: "0 mm", "in last 24h", "4 mm expected in next 24h")
        double rainfall = 0.0;
        String rainfallDesc = rainfall > 0 ? "in last 24h" : "0 mm expected in next 24h";
        updateCard(R.id.cardRainfall, "🌧️", "RAINFALL",
                String.format(Locale.getDefault(), "%.0f mm", rainfall), rainfallDesc);
    }

    /**
     * Update wind card with proper unit conversion (iOS Style: shows speed and direction)
     */
    private void updateWindCard(double windSpeed, int windDeg) {
        String windValue, windUnit;

        if (windSpeedUnit.equals("kmh")) {
            if (temperatureUnit.equals("imperial")) {
                windSpeed = windSpeed * 1.60934; // mph to km/h
            } else {
                windSpeed = windSpeed * 3.6; // m/s to km/h
            }
            windValue = String.format(Locale.getDefault(), "%.0f", windSpeed); // iOS style: no decimal
            windUnit = "km/h";
        } else {
            if (temperatureUnit.equals("imperial")) {
                windSpeed = windSpeed * 0.44704; // mph to m/s
            }
            windValue = String.format(Locale.getDefault(), "%.0f", windSpeed); // iOS style: no decimal
            windUnit = "m/s";
        }

        String direction = getWindDirection(windDeg);
        updateCard(R.id.cardWind, "💨", "WIND", windValue + " " + windUnit, direction);
    }

    /**
     * Update UV Index card (iOS Style: "0", "Low", "Low for the rest of the day.")
     */
    public void updateUVIndexCard(int uvIndex) {
        String uvLevel, uvDesc;
        if (uvIndex < 3) {
            uvLevel = "Low";
            uvDesc = "Low for the rest of the day.";
        } else if (uvIndex < 6) {
            uvLevel = "Moderate";
            uvDesc = "Moderate exposure. Wear sunscreen if outside for extended periods.";
        } else if (uvIndex < 8) {
            uvLevel = "High";
            uvDesc = "High exposure. Protection required.";
        } else if (uvIndex < 11) {
            uvLevel = "Very High";
            uvDesc = "Very high exposure. Extra protection essential.";
        } else {
            uvLevel = "Extreme";
            uvDesc = "Extreme risk of harm from unprotected sun exposure.";
        }

        // iOS shows UV value large, then level below
        updateCard(R.id.cardUvIndex, "☀️", "UV INDEX",
                String.valueOf(uvIndex), uvLevel + "\n" + uvDesc);
    }

    /**
     * Update Air Quality card
     */
    public void updateAirQualityCard(AirQualityData aqiData) {
        View aqiCard = binding.getRoot().findViewById(R.id.card_air_quality);
        if (aqiCard == null) return;

        int aqi = aqiData.getAqi();
        String status, description;

        switch (aqi) {
            case 1:
                status = "Good";
                description = "Air quality is satisfactory";
                break;
            case 2:
                status = "Fair";
                description = "Air quality is acceptable";
                break;
            case 3:
                status = "Moderate";
                description = "Sensitive groups may be affected";
                break;
            case 4:
                status = "Poor";
                description = "Everyone may experience health effects";
                break;
            case 5:
                status = "Very Poor";
                description = "Health alert: everyone may be affected";
                break;
            default:
                status = "Unknown";
                description = "No data available";
        }

        TextView tvAqiValue = aqiCard.findViewById(R.id.tvAqiValue);
        TextView tvAqiStatus = aqiCard.findViewById(R.id.tvAqiStatus);
        TextView tvAqiDescription = aqiCard.findViewById(R.id.tvAqiDescription);
        TextView tvPm25 = aqiCard.findViewById(R.id.tvPm25);
        TextView tvPm10 = aqiCard.findViewById(R.id.tvPm10);
        TextView tvO3 = aqiCard.findViewById(R.id.tvO3);

        if (tvAqiValue != null) tvAqiValue.setText(String.valueOf(aqi));
        if (tvAqiStatus != null) tvAqiStatus.setText(status);
        if (tvAqiDescription != null) tvAqiDescription.setText(description);

        // AirQualityData already has components flattened
        if (tvPm25 != null) tvPm25.setText(String.format(Locale.getDefault(), "%.1f", aqiData.getPm2_5()));
        if (tvPm10 != null) tvPm10.setText(String.format(Locale.getDefault(), "%.1f", aqiData.getPm10()));
        if (tvO3 != null) tvO3.setText(String.format(Locale.getDefault(), "%.1f", aqiData.getO3()));
    }

    /**
     * Generic method to update a card
     */
    public void updateCard(int cardId, String icon, String title, String value, String description) {
        View card = binding.getRoot().findViewById(cardId);
        if (card != null) {
            View contentView = card;
            if (card instanceof ViewGroup && ((ViewGroup) card).getChildCount() > 0) {
                contentView = ((ViewGroup) card).getChildAt(0);
            }

            TextView tvIcon = contentView.findViewById(R.id.tvDetailIcon);
            TextView tvTitle = contentView.findViewById(R.id.tvDetailTitle);
            TextView tvValue = contentView.findViewById(R.id.tvDetailValue);
            TextView tvDesc = contentView.findViewById(R.id.tvDetailDescription);

            if (tvIcon != null) tvIcon.setText(icon);
            if (tvTitle != null) tvTitle.setText(title);
            if (tvValue != null) tvValue.setText(value);
            if (tvDesc != null) {
                tvDesc.setText(description);
                tvDesc.setVisibility(View.VISIBLE);
            }
        }
    }

    // ============ Helper Methods ============

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(timestamp * 1000);
    }

    /**
     * Format time in iOS style: "6:28 AM", "6:10 PM"
     */
    private String formatTimeIOS(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(timestamp * 1000);
    }

    /**
     * Calculate dew point using Magnus-Tetens formula
     * @param temperature Temperature in Celsius
     * @param humidity Relative humidity (0-100)
     * @return Dew point in Celsius
     */
    private double calculateDewPoint(double temperature, int humidity) {
        // Convert to Celsius if needed
        double tempC = temperatureUnit.equals("imperial") ? (temperature - 32) * 5 / 9 : temperature;
        
        // Magnus-Tetens constants
        double a = 17.27;
        double b = 237.7;
        
        // Calculate alpha
        double alpha = ((a * tempC) / (b + tempC)) + Math.log(humidity / 100.0);
        
        // Calculate dew point in Celsius
        double dewPointC = (b * alpha) / (a - alpha);
        
        // Convert back to imperial if needed
        return temperatureUnit.equals("imperial") ? (dewPointC * 9 / 5) + 32 : dewPointC;
    }

    /**
     * Get wind direction from degrees
     */
    private String getWindDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(((degrees % 360) / 45.0)) % 8;
        return directions[index];
    }

    private String capitalizeWords(String str) {
        String[] words = str.split("\\s");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedWords.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return capitalizedWords.toString().trim();
    }
}
