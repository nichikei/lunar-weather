package com.example.weatherapp.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;

/**
 * MapActivity - Display weather map using MapTiler
 * Shows interactive weather layers on map
 */
public class MapActivity extends AppCompatActivity {
    
    private static final String TAG = "MapActivity";
    private static final String MAPTILER_API_KEY = "jiSXvHNlIEeyXEwJ2npr";
    
    private WebView webViewMap;
    private ProgressBar progressBar;
    private TextView tvMapLocation;
    private ImageView btnBack;
    private ImageView btnLayers;
    
    private double latitude = 21.0285; // Default: Hanoi
    private double longitude = 105.8542;
    private String cityName = "Current Location";
    private String currentLayer = "precipitation"; // Default layer
    
    private static final String[] LAYER_NAMES = {
        "Precipitation (Rain)", 
        "Temperature (Heat Map)", 
        "Wind Speed",
        "Clouds"
    };
    
    private static final String[] LAYER_IDS = {
        "precipitation",
        "temperature",
        "wind",
        "clouds"
    };
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        initViews();
        setupListeners();
        
        // Get location from intent
        if (getIntent() != null) {
            latitude = getIntent().getDoubleExtra("latitude", latitude);
            longitude = getIntent().getDoubleExtra("longitude", longitude);
            cityName = getIntent().getStringExtra("cityName");
            if (cityName != null) {
                tvMapLocation.setText(cityName);
            }
        }
        
        loadMap();
    }
    
    private void initViews() {
        webViewMap = findViewById(R.id.webViewMap);
        progressBar = findViewById(R.id.progressBar);
        tvMapLocation = findViewById(R.id.tvMapLocation);
        btnBack = findViewById(R.id.btnBack);
        btnLayers = findViewById(R.id.btnLayers);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnLayers.setOnClickListener(v -> showLayerSelectionDialog());
    }
    
    private void showLayerSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Weather Layer");
        
        // Find current layer index
        int currentIndex = 0;
        for (int i = 0; i < LAYER_IDS.length; i++) {
            if (LAYER_IDS[i].equals(currentLayer)) {
                currentIndex = i;
                break;
            }
        }
        
        builder.setSingleChoiceItems(LAYER_NAMES, currentIndex, (dialog, which) -> {
            currentLayer = LAYER_IDS[which];
            changeWeatherLayer(currentLayer);
            dialog.dismiss();
            android.widget.Toast.makeText(this, "Switched to " + LAYER_NAMES[which], android.widget.Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void changeWeatherLayer(String layerType) {
        String jsCode = "changeWeatherLayer('" + layerType + "');";
        webViewMap.evaluateJavascript(jsCode, null);
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void loadMap() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Configure WebView
        WebSettings webSettings = webViewMap.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Map loaded successfully");
            }
        });
        
        // Load MapTiler map with weather layer
        String htmlContent = generateMapHTML();
        webViewMap.loadDataWithBaseURL("https://api.maptiler.com", htmlContent, "text/html", "UTF-8", null);
    }
    
    private String generateMapHTML() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8' />\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no' />\n" +
                "    <title>Weather Map</title>\n" +
                "    <script src='https://cdn.maptiler.com/maptiler-sdk-js/v1.2.0/maptiler-sdk.umd.js'></script>\n" +
                "    <link href='https://cdn.maptiler.com/maptiler-sdk-js/v1.2.0/maptiler-sdk.css' rel='stylesheet' />\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; }\n" +
                "        #map { position: absolute; top: 0; bottom: 0; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='map'></div>\n" +
                "    <script>\n" +
                "        maptilersdk.config.apiKey = '" + MAPTILER_API_KEY + "';\n" +
                "        \n" +
                "        const map = new maptilersdk.Map({\n" +
                "            container: 'map',\n" +
                "            style: 'https://api.maptiler.com/maps/streets-v2/style.json?key=" + MAPTILER_API_KEY + "',\n" +
                "            center: [" + longitude + ", " + latitude + "],\n" +
                "            zoom: 10\n" +
                "        });\n" +
                "        \n" +
                "        const weatherLayers = {\n" +
                "            'precipitation': 'https://api.maptiler.com/tiles/weather-precipitation/{z}/{x}/{y}.png?key=" + MAPTILER_API_KEY + "',\n" +
                "            'temperature': 'https://api.maptiler.com/tiles/weather-temperature/{z}/{x}/{y}.png?key=" + MAPTILER_API_KEY + "',\n" +
                "            'wind': 'https://api.maptiler.com/tiles/weather-wind/{z}/{x}/{y}.png?key=" + MAPTILER_API_KEY + "',\n" +
                "            'clouds': 'https://api.maptiler.com/tiles/weather-clouds/{z}/{x}/{y}.png?key=" + MAPTILER_API_KEY + "'\n" +
                "        };\n" +
                "        \n" +
                "        map.on('load', function() {\n" +
                "            // Add initial weather layer (precipitation)\n" +
                "            map.addSource('weather-source', {\n" +
                "                'type': 'raster',\n" +
                "                'tiles': [weatherLayers['precipitation']],\n" +
                "                'tileSize': 256\n" +
                "            });\n" +
                "            \n" +
                "            map.addLayer({\n" +
                "                'id': 'weather-layer',\n" +
                "                'type': 'raster',\n" +
                "                'source': 'weather-source',\n" +
                "                'paint': {\n" +
                "                    'raster-opacity': 0.7\n" +
                "                }\n" +
                "            });\n" +
                "            \n" +
                "            // Add marker at location\n" +
                "            new maptilersdk.Marker({color: '#FF6B6B'})\n" +
                "                .setLngLat([" + longitude + ", " + latitude + "])\n" +
                "                .setPopup(new maptilersdk.Popup().setText('" + cityName + "'))\n" +
                "                .addTo(map);\n" +
                "        });\n" +
                "        \n" +
                "        // Function to change weather layer\n" +
                "        function changeWeatherLayer(layerType) {\n" +
                "            if (weatherLayers[layerType]) {\n" +
                "                // Remove existing source and layer\n" +
                "                if (map.getLayer('weather-layer')) {\n" +
                "                    map.removeLayer('weather-layer');\n" +
                "                }\n" +
                "                if (map.getSource('weather-source')) {\n" +
                "                    map.removeSource('weather-source');\n" +
                "                }\n" +
                "                \n" +
                "                // Add new source with selected layer\n" +
                "                map.addSource('weather-source', {\n" +
                "                    'type': 'raster',\n" +
                "                    'tiles': [weatherLayers[layerType]],\n" +
                "                    'tileSize': 256\n" +
                "                });\n" +
                "                \n" +
                "                // Add layer back\n" +
                "                map.addLayer({\n" +
                "                    'id': 'weather-layer',\n" +
                "                    'type': 'raster',\n" +
                "                    'source': 'weather-source',\n" +
                "                    'paint': {\n" +
                "                        'raster-opacity': 0.7\n" +
                "                    }\n" +
                "                });\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
    
    @Override
    public void onBackPressed() {
        if (webViewMap.canGoBack()) {
            webViewMap.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
