package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherapp.R;
import com.example.weatherapp.data.models.CityWeather;
import com.example.weatherapp.databinding.ActivitySearchBinding;
import com.example.weatherapp.presentation.state.UIState;
import com.example.weatherapp.presentation.viewmodel.SearchViewModel;
import com.example.weatherapp.ui.adapters.CityWeatherAdapter;
import com.example.weatherapp.ui.helpers.LocationHelper;
import com.example.weatherapp.ui.helpers.RecyclerViewScrollAnimator;
import com.example.weatherapp.ui.helpers.SlideInItemAnimator;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * SearchActivity with MVVM pattern
 * Allows users to search for cities or use current location
 */
public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private CityWeatherAdapter adapter;
    private LocationHelper locationHelper;
    
    public static final String EXTRA_CITY_NAME = "city_name";
    public static final String EXTRA_USE_GPS = "use_gps";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";
    private static final int MAX_RECENT_SEARCHES = 10;
    
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        
        locationHelper = new LocationHelper(this, LocationServices.getFusedLocationProviderClient(this));
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();

        // Setup animated background
        setupAnimatedBackground();
        setupBlurEffect();
        
        setupObservers();
        setupListeners();
        setupRecyclerView();
        
        // Load recent searches first, if none then load popular cities
        loadRecentSearches();
    }
    
    /**
     * Setup animated gradient background
     */
    private void setupAnimatedBackground() {
        try {
            View animatedBg = binding.getRoot().findViewById(R.id.animatedBackground);
            if (animatedBg != null) {
                AnimationDrawable animDrawable = (AnimationDrawable) animatedBg.getBackground();
                if (animDrawable != null) {
                    animDrawable.setEnterFadeDuration(2000);
                    animDrawable.setExitFadeDuration(2000);
                    animDrawable.start();
                }
            }
        } catch (Exception e) {
            Log.e("SearchActivity", "Error starting background animation", e);
        }
    }
    
    /**
     * Setup blur effect overlay
     */
    private void setupBlurEffect() {
        try {
            eightbitlab.com.blurview.BlurView blurView = binding.getRoot().findViewById(R.id.blurOverlay);
            if (blurView != null) {
                float radius = 10f;
                
                View decorView = getWindow().getDecorView();
                ViewGroup rootView = decorView.findViewById(android.R.id.content);
                android.graphics.drawable.Drawable windowBackground = decorView.getBackground();
                
                blurView.setupWith(rootView, new eightbitlab.com.blurview.RenderScriptBlur(this))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius);
            }
        } catch (Exception e) {
            Log.e("SearchActivity", "Error setting up blur effect", e);
        }
    }
    
    /**
     * Setup LiveData observers (MVVM pattern)
     */
    private void setupObservers() {
        // Observe cities state
        viewModel.getCitiesState().observe(this, state -> {
            if (state instanceof UIState.Loading) {
                // Show loading if needed
            } else if (state instanceof UIState.Success) {
                List<CityWeather> cities = ((UIState.Success<List<CityWeather>>) state).getData();
                updateCitiesList(cities);
            } else if (state instanceof UIState.Error) {
                String error = ((UIState.Error<List<CityWeather>>) state).getMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe location state
        viewModel.getLocationState().observe(this, state -> {
            if (state instanceof UIState.Success) {
                SearchViewModel.LocationData location = ((UIState.Success<SearchViewModel.LocationData>) state).getData();
                returnLocationToMain(location.getLatitude(), location.getLongitude());
            } else if (state instanceof UIState.Error) {
                String error = ((UIState.Error<SearchViewModel.LocationData>) state).getMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new CityWeatherAdapter(new ArrayList<>(), this::onCitySelected);
        binding.recyclerViewCities.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCities.setAdapter(adapter);
        binding.recyclerViewCities.setItemAnimator(new SlideInItemAnimator());
        
        // Add scroll animator
        RecyclerViewScrollAnimator scrollAnimator = new RecyclerViewScrollAnimator();
        binding.recyclerViewCities.addOnScrollListener(scrollAnimator);
    }
    
    /**
     * Update cities list in adapter
     */
    private void updateCitiesList(List<CityWeather> cities) {
        adapter = new CityWeatherAdapter(cities, this::onCitySelected);
        binding.recyclerViewCities.setAdapter(adapter);
    }

    private void setupListeners() {
        // Back button click
        binding.btnBack.setOnClickListener(v -> finish());

        // Search button click
        binding.btnSearchIcon.setOnClickListener(v -> {
            String query = binding.etSearch.getText().toString().trim();
            if (viewModel.validateCitySearch(query)) {
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
                if (viewModel.validateCitySearch(query)) {
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
        if (!locationHelper.hasLocationPermission()) {
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
        locationHelper.getCurrentLocation(new LocationHelper.OnLocationResultListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                viewModel.setLocation(latitude, longitude);
            }

            @Override
            public void onLocationError(String message) {
                viewModel.setLocationError(message);
            }
        });
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

    /**
     * Handle city selection from list
     */
    private void onCitySelected(CityWeather city) {
        // Save to recent searches
        saveRecentSearch(city.getCityName());
        returnCityToMain(city.getCityName());
    }

    private void searchCity(String cityName) {
        // Save to recent searches
        saveRecentSearch(cityName);
        // Return searched city to MainActivity
        returnCityToMain(cityName);
    }

    private void returnCityToMain(String cityName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CITY_NAME, cityName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    /**
     * Save a city search to recent searches history
     */
    private void saveRecentSearch(String cityName) {
        List<String> recentSearches = getRecentSearchesList();
        
        // Remove if already exists (to move it to top)
        recentSearches.remove(cityName);
        
        // Add to beginning
        recentSearches.add(0, cityName);
        
        // Keep only last MAX_RECENT_SEARCHES
        if (recentSearches.size() > MAX_RECENT_SEARCHES) {
            recentSearches = recentSearches.subList(0, MAX_RECENT_SEARCHES);
        }
        
        // Save to SharedPreferences
        String json = gson.toJson(recentSearches);
        sharedPreferences.edit().putString(KEY_RECENT_SEARCHES, json).apply();
    }
    
    /**
     * Get recent searches list from SharedPreferences
     */
    private List<String> getRecentSearchesList() {
        String json = sharedPreferences.getString(KEY_RECENT_SEARCHES, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            return new ArrayList<>(gson.fromJson(json, type));
        }
        return new ArrayList<>();
    }
    
    /**
     * Load recent searches and display them
     */
    private void loadRecentSearches() {
        List<String> recentSearches = getRecentSearchesList();
        
        if (!recentSearches.isEmpty()) {
            // Show recent searches header
            binding.tvRecentSearches.setVisibility(android.view.View.VISIBLE);
            
            // Convert city names to CityWeather objects for adapter
            List<CityWeather> recentCities = new ArrayList<>();
            for (String cityName : recentSearches) {
                // Create CityWeather with minimal info for recent searches
                CityWeather city = new CityWeather(
                    cityName,
                    "", // Empty country
                    "Recent Search", // Description
                    0, // temp
                    0, // high
                    0, // low
                    "" // icon name
                );
                recentCities.add(city);
            }
            
            updateCitiesList(recentCities);
        } else {
            // No recent searches, load popular cities
            binding.tvRecentSearches.setVisibility(android.view.View.GONE);
            viewModel.loadPopularCities();
        }
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
