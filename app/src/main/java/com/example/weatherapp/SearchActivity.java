package com.example.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.databinding.ActivitySearchBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private List<CityWeather> cityList;
    public static final String EXTRA_CITY_NAME = "city_name";
    public static final String EXTRA_USE_GPS = "use_gps";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupListeners();
        setupCityList();
    }

    private void setupListeners() {
        // Back button click
        binding.btnBack.setOnClickListener(v -> finish());

        // Search button click
        binding.btnSearchIcon.setOnClickListener(v -> {
            String query = binding.etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchCity(query);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        // Use Current Location button click
        binding.btnUseCurrentLocation.setOnClickListener(v -> {
            requestCurrentLocation();
        });

        // Search when user presses Enter on keyboard
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String query = binding.etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchCity(query);
                    return true;
                } else {
                    Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });
    }

    private void requestCurrentLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Got location, return to MainActivity with coordinates
                        returnLocationToMain(location.getLatitude(), location.getLongitude());
                    } else {
                        // Last location not available, request fresh location
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Request fresh location when last location is not available
    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Requesting fresh location...", Toast.LENGTH_SHORT).show();

        com.google.android.gms.location.LocationRequest locationRequest =
                new com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setMinUpdateIntervalMillis(5000)
                        .setMaxUpdates(1)
                        .build();

        fusedLocationClient.requestLocationUpdates(locationRequest,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult != null && locationResult.getLastLocation() != null) {
                            android.location.Location location = locationResult.getLastLocation();
                            returnLocationToMain(location.getLatitude(), location.getLongitude());
                            // Stop location updates after getting location
                            fusedLocationClient.removeLocationUpdates(this);
                        } else {
                            Toast.makeText(SearchActivity.this,
                                    "Unable to get your location. Please enable location services and try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupCityList() {
        // Popular cities with consistent gradient matching Figma design
        cityList = new ArrayList<>();

        // All cities use the same radial gradient for consistency
        cityList.add(new CityWeather("Montreal", "Canada", "Mid Rain",
                19, 24, 18,
                R.drawable.moon_cloud_mid_rain,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("Toronto", "Canada", "Fast Wind",
                20, 21, 15,
                R.drawable.moon_cloud_fast_wind,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("Tokyo", "Japan", "Showers",
                13, 16, 8,
                R.drawable.sun_cloud_angled_rain,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("Singapore", "Singapore", "Partly Cloudy",
                31, 36, 26,
                R.drawable.sun_cloud_mid_rain,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("Paris", "France", "Clear Sky",
                18, 22, 14,
                R.drawable.sun_cloud_little_rain,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("New York", "United States", "Cloudy",
                16, 19, 12,
                R.drawable.moon_cloud_mid_rain,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("London", "United Kingdom", "Rainy",
                12, 15, 9,
                R.drawable.big_rain_drops,
                R.drawable.weather_detail_card_background));

        cityList.add(new CityWeather("Hanoi", "Vietnam", "Hot & Sunny",
                32, 35, 28,
                R.drawable.sun_cloud_little_rain,
                R.drawable.weather_detail_card_background));

        // Setup RecyclerView
        CityWeatherAdapter adapter = new CityWeatherAdapter(cityList, this::onCityClick);
        binding.recyclerViewCities.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCities.setAdapter(adapter);
    }

    private void onCityClick(CityWeather city) {
        // Return selected city to MainActivity
        returnCityToMain(city.getCityName());
    }

    private void searchCity(String cityName) {
        // Return searched city to MainActivity
        returnCityToMain(cityName);
    }

    private void returnCityToMain(String cityName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CITY_NAME, cityName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void returnLocationToMain(double latitude, double longitude) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_USE_GPS, true);
        resultIntent.putExtra(EXTRA_LATITUDE, latitude);
        resultIntent.putExtra(EXTRA_LONGITUDE, longitude);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
