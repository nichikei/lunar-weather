package com.example.weatherapp.presentation.state;

/**
 * Base sealed class for representing UI states
 * Follows MVVM pattern to manage UI state changes
 * 
 * @param <T> The type of data held by the success state
 */
public abstract class UIState<T> {
    
    /**
     * Idle state - initial state before any action
     */
    public static final class Idle<T> extends UIState<T> {
        public Idle() {}
    }
    
    /**
     * Loading state - when data is being fetched
     */
    public static final class Loading<T> extends UIState<T> {
        public Loading() {}
    }
    
    /**
     * Success state - when data is successfully fetched
     */
    public static final class Success<T> extends UIState<T> {
        private final T data;
        
        public Success(T data) {
            this.data = data;
        }
        
        public T getData() {
            return data;
        }
    }
    
    /**
     * Error state - when an error occurs
     */
    public static final class Error<T> extends UIState<T> {
        private final String message;
        private final Throwable throwable;
        
        public Error(String message) {
            this(message, null);
        }
        
        public Error(String message, Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Throwable getThrowable() {
            return throwable;
        }
    }
    
    // Prevent external instantiation
    private UIState() {}
}
