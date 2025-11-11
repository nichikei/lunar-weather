package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
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

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        
        locationHelper = new LocationHelper(this, LocationServices.getFusedLocationProviderClient(this));

        setupObservers();
        setupListeners();
        setupRecyclerView();
        
        // Load popular cities
        viewModel.loadPopularCities();
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
