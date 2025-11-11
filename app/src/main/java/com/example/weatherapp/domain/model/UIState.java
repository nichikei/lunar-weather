package com.example.weatherapp.domain.model;

/**
 * Generic UI State wrapper for async operations
 * Handles Loading, Success, and Error states
 */
public class UIState<T> {
    
    private final Status status;
    private final T data;
    private final String errorMessage;
    
    private UIState(Status status, T data, String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }
    
    public static <T> UIState<T> loading() {
        return new UIState<>(Status.LOADING, null, null);
    }
    
    public static <T> UIState<T> success(T data) {
        return new UIState<>(Status.SUCCESS, data, null);
    }
    
    public static <T> UIState<T> error(String errorMessage) {
        return new UIState<>(Status.ERROR, null, errorMessage);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public T getData() {
        return data;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isLoading() {
        return status == Status.LOADING;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isError() {
        return status == Status.ERROR;
    }
    
    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }
}
