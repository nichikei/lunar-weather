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
     */
    public void updateMainWeatherInfo(WeatherData weatherData) {
        // Update city name
        binding.tvCityName.setText(weatherData.getCityName());
        binding.tvTopBarCityName.setText(weatherData.getCityName());

        // Update weather description
        String description = weatherData.getWeatherDescription();
        if (description != null && !description.isEmpty()) {
            binding.tvWeatherDescription.setText(capitalizeWords(description));
        }

        // Update main temperature
        double temp = weatherData.getTemperature();
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";
        binding.tvTemperature.setText(String.format(Locale.getDefault(), "%.0f%s", temp, tempSymbol));

        // Update temperature range
        double tempMax = weatherData.getMaxTemperature();
        double tempMin = weatherData.getMinTemperature();
        binding.tvTempRange.setText(String.format(Locale.getDefault(),
                "H:%.0f%s   L:%.0f%s", tempMax, tempSymbol, tempMin, tempSymbol));
    }

    /**
     * Update all weather detail cards
     */
    public void updateWeatherDetailsCards(WeatherData weatherData) {
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";

        // Feels Like
        updateCard(R.id.cardFeelsLike, "🌡️", "FEELS LIKE",
                String.format(Locale.getDefault(), "%.0f%s", weatherData.getFeelsLike(), tempSymbol),
                "Similar to actual temp");

        // Humidity
        int humidity = weatherData.getHumidity();
        String humidityDesc = humidity > 70 ? "High level" : humidity > 40 ? "Comfortable" : "Low level";
        updateCard(R.id.cardHumidity, "💧", "HUMIDITY",
                String.format(Locale.getDefault(), "%d%%", humidity), humidityDesc);

        // Wind
        updateWindCard(weatherData.getWindSpeed());

        // Pressure
        int pressure = (int) weatherData.getPressure();
        String pressureUnitText = pressureUnit.equals("mbar") ? "mbar" : "hPa";
        updateCard(R.id.cardPressure, "📊", "PRESSURE",
                String.format(Locale.getDefault(), "%d", pressure), pressureUnitText);

        // Sunrise/Sunset
        long sunrise = weatherData.getSunrise();
        long sunset = weatherData.getSunset();
        String sunriseTime = formatTime(sunrise);
        String sunsetTime = formatTime(sunset);
        updateCard(R.id.cardSunrise, "🌅", "SUNRISE", sunriseTime, "Sunset: " + sunsetTime);

        // Visibility
        int visibility = (int) (weatherData.getVisibility() / 1000);
        updateCard(R.id.cardVisibility, "👁️", "VISIBILITY", String.valueOf(visibility), "km");

        // Rainfall - WeatherData doesn't have rainfall field, default to 0
        double rainfall = 0.0;
        updateCard(R.id.cardRainfall, "🌧️", "RAINFALL",
                String.format(Locale.getDefault(), "%.1f", rainfall), "mm last hour");
    }

    /**
     * Update wind card with proper unit conversion
     */
    private void updateWindCard(double windSpeed) {
        String windValue, windUnit;

        if (windSpeedUnit.equals("kmh")) {
            if (temperatureUnit.equals("imperial")) {
                windSpeed = windSpeed * 1.60934; // mph to km/h
            } else {
                windSpeed = windSpeed * 3.6; // m/s to km/h
            }
            windValue = String.format(Locale.getDefault(), "%.1f", windSpeed);
            windUnit = "km/h";
        } else {
            if (temperatureUnit.equals("imperial")) {
                windSpeed = windSpeed * 0.44704; // mph to m/s
            }
            windValue = String.format(Locale.getDefault(), "%.1f", windSpeed);
            windUnit = "m/s";
        }

        updateCard(R.id.cardWind, "💨", "WIND", windValue, windUnit);
    }

    /**
     * Update UV Index card
     */
    public void updateUVIndexCard(int uvIndex) {
        String uvDesc;
        if (uvIndex < 3) {
            uvDesc = "Low";
        } else if (uvIndex < 6) {
            uvDesc = "Moderate";
        } else if (uvIndex < 8) {
            uvDesc = "High";
        } else if (uvIndex < 11) {
            uvDesc = "Very High";
        } else {
            uvDesc = "Extreme";
        }

        updateCard(R.id.cardUvIndex, "☀️", "UV INDEX",
                String.valueOf(uvIndex), uvDesc);
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
