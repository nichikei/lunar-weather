package com.example.weatherapp.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;

/**
 * ViewModel for ChartsActivity
 * Manages chart data state for weather statistics visualization
 */
public class ChartsViewModel extends ViewModel {
    
    private final MutableLiveData<ChartDataState> chartDataState = new MutableLiveData<>();
    
    public LiveData<ChartDataState> getChartDataState() {
        return chartDataState;
    }
    
    /**
     * Load chart data from Intent
     */
    public void loadChartData(HourlyForecastResponse hourlyData, 
                             WeatherResponse currentData, 
                             int uvIndex,
                             String windSpeedUnit) {
        
        if (hourlyData == null || currentData == null) {
            return;
        }
        
        ChartDataState state = new ChartDataState(
            hourlyData,
            currentData,
            uvIndex,
            windSpeedUnit,
            currentData.getName()
        );
        
        chartDataState.setValue(state);
    }
    
    /**
     * Chart data state holder
     */
    public static class ChartDataState {
        public final HourlyForecastResponse hourlyForecastData;
        public final WeatherResponse currentWeatherData;
        public final int currentUVIndex;
        public final String windSpeedUnit;
        public final String cityName;
        
        public ChartDataState(HourlyForecastResponse hourlyForecastData,
                            WeatherResponse currentWeatherData,
                            int currentUVIndex,
                            String windSpeedUnit,
                            String cityName) {
            this.hourlyForecastData = hourlyForecastData;
            this.currentWeatherData = currentWeatherData;
            this.currentUVIndex = currentUVIndex;
            this.windSpeedUnit = windSpeedUnit;
            this.cityName = cityName;
        }
    }
}
