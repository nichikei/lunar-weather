package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.WeatherRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetWeatherByCityUseCase
 * Tests business logic validation
 */
public class GetWeatherByCityUseCaseTest {

    @Mock
    private WeatherRepository mockRepository;

    @Mock
    private GetWeatherByCityUseCase.Callback mockCallback;

    private GetWeatherByCityUseCase useCase;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetWeatherByCityUseCase(mockRepository);
    }

    @Test
    public void execute_withValidCity_callsRepository() {
        // Given
        String cityName = "Hanoi";
        String tempUnit = "celsius";

        // When
        useCase.execute(cityName, tempUnit, mockCallback);

        // Then
        verify(mockRepository).getWeatherByCity(eq(cityName), eq(tempUnit), any());
    }

    @Test
    public void execute_withEmptyCity_callsOnError() {
        // Given
        String cityName = "";
        String tempUnit = "celsius";

        // When
        useCase.execute(cityName, tempUnit, mockCallback);

        // Then
        verify(mockCallback).onError("City name cannot be empty");
        verify(mockRepository, never()).getWeatherByCity(anyString(), anyString(), any());
    }

    @Test
    public void execute_withNullCity_callsOnError() {
        // Given
        String cityName = null;
        String tempUnit = "celsius";

        // When
        useCase.execute(cityName, tempUnit, mockCallback);

        // Then
        verify(mockCallback).onError("City name cannot be empty");
        verify(mockRepository, never()).getWeatherByCity(anyString(), anyString(), any());
    }

    @Test
    public void execute_withWhitespaceCity_callsOnError() {
        // Given
        String cityName = "   ";
        String tempUnit = "celsius";

        // When
        useCase.execute(cityName, tempUnit, mockCallback);

        // Then
        verify(mockCallback).onError("City name cannot be empty");
    }

    @Test
    public void execute_repositorySuccess_callsOnSuccess() {
        // Given
        String cityName = "Hanoi";
        String tempUnit = "celsius";
        WeatherData mockData = createMockWeatherData(cityName);

        ArgumentCaptor<WeatherRepository.WeatherCallback> captor = 
            ArgumentCaptor.forClass(WeatherRepository.WeatherCallback.class);

        // When
        useCase.execute(cityName, tempUnit, mockCallback);
        verify(mockRepository).getWeatherByCity(eq(cityName), eq(tempUnit), captor.capture());
        
        // Simulate repository success
        captor.getValue().onSuccess(mockData);

        // Then
        verify(mockCallback).onSuccess(mockData);
    }

    @Test
    public void execute_repositoryError_callsOnError() {
        // Given
        String cityName = "Hanoi";
        String tempUnit = "celsius";
        String errorMessage = "Network error";

        ArgumentCaptor<WeatherRepository.WeatherCallback> captor = 
            ArgumentCaptor.forClass(WeatherRepository.WeatherCallback.class);

        // When
        useCase.execute(cityName, tempUnit, mockCallback);
        verify(mockRepository).getWeatherByCity(eq(cityName), eq(tempUnit), captor.capture());
        
        // Simulate repository error
        captor.getValue().onError(errorMessage);

        // Then
        verify(mockCallback).onError(errorMessage);
    }

    @Test
    public void execute_withDifferentTemperatureUnits_passesCorrectUnit() {
        // Test Celsius
        useCase.execute("Hanoi", "celsius", mockCallback);
        verify(mockRepository).getWeatherByCity(eq("Hanoi"), eq("celsius"), any());

        // Test Fahrenheit
        useCase.execute("Hanoi", "fahrenheit", mockCallback);
        verify(mockRepository).getWeatherByCity(eq("Hanoi"), eq("fahrenheit"), any());
    }

    // Helper method
    private WeatherData createMockWeatherData(String cityName) {
        return new WeatherData.Builder()
                .setCityName(cityName)
                .setCountryCode("VN")
                .setTemperature(25.0)
                .setTemperatureUnit("celsius")
                .build();
    }
}
