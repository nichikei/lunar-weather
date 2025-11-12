package com.example.weatherapp.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

    public OutfitSuggestionViewHolder(@NonNull View itemView) {
        super(itemView);
        tvEmoji = itemView.findViewById(R.id.tvEmoji);
        tvCategory = itemView.findViewById(R.id.tvCategory);
        tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
        tvReasoning = itemView.findViewById(R.id.tvReasoning);
    }

    /**
     * Bind dữ liệu vào views
     */
    public void bind(OutfitSuggestion suggestion) {
        if (suggestion == null) return;

        tvEmoji.setText(suggestion.getEmoji());
        
        // Add priority badge to category
        String categoryWithPriority = suggestion.getPriorityBadge() + suggestion.getCategory();
        tvCategory.setText(categoryWithPriority);
        
        // Highlight suggestion based on priority
        tvSuggestion.setText(suggestion.getSuggestion());
        
        // Add fabric type to reasoning if available
        String reasoning = suggestion.getReasoning();
        if (suggestion.getFabricType() != null && !suggestion.getFabricType().isEmpty() 
            && !suggestion.getFabricType().equals("N/A")) {
            reasoning += "\n🧵 Fabric: " + suggestion.getFabricType();
        }
        tvReasoning.setText(reasoning);
        
        // Style based on priority
        switch (suggestion.getPriority()) {
            case ESSENTIAL:
                tvSuggestion.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                tvCategory.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                break;
            case RECOMMENDED:
                tvSuggestion.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                tvCategory.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                break;
            case OPTIONAL:
                tvSuggestion.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                tvCategory.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                break;
        }
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

