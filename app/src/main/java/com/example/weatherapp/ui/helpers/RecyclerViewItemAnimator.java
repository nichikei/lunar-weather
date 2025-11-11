package com.example.weatherapp.ui.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemAnimator for RecyclerView that creates smooth slide-in and fade-in effects
 * when items scroll into view
 */
public class RecyclerViewItemAnimator {

    private static final int ANIMATION_DURATION = 400;
    private static final float SLIDE_FROM_BOTTOM_DISTANCE = 100f;
    private static final float SLIDE_FROM_RIGHT_DISTANCE = 80f;
    
    private int lastPosition = -1;
    private boolean isFirstLoad = true;

    /**
     * Animate item with slide from bottom and fade in
     */
    public void animateSlideFromBottom(@NonNull View view, int position) {
        // Only animate if this item hasn't been animated yet
        if (position > lastPosition) {
            lastPosition = position;

            // Set initial state
            view.setAlpha(0f);
            view.setTranslationY(SLIDE_FROM_BOTTOM_DISTANCE);

            // Animate
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }
    }

    /**
     * Animate item with slide from right and fade in
     */
    public void animateSlideFromRight(@NonNull View view, int position) {
        if (position > lastPosition) {
            lastPosition = position;

            view.setAlpha(0f);
            view.setTranslationX(SLIDE_FROM_RIGHT_DISTANCE);

            view.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator(1.3f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }
    }

    /**
     * Animate item with scale and fade effect
     */
    public void animateScaleIn(@NonNull View view, int position) {
        if (position > lastPosition) {
            lastPosition = position;

            view.setAlpha(0f);
            view.setScaleX(0.7f);
            view.setScaleY(0.7f);

            view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new OvershootInterpolator(0.5f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }
    }

    /**
     * Animate item with combined slide and scale effect
     */
    public void animateSlideAndScale(@NonNull View view, int position) {
        if (position > lastPosition) {
            lastPosition = position;

            view.setAlpha(0f);
            view.setTranslationY(SLIDE_FROM_BOTTOM_DISTANCE);
            view.setScaleX(0.9f);
            view.setScaleY(0.9f);

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }
    }

    /**
     * Animate with staggered delay based on position
     */
    public void animateWithDelay(@NonNull View view, int position, long delayPerItem) {
        if (position > lastPosition) {
            lastPosition = position;

            view.setAlpha(0f);
            view.setTranslationY(SLIDE_FROM_BOTTOM_DISTANCE);

            long delay = position * delayPerItem;

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }
    }

    /**
     * Flip animation effect
     */
    public void animateFlipIn(@NonNull View view, int position) {
        if (position > lastPosition) {
            lastPosition = position;

            view.setAlpha(0f);
            view.setRotationY(-90f);

            view.animate()
                    .alpha(1f)
                    .rotationY(0f)
                    .setDuration(ANIMATION_DURATION + 100)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }
    }

    /**
     * Reset animator to replay animations
     */
    public void reset() {
        lastPosition = -1;
    }

    /**
     * Set whether this is the first load (for initial animation of all items)
     */
    public void setFirstLoad(boolean firstLoad) {
        isFirstLoad = firstLoad;
        if (firstLoad) {
            lastPosition = -1;
        }
    }

    /**
     * Clear all animations on view
     */
    public static void clearAnimation(@NonNull View view) {
        view.clearAnimation();
        view.animate().cancel();
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setRotation(0f);
        view.setRotationX(0f);
        view.setRotationY(0f);
    }

    /**
     * RecyclerView scroll listener to handle animations
     */
    public static class AnimationScrollListener extends RecyclerView.OnScrollListener {
        
        private final RecyclerViewItemAnimator animator;
        private final AnimationType animationType;

        public enum AnimationType {
            SLIDE_BOTTOM,
            SLIDE_RIGHT,
            SCALE,
            SLIDE_SCALE,
            FLIP
        }

        public AnimationScrollListener(AnimationType type) {
            this.animator = new RecyclerViewItemAnimator();
            this.animationType = type;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            
            // Animate visible items
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                int position = recyclerView.getChildAdapterPosition(child);
                
                if (position != RecyclerView.NO_POSITION) {
                    animateView(child, position);
                }
            }
        }

        private void animateView(View view, int position) {
            switch (animationType) {
                case SLIDE_BOTTOM:
                    animator.animateSlideFromBottom(view, position);
                    break;
                case SLIDE_RIGHT:
                    animator.animateSlideFromRight(view, position);
                    break;
                case SCALE:
                    animator.animateScaleIn(view, position);
                    break;
                case SLIDE_SCALE:
                    animator.animateSlideAndScale(view, position);
                    break;
                case FLIP:
                    animator.animateFlipIn(view, position);
                    break;
            }
        }

        public void reset() {
            animator.reset();
        }
    }
}
