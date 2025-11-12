package com.example.weatherapp.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        CardView cardView;
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
            
            // Title
            titleText.setText(suggestion.getTitle());
            
            // Description
            descriptionText.setText(suggestion.getDescription());
            
            // Category
            categoryText.setText(suggestion.getCategoryIcon() + " " + suggestion.getCategory().toUpperCase());
            
            // Suitability Score
            int score = suggestion.getSuitabilityScore();
            scoreText.setText(score + "%");
            try {
                scoreText.setTextColor(Color.parseColor(suggestion.getScoreColor()));
            } catch (Exception e) {
                scoreText.setTextColor(Color.parseColor("#4CAF50"));
            }
            
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
            
            // Card color based on score
            if (score >= 80) {
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            } else if (score >= 60) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Light orange
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#FAFAFA")); // Light gray
            }
        }
    }
}
