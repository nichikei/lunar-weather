package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.WeatherRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for GetWeatherByCoordinatesUseCase
 */
public class GetWeatherByCoordinatesUseCaseTest {
    
    @Mock
    private WeatherRepository repository;
    
    @Mock
    private GetWeatherByCoordinatesUseCase.Callback callback;
    
    private GetWeatherByCoordinatesUseCase useCase;
    
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetWeatherByCoordinatesUseCase(repository);
    }
    
    @Test
    public void execute_withValidCoordinates_callsRepository() {
        // Arrange
        double latitude = 40.7128;
        double longitude = -74.0060;
        String temperatureUnit = "celsius";
        
        // Act
        useCase.execute(latitude, longitude, temperatureUnit, callback);
        
        // Assert
        verify(repository).getWeatherByCoordinates(
                eq(latitude), 
                eq(longitude), 
                eq(temperatureUnit), 
                any(WeatherRepository.WeatherCallback.class)
        );
    }
    
    @Test
    public void execute_withInvalidLatitude_callsOnError() {
        // Arrange - latitude out of range
        double latitude = 100.0; // Invalid: > 90
        double longitude = -74.0060;
        String temperatureUnit = "celsius";
        
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        
        // Act
        useCase.execute(latitude, longitude, temperatureUnit, callback);
        
        // Assert
        verify(callback).onError(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().contains("Invalid"));
    }
    
    @Test
    public void execute_withInvalidLongitude_callsOnError() {
        // Arrange - longitude out of range
        double latitude = 40.7128;
        double longitude = -200.0; // Invalid: < -180
        String temperatureUnit = "celsius";
        
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        
        // Act
        useCase.execute(latitude, longitude, temperatureUnit, callback);
        
        // Assert
        verify(callback).onError(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().contains("Invalid"));
    }
    
    @Test
    public void execute_withBoundaryLatitude_callsRepository() {
        // Arrange - test boundary values
        double latitudeMax = 90.0;
        double latitudeMin = -90.0;
        double longitude = 0.0;
        String temperatureUnit = "celsius";
        
        // Act & Assert - max latitude
        useCase.execute(latitudeMax, longitude, temperatureUnit, callback);
        verify(repository).getWeatherByCoordinates(
                eq(latitudeMax),
                eq(longitude),
                eq(temperatureUnit),
                any(WeatherRepository.WeatherCallback.class)
        );
        
        // Act & Assert - min latitude
        useCase.execute(latitudeMin, longitude, temperatureUnit, callback);
        verify(repository).getWeatherByCoordinates(
                eq(latitudeMin),
                eq(longitude),
                eq(temperatureUnit),
                any(WeatherRepository.WeatherCallback.class)
        );
    }
    
    @Test
    public void execute_withBoundaryLongitude_callsRepository() {
        // Arrange - test boundary values
        double latitude = 0.0;
        double longitudeMax = 180.0;
        double longitudeMin = -180.0;
        String temperatureUnit = "fahrenheit";
        
        // Act & Assert - max longitude
        useCase.execute(latitude, longitudeMax, temperatureUnit, callback);
        verify(repository).getWeatherByCoordinates(
                eq(latitude),
                eq(longitudeMax),
                eq(temperatureUnit),
                any(WeatherRepository.WeatherCallback.class)
        );
        
        // Act & Assert - min longitude
        useCase.execute(latitude, longitudeMin, temperatureUnit, callback);
        verify(repository).getWeatherByCoordinates(
                eq(latitude),
                eq(longitudeMin),
                eq(temperatureUnit),
                any(WeatherRepository.WeatherCallback.class)
        );
    }
    
    @Test
    public void execute_withDifferentTemperatureUnits_passesCorrectUnit() {
        // Arrange
        double latitude = 40.7128;
        double longitude = -74.0060;
        
        // Act & Assert - celsius
        useCase.execute(latitude, longitude, "celsius", callback);
        verify(repository).getWeatherByCoordinates(
                anyDouble(),
                anyDouble(),
                eq("celsius"),
                any(WeatherRepository.WeatherCallback.class)
        );
        
        // Act & Assert - fahrenheit
        useCase.execute(latitude, longitude, "fahrenheit", callback);
        verify(repository).getWeatherByCoordinates(
                anyDouble(),
                anyDouble(),
                eq("fahrenheit"),
                any(WeatherRepository.WeatherCallback.class)
        );
    }
}
