package com.example.weatherapp.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.WeatherRepository;
import com.example.weatherapp.domain.usecase.GetAirQualityUseCase;
import com.example.weatherapp.domain.usecase.GetForecastUseCase;
import com.example.weatherapp.domain.usecase.GetUVIndexUseCase;
import com.example.weatherapp.domain.usecase.GetWeatherByCityUseCase;
import com.example.weatherapp.domain.usecase.GetWeatherByCoordinatesUseCase;
import com.example.weatherapp.presentation.state.UIState;

/**
 * MainViewModel - Heart of MVVM architecture
 * Manages UI state and business logic for MainActivity
 * Survives configuration changes (screen rotation, etc.)
 */
public class MainViewModel extends ViewModel {
    
    // Use Cases
    private final GetWeatherByCityUseCase getWeatherByCityUseCase;
    private final GetWeatherByCoordinatesUseCase getWeatherByCoordinatesUseCase;
    private final GetForecastUseCase getForecastUseCase;
    private final GetUVIndexUseCase getUVIndexUseCase;
    private final GetAirQualityUseCase getAirQualityUseCase;
    
    // LiveData for observing from UI
    private final MutableLiveData<UIState<WeatherData>> weatherState = new MutableLiveData<>(new UIState.Idle<>());
    private final MutableLiveData<UIState<ForecastData>> forecastState = new MutableLiveData<>(new UIState.Idle<>());
    private final MutableLiveData<UIState<Integer>> uvIndexState = new MutableLiveData<>(new UIState.Idle<>());
    private final MutableLiveData<UIState<AirQualityData>> airQualityState = new MutableLiveData<>(new UIState.Idle<>());
    
    // Current settings and state
    private String temperatureUnit = "celsius";
    private String currentCityName = "Hanoi";
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    
    /**
     * Constructor with dependency injection
     */
    public MainViewModel(WeatherRepository repository) {
        // Initialize use cases
        this.getWeatherByCityUseCase = new GetWeatherByCityUseCase(repository);
        this.getWeatherByCoordinatesUseCase = new GetWeatherByCoordinatesUseCase(repository);
        this.getForecastUseCase = new GetForecastUseCase(repository);
        this.getUVIndexUseCase = new GetUVIndexUseCase(repository);
        this.getAirQualityUseCase = new GetAirQualityUseCase(repository);
    }
    
    // ============ Public API for UI ============
    
    /**
     * Get weather state as LiveData for observing
     */
    public LiveData<UIState<WeatherData>> getWeatherState() {
        return weatherState;
    }
    
    public LiveData<UIState<ForecastData>> getForecastState() {
        return forecastState;
    }
    
    public LiveData<UIState<Integer>> getUVIndexState() {
        return uvIndexState;
    }
    
    public LiveData<UIState<AirQualityData>> getAirQualityState() {
        return airQualityState;
    }
    
    // ============ Actions ============
    
    /**
     * Load weather by city name
     */
    public void loadWeatherByCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            weatherState.setValue(new UIState.Error<>("City name cannot be empty"));
            return;
        }
        
        this.currentCityName = cityName;
        weatherState.setValue(new UIState.Loading<>());
        
        getWeatherByCityUseCase.execute(cityName, temperatureUnit, 
            new GetWeatherByCityUseCase.Callback() {
                @Override
                public void onSuccess(WeatherData weatherData) {
                    weatherState.postValue(new UIState.Success<>(weatherData));
                    
                    // Update coordinates for additional data
                    currentLatitude = weatherData.getLatitude();
                    currentLongitude = weatherData.getLongitude();
                    
                    // Load additional data
                    loadForecast();
                    loadUVIndex();
                    loadAirQuality();
                }
                
                @Override
                public void onError(String message) {
                    weatherState.postValue(new UIState.Error<>(message));
                }
            });
    }
    
    /**
     * Load weather by GPS coordinates
     */
    public void loadWeatherByCoordinates(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        weatherState.setValue(new UIState.Loading<>());
        
        getWeatherByCoordinatesUseCase.execute(latitude, longitude, temperatureUnit,
            new GetWeatherByCoordinatesUseCase.Callback() {
                @Override
                public void onSuccess(WeatherData weatherData) {
                    weatherState.postValue(new UIState.Success<>(weatherData));
                    currentCityName = weatherData.getCityName();
                    
                    // Load additional data
                    loadForecast();
                    loadUVIndex();
                    loadAirQuality();
                }
                
                @Override
                public void onError(String message) {
                    weatherState.postValue(new UIState.Error<>(message));
                }
            });
    }
    
    /**
     * Load forecast data
     */
    public void loadForecast() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            return; // No coordinates available
        }
        
        forecastState.setValue(new UIState.Loading<>());
        
        getForecastUseCase.execute(currentLatitude, currentLongitude, temperatureUnit,
            new GetForecastUseCase.Callback() {
                @Override
                public void onSuccess(ForecastData forecastData) {
                    forecastState.postValue(new UIState.Success<>(forecastData));
                }
                
                @Override
                public void onError(String message) {
                    forecastState.postValue(new UIState.Error<>(message));
                }
            });
    }
    
    /**
     * Load UV index
     */
    public void loadUVIndex() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            return; // No coordinates available
        }
        
        uvIndexState.setValue(new UIState.Loading<>());
        
        getUVIndexUseCase.execute(currentLatitude, currentLongitude,
            new GetUVIndexUseCase.Callback() {
                @Override
                public void onSuccess(int uvIndex) {
                    uvIndexState.postValue(new UIState.Success<>(uvIndex));
                }
                
                @Override
                public void onError(String message) {
                    uvIndexState.postValue(new UIState.Error<>(message));
                }
            });
    }
    
    /**
     * Load air quality data
     */
    public void loadAirQuality() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            return; // No coordinates available
        }
        
        airQualityState.setValue(new UIState.Loading<>());
        
        getAirQualityUseCase.execute(currentLatitude, currentLongitude,
            new GetAirQualityUseCase.Callback() {
                @Override
                public void onSuccess(AirQualityData airQualityData) {
                    airQualityState.postValue(new UIState.Success<>(airQualityData));
                }
                
                @Override
                public void onError(String message) {
                    airQualityState.postValue(new UIState.Error<>(message));
                }
            });
    }
    
    /**
     * Refresh all weather data
     */
    public void refreshAllData() {
        if (currentCityName != null && !currentCityName.isEmpty()) {
            loadWeatherByCity(currentCityName);
        } else if (currentLatitude != 0 && currentLongitude != 0) {
            loadWeatherByCoordinates(currentLatitude, currentLongitude);
        }
    }
    
    // ============ Settings ============
    
    public void setTemperatureUnit(String unit) {
        if (!this.temperatureUnit.equals(unit)) {
            this.temperatureUnit = unit;
            // Reload data with new unit
            refreshAllData();
        }
    }
    
    public String getTemperatureUnit() {
        return temperatureUnit;
    }
    
    public String getCurrentCityName() {
        return currentCityName;
    }
    
    public double getCurrentLatitude() {
        return currentLatitude;
    }
    
    public double getCurrentLongitude() {
        return currentLongitude;
    }
    
    /**
     * Get current WeatherData from state
     * @return Current WeatherData or null if not available
     */
    public WeatherData getCurrentWeatherData() {
        UIState<WeatherData> state = weatherState.getValue();
        if (state instanceof UIState.Success) {
            return ((UIState.Success<WeatherData>) state).getData();
        }
        return null;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup resources if needed
    }
}
