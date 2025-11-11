package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.model.AirQualityData;
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
 * Unit tests for GetAirQualityUseCase
 */
public class GetAirQualityUseCaseTest {
    
    @Mock
    private WeatherRepository repository;
    
    @Mock
    private GetAirQualityUseCase.Callback callback;
    
    private GetAirQualityUseCase useCase;
    
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetAirQualityUseCase(repository);
    }
    
    @Test
    public void execute_withValidCoordinates_callsRepository() {
        // Arrange
        double latitude = 40.7128;
        double longitude = -74.0060;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(repository).getAirQuality(
                eq(latitude),
                eq(longitude),
                any(WeatherRepository.AirQualityCallback.class)
        );
    }
    
    @Test
    public void execute_withInvalidLatitude_callsOnError() {
        // Arrange
        double latitude = -91.0; // Invalid: < -90
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
        double longitude = 181.0; // Invalid: > 180
        
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert
        verify(callback).onError(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().contains("Invalid"));
    }
    
    @Test
    public void execute_withBoundaryLatitudes_callsRepository() {
        // Arrange
        double longitude = 0.0;
        
        // Act & Assert - Maximum latitude (90°N)
        useCase.execute(90.0, longitude, callback);
        verify(repository).getAirQuality(
                eq(90.0),
                eq(longitude),
                any(WeatherRepository.AirQualityCallback.class)
        );
        
        // Act & Assert - Minimum latitude (90°S)
        useCase.execute(-90.0, longitude, callback);
        verify(repository).getAirQuality(
                eq(-90.0),
                eq(longitude),
                any(WeatherRepository.AirQualityCallback.class)
        );
    }
    
    @Test
    public void execute_withBoundaryLongitudes_callsRepository() {
        // Arrange
        double latitude = 0.0;
        
        // Act & Assert - Maximum longitude (180°E)
        useCase.execute(latitude, 180.0, callback);
        verify(repository).getAirQuality(
                eq(latitude),
                eq(180.0),
                any(WeatherRepository.AirQualityCallback.class)
        );
        
        // Act & Assert - Minimum longitude (180°W)
        useCase.execute(latitude, -180.0, callback);
        verify(repository).getAirQuality(
                eq(latitude),
                eq(-180.0),
                any(WeatherRepository.AirQualityCallback.class)
        );
    }
    
    @Test
    public void execute_withRealWorldCities_callsRepository() {
        // Arrange & Act & Assert - Beijing (high pollution city)
        useCase.execute(39.9042, 116.4074, callback);
        verify(repository).getAirQuality(
                eq(39.9042),
                eq(116.4074),
                any(WeatherRepository.AirQualityCallback.class)
        );
        
        // Delhi (another high pollution city)
        useCase.execute(28.7041, 77.1025, callback);
        verify(repository).getAirQuality(
                eq(28.7041),
                eq(77.1025),
                any(WeatherRepository.AirQualityCallback.class)
        );
        
        // Reykjavik (clean air city)
        useCase.execute(64.1466, -21.9426, callback);
        verify(repository).getAirQuality(
                eq(64.1466),
                eq(-21.9426),
                any(WeatherRepository.AirQualityCallback.class)
        );
    }
    
    @Test
    public void execute_rapidSuccessiveCalls_callsRepositoryMultipleTimes() {
        // Arrange
        double latitude = 51.5074;  // London
        double longitude = -0.1278;
        
        // Act - simulate rapid refresh
        for (int i = 0; i < 5; i++) {
            useCase.execute(latitude, longitude, callback);
        }
        
        // Assert - should be called 5 times
        verify(repository, org.mockito.Mockito.times(5)).getAirQuality(
                eq(latitude),
                eq(longitude),
                any(WeatherRepository.AirQualityCallback.class)
        );
    }
    
    @Test
    public void execute_withPrecisionCoordinates_callsRepositoryWithExactValues() {
        // Arrange - test high precision coordinates
        double latitude = 40.712776;  // Very precise NYC coordinates
        double longitude = -74.005974;
        
        // Act
        useCase.execute(latitude, longitude, callback);
        
        // Assert - verify exact precision is maintained
        verify(repository).getAirQuality(
                eq(40.712776),
                eq(-74.005974),
                any(WeatherRepository.AirQualityCallback.class)
        );
    }
}
