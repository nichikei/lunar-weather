package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OutfitSuggestionAdapter extends RecyclerView.Adapter<OutfitSuggestionAdapter.ViewHolder> {

    private List<OutfitSuggestion> suggestions;

    public OutfitSuggestionAdapter(List<OutfitSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutfitSuggestion suggestion = suggestions.get(position);
        holder.bind(suggestion);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void updateSuggestions(List<OutfitSuggestion> newSuggestions) {
        this.suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEmoji;
        private TextView tvCategory;
        private TextView tvSuggestion;
        private TextView tvReasoning;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
            tvReasoning = itemView.findViewById(R.id.tvReasoning);
        }

        public void bind(OutfitSuggestion suggestion) {
            tvEmoji.setText(suggestion.getEmoji());
            tvCategory.setText(suggestion.getCategory());
            tvSuggestion.setText(suggestion.getSuggestion());
            tvReasoning.setText(suggestion.getReasoning());
        }
    }
}

