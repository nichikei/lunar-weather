package com.example.weatherapp.domain.usecase;

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
 * Unit tests for GetUVIndexUseCase
 */
public class GetUVIndexUseCaseTest {
    
    @Mock
    private WeatherRepository repository;
    
    @Mock
    private GetUVIndexUseCase.Callback callback;
    
    private GetUVIndexUseCase useCase;
    
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetUVIndexUseCase(repository);
    }
    
    @Test
    public void execute_withValidCoordinates_callsRepository() {
        // Arrange
        double latitude = 40.7128;
        double longitude = -74.0060;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(repository).getUVIndex(
                eq(latitude),
                eq(longitude),
                any(WeatherRepository.UVIndexCallback.class)
        );
    }
    
    @Test
    public void execute_withInvalidLatitude_callsOnError() {
        // Arrange
        double latitude = 91.0; // Invalid: > 90
        double longitude = 0.0;
        
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(callback).onError(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().contains("Invalid"));
    }
    
    @Test
    public void execute_withInvalidLongitude_callsOnError() {
        // Arrange
        double latitude = 0.0;
        double longitude = -181.0; // Invalid: < -180
        
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(callback).onError(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().contains("Invalid"));
    }
    
    @Test
    public void execute_withEquatorCoordinates_callsRepository() {
        // Arrange - Equator, Prime Meridian
        double latitude = 0.0;
        double longitude = 0.0;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(repository).getUVIndex(
                eq(0.0),
                eq(0.0),
                any(WeatherRepository.UVIndexCallback.class)
        );
    }
    
    @Test
    public void execute_withNorthPole_callsRepository() {
        // Arrange - North Pole
        double latitude = 90.0;
        double longitude = 0.0;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(repository).getUVIndex(
                eq(90.0),
                eq(0.0),
                any(WeatherRepository.UVIndexCallback.class)
        );
    }
    
    @Test
    public void execute_withSouthPole_callsRepository() {
        // Arrange - South Pole
        double latitude = -90.0;
        double longitude = 0.0;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(repository).getUVIndex(
                eq(-90.0),
                eq(0.0),
                any(WeatherRepository.UVIndexCallback.class)
        );
    }
    
    @Test
    public void execute_withDatelineCoordinates_callsRepository() {
        // Arrange - International Date Line
        double latitude = 0.0;
        double longitude = 180.0;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(repository).getUVIndex(
                eq(0.0),
                eq(180.0),
                any(WeatherRepository.UVIndexCallback.class)
        );
        
        // Also test -180
        useCase.execute(latitude, -180.0, callback);
        verify(repository).getUVIndex(
                eq(0.0),
                eq(-180.0),
                any(WeatherRepository.UVIndexCallback.class)
        );
    }
    
    @Test
    public void execute_multipleCallsWithDifferentCoordinates_callsRepositoryEachTime() {
        // Arrange - different famous cities
        double[] latitudes = {40.7128, 51.5074, 35.6762}; // NYC, London, Tokyo
        double[] longitudes = {-74.0060, -0.1278, 139.6503};
        
        // Act
        for (int i = 0; i < latitudes.length; i++) {
            useCase.execute(latitudes[i], longitudes[i], callback);
        }
        
        // Assert - verify each call
        verify(repository).getUVIndex(eq(40.7128), eq(-74.0060), any());
        verify(repository).getUVIndex(eq(51.5074), eq(-0.1278), any());
        verify(repository).getUVIndex(eq(35.6762), eq(139.6503), any());
    }
}
