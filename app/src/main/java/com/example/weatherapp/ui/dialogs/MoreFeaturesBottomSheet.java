package com.example.weatherapp.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

/**
 * Bottom Sheet for "More Features" menu
 * Clean UI with categorized options
 */
public class MoreFeaturesBottomSheet extends BottomSheetDialogFragment {

    private MoreFeaturesListener listener;

    public interface MoreFeaturesListener {
        void onWeatherMapsClicked();
        void onOutfitSuggestionsClicked();
        void onActivitySuggestionsClicked();
    }

    public static MoreFeaturesBottomSheet newInstance() {
        return new MoreFeaturesBottomSheet();
    }

    public void setListener(MoreFeaturesListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_more_features, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Animate cards on appear
        animateCards(view);

        // Set up click listeners
        MaterialCardView cardWeatherMaps = view.findViewById(R.id.cardWeatherMaps);
        MaterialCardView cardOutfitSuggestions = view.findViewById(R.id.cardOutfitSuggestions);
        MaterialCardView cardActivitySuggestions = view.findViewById(R.id.cardActivitySuggestions);

        cardWeatherMaps.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWeatherMapsClicked();
            }
            dismiss();
        });

        cardOutfitSuggestions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOutfitSuggestionsClicked();
            }
            dismiss();
        });

        cardActivitySuggestions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivitySuggestionsClicked();
            }
            dismiss();
        });
    }

    /**
     * Animate cards with stagger effect
     */
    private void animateCards(View view) {
        MaterialCardView[] cards = {
                view.findViewById(R.id.cardWeatherMaps),
                view.findViewById(R.id.cardOutfitSuggestions),
                view.findViewById(R.id.cardActivitySuggestions)
        };

        Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_up);

        for (int i = 0; i < cards.length; i++) {
            final MaterialCardView card = cards[i];
            final int delay = i * 50; // Stagger delay

            card.setAlpha(0f);
            card.setTranslationY(30f);

            card.postDelayed(() -> {
                card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }, delay);
        }
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
