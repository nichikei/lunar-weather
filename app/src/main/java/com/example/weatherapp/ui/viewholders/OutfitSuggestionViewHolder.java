package com.example.weatherapp.ui.viewholders;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import com.example.weatherapp.data.models.OutfitSuggestion;

/**
 * ViewHolder cho Outfit Suggestion items
 * Tách biệt khỏi Adapter để code sạch hơn và dễ maintain
 */
public class OutfitSuggestionViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvEmoji;
    private final TextView tvCategory;
    private final TextView tvSuggestion;
    private final TextView tvReasoning;
    private final View iconBackground;

    public OutfitSuggestionViewHolder(@NonNull View itemView) {
        super(itemView);
        tvEmoji = itemView.findViewById(R.id.tvEmoji);
        tvCategory = itemView.findViewById(R.id.tvCategory);
        tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
        tvReasoning = itemView.findViewById(R.id.tvReasoning);
        iconBackground = itemView.findViewById(R.id.iconBackground);
    }

    /**
     * Bind dữ liệu vào views
     */
    public void bind(OutfitSuggestion suggestion) {
        if (suggestion == null) return;

        tvEmoji.setText(suggestion.getEmoji());
        tvCategory.setText(suggestion.getCategory());
        tvSuggestion.setText(suggestion.getSuggestion());
        tvReasoning.setText(suggestion.getReasoning());
        
        // Set icon background color based on category (iOS style)
        int backgroundColor = getCategoryColor(suggestion.getCategory());
        if (iconBackground != null) {
            iconBackground.setBackgroundResource(getCategoryBackgroundDrawable(suggestion.getCategory()));
        }
    }
    
    /**
     * Get iOS-style color for category
     */
    private int getCategoryColor(String category) {
        String categoryLower = category.toLowerCase();
        
        if (categoryLower.contains("base") || categoryLower.contains("upper")) {
            return Color.parseColor("#007AFF"); // Blue
        } else if (categoryLower.contains("outer") || categoryLower.contains("jacket")) {
            return Color.parseColor("#5856D6"); // Purple
        } else if (categoryLower.contains("lower") || categoryLower.contains("bottom")) {
            return Color.parseColor("#FF9500"); // Orange
        } else if (categoryLower.contains("foot") || categoryLower.contains("shoe")) {
            return Color.parseColor("#34C759"); // Green
        } else if (categoryLower.contains("accessories")) {
            return Color.parseColor("#FF3B30"); // Red
        }
        
        return Color.parseColor("#007AFF"); // Default blue
    }
    
    /**
     * Get background drawable for category
     */
    private int getCategoryBackgroundDrawable(String category) {
        String categoryLower = category.toLowerCase();
        
        if (categoryLower.contains("base") || categoryLower.contains("upper")) {
            return R.drawable.icon_background_blue;
        } else if (categoryLower.contains("outer") || categoryLower.contains("jacket")) {
            return R.drawable.icon_background_purple;
        } else if (categoryLower.contains("lower") || categoryLower.contains("bottom")) {
            return R.drawable.icon_background_orange;
        } else if (categoryLower.contains("foot") || categoryLower.contains("shoe")) {
            return R.drawable.icon_background_green;
        } else if (categoryLower.contains("accessories")) {
            return R.drawable.icon_background_red;
        }
        
        return R.drawable.icon_background_blue; // Default
    }

    /**
     * Bind với click listener
     */
    public void bind(OutfitSuggestion suggestion, OnItemClickListener listener) {
        bind(suggestion);

        if (listener != null) {
            itemView.setOnClickListener(v -> listener.onItemClick(suggestion, getAdapterPosition()));
        }
    }

    /**
     * Interface cho item click events
     */
    public interface OnItemClickListener {
        void onItemClick(OutfitSuggestion suggestion, int position);
    }
}

