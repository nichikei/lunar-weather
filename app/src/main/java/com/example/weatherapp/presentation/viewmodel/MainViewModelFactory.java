package com.example.weatherapp.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherapp.domain.repository.WeatherRepository;

/**
 * Factory to create ViewModels with dependencies
 * Required because ViewModel needs constructor parameters
 */
public class MainViewModelFactory implements ViewModelProvider.Factory {
    private final WeatherRepository repository;
    
    public MainViewModelFactory(WeatherRepository repository) {
        this.repository = repository;
    }
    
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
