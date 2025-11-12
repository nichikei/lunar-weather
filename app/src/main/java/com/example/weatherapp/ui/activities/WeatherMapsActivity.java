package com.example.weatherapp.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityWeatherMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.net.URL;
import java.util.Locale;

/**
 * Weather Maps Activity - Shows weather overlays on Google Maps
 * Features: Temperature, Precipitation, Clouds, Wind layers
 */
public class WeatherMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityWeatherMapsBinding binding;
    private GoogleMap mMap;
    private TileOverlay currentOverlay;
    private String currentLayer = "temp_new";
    private static final String API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    
    // Default location (can be passed from MainActivity)
    private double latitude = 21.0285;
    private double longitude = 105.8542;
    private String cityName = "Hanoi";
    
    private boolean isAnimating = false;
    private Handler animationHandler;
    private Runnable animationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherMapsBinding.inflate(getLayoutInflater());
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
    }
    
    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
        
        // City name
        binding.tvCityName.setText(cityName);
        
        // Layer selection chips
        binding.chipTemperature.setOnClickListener(v -> switchLayer("temp_new"));
        binding.chipPrecipitation.setOnClickListener(v -> switchLayer("precipitation_new"));
        binding.chipClouds.setOnClickListener(v -> switchLayer("clouds_new"));
        binding.chipWind.setOnClickListener(v -> switchLayer("wind_new"));
        binding.chipPressure.setOnClickListener(v -> switchLayer("pressure_new"));
        
        // Set initial chip selection
        binding.chipTemperature.setChecked(true);
        
        // Animation toggle
        binding.btnAnimate.setOnClickListener(v -> {
            if (isAnimating) {
                stopAnimation();
            } else {
                startAnimation();
            }
        });
        
        // Refresh button
        binding.btnRefresh.setOnClickListener(v -> {
            refreshCurrentLayer();
        });
    }
    
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Set map style
        try {
            boolean success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)
            );
            if (!success) {
                // Fallback to default style
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        } catch (Exception e) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        
        // Move camera to location
        LatLng location = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        
        // Add marker
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(cityName));
        
        // Enable UI controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        
        // Add weather overlay
        addWeatherLayer(currentLayer);
    }
    
    private void switchLayer(String layerType) {
        currentLayer = layerType;
        
        // Update chip selection
        binding.chipTemperature.setChecked(layerType.equals("temp_new"));
        binding.chipPrecipitation.setChecked(layerType.equals("precipitation_new"));
        binding.chipClouds.setChecked(layerType.equals("clouds_new"));
        binding.chipWind.setChecked(layerType.equals("wind_new"));
        binding.chipPressure.setChecked(layerType.equals("pressure_new"));
        
        // Remove old overlay
        if (currentOverlay != null) {
            currentOverlay.remove();
        }
        
        // Add new layer
        addWeatherLayer(layerType);
        
        // Show feedback
        String layerName = getLayerName(layerType);
        Toast.makeText(this, "Switched to " + layerName, Toast.LENGTH_SHORT).show();
    }
    
    private void addWeatherLayer(String layerType) {
        if (mMap == null) return;
        
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        
        TileOverlayOptions tileOverlayOptions = new TileOverlayOptions()
                .tileProvider(new WeatherTileProvider(layerType))
                .transparency(0.3f); // 70% visible
        
        currentOverlay = mMap.addTileOverlay(tileOverlayOptions);
        
        // Hide loading after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.loadingOverlay.setVisibility(View.GONE);
        }, 1000);
    }
    
    private void refreshCurrentLayer() {
        if (currentOverlay != null) {
            currentOverlay.clearTileCache();
            Toast.makeText(this, "Layer refreshed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startAnimation() {
        isAnimating = true;
        binding.btnAnimate.setText("Stop Animation");
        
        // Animate through different time layers (simulated)
        Toast.makeText(this, "Animation feature coming soon!", Toast.LENGTH_SHORT).show();
        
        // Future implementation: Cycle through forecast timestamps
        // This would require OpenWeatherMap's forecast API
    }
    
    private void stopAnimation() {
        isAnimating = false;
        binding.btnAnimate.setText("Animate");
        
        if (animationHandler != null && animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
        }
    }
    
    private String getLayerName(String layerType) {
        switch (layerType) {
            case "temp_new": return "Temperature";
            case "precipitation_new": return "Precipitation";
            case "clouds_new": return "Clouds";
            case "wind_new": return "Wind Speed";
            case "pressure_new": return "Pressure";
            default: return "Weather";
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAnimation();
    }
    
    /**
     * Custom Tile Provider for OpenWeatherMap layers
     */
    private class WeatherTileProvider extends UrlTileProvider {
        private final String layerType;
        
        public WeatherTileProvider(String layerType) {
            super(256, 256);
            this.layerType = layerType;
        }
        
        @Override
        public URL getTileUrl(int x, int y, int zoom) {
            try {
                String url = String.format(Locale.US,
                        "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=%s",
                        layerType, zoom, x, y, API_KEY);
                return new URL(url);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
