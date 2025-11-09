package com.example.weatherapp.ui.base;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherapp.utils.LocaleHelper;

/**
 * Base Activity cho tất cả các Activity trong app
 * Xử lý các logic chung như: locale, analytics, error handling
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Phương thức abstract để các Activity con cung cấp layout ID
     */
    protected abstract int getLayoutId();

    /**
     * Phương thức abstract để khởi tạo views
     */
    protected abstract void initViews();

    /**
     * Phương thức abstract để setup listeners
     */
    protected abstract void setupListeners();

    /**
     * Phương thức tùy chọn để setup observers (LiveData, ViewModel)
     */
    protected void setupObservers() {
        // Override nếu cần
    }

    /**
     * Phương thức tùy chọn để load dữ liệu ban đầu
     */
    protected void loadInitialData() {
        // Override nếu cần
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            setContentView(layoutId);
        }

        // Khởi tạo views
        initViews();

        // Setup listeners
        setupListeners();

        // Setup observers (nếu có)
        setupObservers();

        // Load dữ liệu ban đầu
        loadInitialData();
    }

    /**
     * Hiển thị loading dialog/progress
     */
    protected void showLoading() {
        // Implement loading UI
    }

    /**
     * Ẩn loading dialog/progress
     */
    protected void hideLoading() {
        // Implement hide loading
    }

    /**
     * Hiển thị error message
     */
    protected void showError(String message) {
        // Implement error UI
    }

    /**
     * Kiểm tra network connection
     */
    protected boolean isNetworkAvailable() {
        // Implement network check
        return true;
    }
}

