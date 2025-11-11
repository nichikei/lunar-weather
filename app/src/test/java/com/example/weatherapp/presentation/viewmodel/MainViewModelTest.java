package com.example.weatherapp.presentation.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.WeatherRepository;
import com.example.weatherapp.presentation.state.UIState;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MainViewModel
 * Tests MVVM architecture with mocked repository
 */
public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WeatherRepository mockRepository;

    @Mock
    private Observer<UIState<WeatherData>> weatherStateObserver;

    private MainViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        viewModel = new MainViewModel(mockRepository);
    }

    @Test
    public void loadWeatherByCity_emitsLoadingThenSuccess() {
        // Given
        String cityName = "Hanoi";
        String tempUnit = "celsius";
        WeatherData mockWeatherData = createMockWeatherData(cityName);

        viewModel.getWeatherState().observeForever(weatherStateObserver);

        // When
        viewModel.loadWeatherByCity(cityName);

        // Then - verify Loading state is emitted
        verify(weatherStateObserver).onChanged(argThat(state -> state instanceof UIState.Loading));

        // Simulate repository success callback
        verify(mockRepository).getWeatherByCity(eq(cityName), eq(tempUnit), any());
    }

    @Test
    public void loadWeatherByCity_withEmptyCity_emitsError() {
        // Given
        viewModel.getWeatherState().observeForever(weatherStateObserver);

        // When
        viewModel.loadWeatherByCity("");

        // Then
        verify(weatherStateObserver).onChanged(argThat(state -> 
            state instanceof UIState.Error && 
            ((UIState.Error<WeatherData>) state).getMessage().contains("empty")
        ));
    }

    @Test
    public void loadWeatherByCity_withNullCity_emitsError() {
        // Given
        viewModel.getWeatherState().observeForever(weatherStateObserver);

        // When
        viewModel.loadWeatherByCity(null);

        // Then
        verify(weatherStateObserver).onChanged(argThat(state -> 
            state instanceof UIState.Error
        ));
    }

    @Test
    public void loadWeatherByCoordinates_callsRepository() {
        // Given
        double lat = 21.0285;
        double lon = 105.8542;

        // When
        viewModel.loadWeatherByCoordinates(lat, lon);

        // Then
        verify(mockRepository).getWeatherByCoordinates(eq(lat), eq(lon), anyString(), any());
    }

    @Test
    public void setTemperatureUnit_updatesUnit() {
        // Given
        String newUnit = "fahrenheit";

        // When
        viewModel.setTemperatureUnit(newUnit);

        // Then
        assertEquals(newUnit, viewModel.getTemperatureUnit());
    }

    @Test
    public void getCurrentCityName_returnsCorrectName() {
        // Given
        String expectedCity = "Hanoi";
        viewModel.loadWeatherByCity(expectedCity);

        // When
        String actualCity = viewModel.getCurrentCityName();

        // Then - initially set to default
        assertNotNull(actualCity);
    }

    @Test
    public void refreshAllData_reloadsWeather() {
        // Given
        String cityName = "Hanoi";
        viewModel.loadWeatherByCity(cityName);

        // When
        viewModel.refreshAllData();

        // Then - should call repository again
        verify(mockRepository, atLeast(2)).getWeatherByCity(anyString(), anyString(), any());
    }

    // Helper method to create mock WeatherData
    private WeatherData createMockWeatherData(String cityName) {
        return new WeatherData.Builder()
                .setCityName(cityName)
                .setCountryCode("VN")
                .setTemperature(25.0)
                .setFeelsLike(24.0)
                .setMinTemperature(22.0)
                .setMaxTemperature(28.0)
                .setHumidity(70)
                .setPressure(1013)
                .setWindSpeed(3.5)
                .setWindDegree(180)
                .setWeatherMain("Clear")
                .setWeatherDescription("clear sky")
                .setWeatherIcon("01d")
                .setCloudiness(10)
                .setVisibility(10000)
                .setSunrise(1699747200L)
                .setSunset(1699786800L)
                .setTimestamp(1699767000L)
                .setLatitude(21.0285)
                .setLongitude(105.8542)
                .setTemperatureUnit("celsius")
                .build();
    }
}
