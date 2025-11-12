package com.example.weatherapp.ui.helpers;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.ui.activities.ChartsActivity;
import com.example.weatherapp.ui.activities.MainActivity;
import com.example.weatherapp.ui.activities.OutfitSuggestionActivity;
import com.example.weatherapp.ui.activities.SearchActivity;
import com.example.weatherapp.ui.activities.SettingsActivity;

/**
 * Helper class for UI setup and event listeners
 * Handles button clicks, UI effects, and view animations
 */
public class UISetupHelper {
    private final MainActivity activity;
    private final ActivityMainBinding binding;
    
    // Callbacks for actions
    public interface UIActionCallbacks {
        void onSearchRequested();
        void onLocationRequested();
        void onToggleFavorite();
        void onViewChartsRequested();
        void onWeatherMapsRequested();
        void onOutfitSuggestionRequested();
        void onActivitySuggestionsRequested(); // NEW
        void onMoreFeaturesRequested(); // NEW - Opens bottom sheet
        void onFavoritesListRequested();
        void onTabChanged(boolean isHourly);
    }
    
    public UISetupHelper(MainActivity activity, ActivityMainBinding binding) {
        this.activity = activity;
        this.binding = binding;
    }
    
    /**
     * Setup all button listeners
     */
    public void setupListeners(
            ActivityResultLauncher<Intent> settingsLauncher,
            ActivityResultLauncher<Intent> searchLauncher,
            UIActionCallbacks callbacks) {
        
        // Top Bar - Settings Icon
        binding.btnSettingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(activity, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        // Top Bar - Search Icon
        binding.btnSearchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(activity, SearchActivity.class);
            searchLauncher.launch(intent);
        });

        // Top Bar City Name - tap to search
        binding.tvTopBarCityName.setOnClickListener(v -> {
            Intent intent = new Intent(activity, SearchActivity.class);
            searchLauncher.launch(intent);
        });

        // Top Bar Favorites Icon
        if (binding.btnFavoritesIcon != null) {
            binding.btnFavoritesIcon.setOnClickListener(v -> {
                callbacks.onFavoritesListRequested();
            });
        }

        // FAB Add to Favorites Button
        if (binding.fabAddToFavorites != null) {
            binding.fabAddToFavorites.setOnClickListener(v -> {
                callbacks.onToggleFavorite();
            });
        }

        // Search button
        binding.btnSearch.setOnClickListener(v -> {
            callbacks.onSearchRequested();
        });

        // Current Location button
        binding.btnCurrentLocation.setOnClickListener(v -> {
            callbacks.onLocationRequested();
        });

        // More Features button - Opens bottom sheet with Charts, Maps, AI Suggestions
        if (binding.btnMoreFeatures != null) {
            binding.btnMoreFeatures.setOnClickListener(v -> {
                callbacks.onMoreFeaturesRequested();
            });
        }

        // Hourly/Weekly toggle buttons
        binding.btnHourly.setOnClickListener(v -> {
            callbacks.onTabChanged(true);
        });

        binding.btnWeekly.setOnClickListener(v -> {
            callbacks.onTabChanged(false);
        });
    }
    
    /**
     * Toggle search bar visibility
     */
    public void toggleSearchBar(boolean isVisible) {
        binding.searchContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Apply glass morphism effects to UI elements
     */
    public void applyGlassMorphismEffects() {
        // Glass morphism is handled by background drawables
        // No additional blur effects needed
    }
    
    /**
     * Apply blur effect to top bar
     */
    public void applyTopBarBlurEffect() {
        View topGlassBar = activity.findViewById(R.id.topGlassBar);
        if (topGlassBar == null) return;

        float density = activity.getResources().getDisplayMetrics().density;
        float cornerRadiusPx = 32f * density;

        // Set outline for rounded corners
        topGlassBar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
        topGlassBar.setClipToOutline(true);

        // Create glass morphism background
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(android.graphics.Color.argb(40, 255, 255, 255));
        background.setCornerRadius(cornerRadiusPx);
        background.setStroke((int)(1.5f * density), android.graphics.Color.argb(80, 255, 255, 255));
        topGlassBar.setBackground(background);

        // Set elevation
        topGlassBar.setElevation(12f * density);
    }
    
    /**
     * Animate tab selection indicator
     */
    public void animateTabSelection(boolean isHourly) {
        View indicator = binding.getRoot().findViewById(R.id.tabIndicator);
        if (indicator == null) return;

        indicator.animate().cancel();

        // Update text colors
        if (isHourly) {
            binding.btnHourly.setTextColor(activity.getResources().getColor(R.color.text_primary, null));
            binding.btnWeekly.setTextColor(activity.getResources().getColor(R.color.text_secondary, null));
        } else {
            binding.btnHourly.setTextColor(activity.getResources().getColor(R.color.text_secondary, null));
            binding.btnWeekly.setTextColor(activity.getResources().getColor(R.color.text_primary, null));
        }

        // Animate indicator position
        indicator.post(() -> {
            float targetX = isHourly ? 0 : ((View) indicator.getParent()).getWidth() - indicator.getWidth();
            indicator.animate()
                    .translationX(targetX)
                    .setDuration(250)
                    .start();
        });
    }
    
    /**
     * Show loading state
     */
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);
    }
    
    /**
     * Show weather details
     */
    public void showWeatherDetails() {
        binding.progressBar.setVisibility(View.GONE);
        binding.weatherDetailsSection.setVisibility(View.VISIBLE);
        binding.tvError.setVisibility(View.GONE);
    }
    
    /**
     * Show error message
     */
    public void showError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Add press animation to a view (scale down on press)
     */
    public static void addPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // Scale down with bounce
                    v.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .setDuration(100)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // Scale back to normal with overshoot
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
                            .start();
                    break;
            }
            return false; // Allow click to proceed
        });
    }
}
