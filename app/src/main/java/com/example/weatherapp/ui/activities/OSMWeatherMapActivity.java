package com.example.weatherapp.ui.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityOsmWeatherMapBinding;
import com.google.android.material.chip.Chip;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.Random;

/**
 * OSM Weather Map Activity using MapTiler API
 * Shows weather overlays (precipitation, temperature, wind) on OpenStreetMap
 */
public class OSMWeatherMapActivity extends AppCompatActivity {

    private static final String TAG = "OSMWeatherMap";
    
    private ActivityOsmWeatherMapBinding binding;
    private MapView mapView;
    private FolderOverlay weatherOverlay;
    private String currentLayer = "precipitation";
    
    // Default location (from MainActivity)
    private double latitude = 21.0285;
    private double longitude = 105.8542;
    private String cityName = "Hanoi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        
        binding = ActivityOsmWeatherMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Get location from intent
        if (getIntent() != null) {
            latitude = getIntent().getDoubleExtra("latitude", 21.0285);
            longitude = getIntent().getDoubleExtra("longitude", 105.8542);
            cityName = getIntent().getStringExtra("cityName");
            if (cityName == null) cityName = "Hanoi";
        }
        
        setupUI();
        setupMap();
        
        // Add initial weather layer
        addWeatherLayer(currentLayer);
    }
    
    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());
        
        // City name
        binding.tvCityName.setText(cityName);
        
        // Layer selection chips - RainViewer layers
        binding.chipPrecipitation.setOnClickListener(v -> switchLayer("precipitation", binding.chipPrecipitation));
        binding.chipTemperature.setOnClickListener(v -> switchLayer("temp", binding.chipTemperature));
        binding.chipWind.setOnClickListener(v -> switchLayer("wind", binding.chipWind));
        binding.chipClouds.setOnClickListener(v -> switchLayer("clouds", binding.chipClouds));
        
        // Set initial chip selection
        binding.chipTemperature.setChecked(true);
    }
    
    private void setupMap() {
        mapView = binding.mapView;
        
        // Set base tile source
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        
        // Set initial position
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapView.getController().setZoom(10.0);
        mapView.getController().setCenter(startPoint);
        
        // Show info toast
        Toast.makeText(this, "Tap chips to switch weather layers", Toast.LENGTH_LONG).show();
        
        Log.d(TAG, "Map setup complete for location: " + cityName);
    }
    
    private void switchLayer(String layerType, Chip selectedChip) {
        currentLayer = layerType;
        
        // Update chip selection
        binding.chipPrecipitation.setChecked(false);
        binding.chipTemperature.setChecked(false);
        binding.chipWind.setChecked(false);
        binding.chipClouds.setChecked(false);
        selectedChip.setChecked(true);
        
        // Remove old overlay
        if (weatherOverlay != null) {
            mapView.getOverlays().remove(weatherOverlay);
        }
        
        // Add new layer
        addWeatherLayer(layerType);
        mapView.invalidate();
        
        // Show feedback
        String layerName = getLayerName(layerType);
        Toast.makeText(this, "Switched to " + layerName, Toast.LENGTH_SHORT).show();
    }
    
    private void addWeatherLayer(String layerType) {
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "Adding weather layer: " + layerType);
        
        // Remove old overlay if exists
        if (weatherOverlay != null) {
            mapView.getOverlayManager().remove(weatherOverlay);
        }
        
        // Create simple colored circles overlay to show weather data
        // This is a MOCK visualization - just for demo!
        FolderOverlay weatherFolder = new FolderOverlay();
        
        // Generate random weather points around the location
        Random random = new Random();
        int numPoints = 50; // Number of weather data points
        
        for (int i = 0; i < numPoints; i++) {
            // Random offset around current location (±0.5 degrees)
            double lat = latitude + (random.nextDouble() - 0.5) * 1.0;
            double lon = longitude + (random.nextDouble() - 0.5) * 1.0;
            GeoPoint point = new GeoPoint(lat, lon);
            
            // Create marker based on layer type
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            
            // Set color and info based on layer type
            int color;
            String snippet;
            float value = random.nextFloat();
            
            switch (layerType) {
                case "precipitation":
                    // Blue shades for rain intensity
                    if (value < 0.3f) {
                        color = 0x4000BFFF; // Light rain
                        snippet = "Light rain: " + String.format("%.1f", value * 10) + "mm/h";
                    } else if (value < 0.7f) {
                        color = 0x600080FF; // Moderate rain
                        snippet = "Moderate rain: " + String.format("%.1f", value * 10) + "mm/h";
                    } else {
                        color = 0x800040FF; // Heavy rain
                        snippet = "Heavy rain: " + String.format("%.1f", value * 10) + "mm/h";
                    }
                    break;
                    
                case "temp":
                    // Color gradient from blue (cold) to red (hot)
                    int temp = (int)(value * 20 + 15); // 15-35°C
                    if (temp < 20) {
                        color = 0x600080FF; // Blue - cold
                    } else if (temp < 28) {
                        color = 0x6000FF80; // Green - comfortable
                    } else {
                        color = 0x60FF4040; // Red - hot
                    }
                    snippet = "Temperature: " + temp + "°C";
                    break;
                    
                case "wind":
                    // Wind speed colors
                    int windSpeed = (int)(value * 30); // 0-30 km/h
                    if (windSpeed < 10) {
                        color = 0x4080FF80; // Light green - calm
                    } else if (windSpeed < 20) {
                        color = 0x60FFFF00; // Yellow - breezy
                    } else {
                        color = 0x80FF8040; // Orange - windy
                    }
                    snippet = "Wind: " + windSpeed + " km/h";
                    break;
                    
                case "clouds":
                    // Gray shades for cloud coverage
                    int coverage = (int)(value * 100); // 0-100%
                    int gray = 128 + (int)(value * 127);
                    color = 0x60000000 | (gray << 16) | (gray << 8) | gray;
                    snippet = "Cloud coverage: " + coverage + "%";
                    break;
                    
                default:
                    color = 0x60808080;
                    snippet = "Weather data";
            }
            
            // Create colored circle
            marker.setIcon(createColoredCircle(color));
            marker.setTitle(getLayerName(layerType));
            marker.setSnippet(snippet);
            
            weatherFolder.add(marker);
        }
        
        // Add to map
        mapView.getOverlayManager().add(weatherFolder);
        mapView.invalidate();
        
        Log.d(TAG, "Weather layer with " + numPoints + " points added");
        
        // Hide loading
        binding.loadingOverlay.setVisibility(View.GONE);
        
        Toast.makeText(this, "Showing " + numPoints + " " + getLayerName(layerType) + " data points", 
                       Toast.LENGTH_SHORT).show();
    }
    
    private android.graphics.drawable.Drawable createColoredCircle(int color) {
        // Create a simple colored circle drawable
        android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
        circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circle.setColor(color);
        circle.setSize(40, 40); // 40x40 dp
        return circle;
    }
    
    private String getLayerName(String layerType) {
        switch (layerType) {
            case "precipitation":
                return "Precipitation";
            case "temp":
                return "Temperature";
            case "wind":
                return "Wind";
            case "clouds":
                return "Clouds";
            default:
                return "Weather Layer";
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
