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
        tvCategory.setText(suggestion.getCategory());
        tvSuggestion.setText(suggestion.getSuggestion());
        tvReasoning.setText(suggestion.getReasoning());
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

