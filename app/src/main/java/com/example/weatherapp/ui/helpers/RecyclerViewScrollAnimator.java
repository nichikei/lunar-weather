package com.example.weatherapp.ui.helpers;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView scroll listener to animate items as they enter the viewport
 */
public class RecyclerViewScrollAnimator extends RecyclerView.OnScrollListener {

    private int lastPosition = -1;
    private boolean isFirstLoad = true;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        
        // Animate visible items
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            int position = recyclerView.getChildAdapterPosition(child);
            
            if (position > lastPosition) {
                animateItem(child, i);
                lastPosition = position;
            }
        }
    }

    /**
     * Animate item with slide-in and fade-in effect
     */
    private void animateItem(View view, int position) {
        // Set initial state
        view.setAlpha(0f);
        view.setTranslationY(100f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
        
        // Animate with delay based on position for cascade effect
        long delay = isFirstLoad ? position * 80L : 0L;
        
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator(1.2f))
                .start();
    }

    /**
     * Reset animation state
     */
    public void resetAnimation() {
        lastPosition = -1;
        isFirstLoad = true;
    }

    /**
     * Set first load flag to false after initial load
     */
    public void onInitialLoadComplete() {
        isFirstLoad = false;
    }
}
