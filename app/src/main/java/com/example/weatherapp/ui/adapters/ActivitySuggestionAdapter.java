package com.example.weatherapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.ActivitySuggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying Activity Suggestions in RecyclerView
 */
public class ActivitySuggestionAdapter extends RecyclerView.Adapter<ActivitySuggestionAdapter.ViewHolder> {
    
    private List<ActivitySuggestion> suggestions;
    private Context context;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(ActivitySuggestion suggestion);
        void onAddToCalendarClick(ActivitySuggestion suggestion);
    }

    public ActivitySuggestionAdapter(Context context, OnActivityClickListener listener) {
        this.context = context;
        this.suggestions = new ArrayList<>();
        this.listener = listener;
    }

    public void setSuggestions(List<ActivitySuggestion> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
            R.layout.item_activity_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivitySuggestion suggestion = suggestions.get(position);
        holder.bind(suggestion);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        View iconContainer;
        TextView iconText;
        TextView titleText;
        TextView descriptionText;
        TextView categoryText;
        TextView scoreText;
        TextView reasonText;
        TextView bestTimeText;
        TextView calendarButton;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardActivity);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            iconText = itemView.findViewById(R.id.txtActivityIcon);
            titleText = itemView.findViewById(R.id.txtActivityTitle);
            descriptionText = itemView.findViewById(R.id.txtActivityDescription);
            categoryText = itemView.findViewById(R.id.txtActivityCategory);
            scoreText = itemView.findViewById(R.id.txtSuitabilityScore);
            reasonText = itemView.findViewById(R.id.txtActivityReason);
            bestTimeText = itemView.findViewById(R.id.txtBestTime);
            calendarButton = itemView.findViewById(R.id.btnAddToCalendar);
        }

        void bind(ActivitySuggestion suggestion) {
            // Icon
            iconText.setText(suggestion.getIcon());
            
            // Set icon background color based on category
            iconContainer.setBackgroundResource(getCategoryBackgroundDrawable(suggestion.getCategory()));
            
            // Title
            titleText.setText(suggestion.getTitle());
            
            // Description
            descriptionText.setText(suggestion.getDescription());
            
            // Category
            categoryText.setText(suggestion.getCategoryIcon() + " " + suggestion.getCategory().toUpperCase());
            
            // Suitability Score
            int score = suggestion.getSuitabilityScore();
            scoreText.setText(score + "%");
            
            // Reason
            reasonText.setText("💡 " + suggestion.getReason());
            
            // Best Time
            bestTimeText.setText("⏰ Best time: " + suggestion.getBestTime());
            
            // Calendar button
            if (suggestion.isCalendarSyncable()) {
                calendarButton.setVisibility(View.VISIBLE);
                calendarButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddToCalendarClick(suggestion);
                    }
                });
            } else {
                calendarButton.setVisibility(View.GONE);
            }
            
            // Card click
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityClick(suggestion);
                }
            });
        }
        
        /**
         * Get background drawable resource based on category
         */
        private int getCategoryBackgroundDrawable(String category) {
            String categoryLower = category.toLowerCase();
            
            // Outdoor activities → Green
            if (categoryLower.contains("outdoor") || categoryLower.contains("sport") || 
                categoryLower.contains("exercise") || categoryLower.contains("fitness")) {
                return R.drawable.icon_background_green;
            }
            // Indoor activities → Blue
            else if (categoryLower.contains("indoor") || categoryLower.contains("entertainment") || 
                     categoryLower.contains("leisure")) {
                return R.drawable.icon_background_blue;
            }
            // Social activities → Purple
            else if (categoryLower.contains("social") || categoryLower.contains("dining") || 
                     categoryLower.contains("food")) {
                return R.drawable.icon_background_purple;
            }
            // Creative activities → Orange
            else if (categoryLower.contains("creative") || categoryLower.contains("hobby") || 
                     categoryLower.contains("art")) {
                return R.drawable.icon_background_orange;
            }
            // Default → Red
            else {
                return R.drawable.icon_background_red;
            }
        }
    }
}
