package com.example.weatherapp.ui.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom ItemAnimator for RecyclerView with slide-in and fade-in effects
 */
public class SlideInItemAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        // Prepare the initial state
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(200f);
        holder.itemView.setTranslationX(50f);
        
        // Animate to final state
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .translationX(0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dispatchAddFinished(holder);
                    }
                })
                .start();
        
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        holder.itemView.animate()
                .alpha(0f)
                .translationX(-100f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dispatchRemoveFinished(holder);
                    }
                })
                .start();
        
        return true;
    }
}
