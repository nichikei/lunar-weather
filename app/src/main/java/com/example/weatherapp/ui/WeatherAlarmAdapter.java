package com.example.weatherapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.WeatherAlarm;

import java.util.ArrayList;
import java.util.List;

public class WeatherAlarmAdapter extends RecyclerView.Adapter<WeatherAlarmAdapter.AlarmViewHolder> {

    private List<WeatherAlarm> alarms = new ArrayList<>();
    private OnAlarmInteractionListener listener;

    public interface OnAlarmInteractionListener {
        void onAlarmToggled(WeatherAlarm alarm, boolean enabled);
        void onAlarmDeleted(WeatherAlarm alarm);
        void onAlarmClicked(WeatherAlarm alarm);
    }

    public WeatherAlarmAdapter(OnAlarmInteractionListener listener) {
        this.listener = listener;
    }

    public void setAlarms(List<WeatherAlarm> alarms) {
        this.alarms = alarms != null ? alarms : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        WeatherAlarm alarm = alarms.get(position);
        holder.bind(alarm);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView tvIcon, tvTime, tvTitle, tvDescription, tvDays;
        private SwitchCompat switchEnabled;
        private ImageButton btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvAlarmIcon);
            tvTime = itemView.findViewById(R.id.tvAlarmTime);
            tvTitle = itemView.findViewById(R.id.tvAlarmTitle);
            tvDescription = itemView.findViewById(R.id.tvAlarmDescription);
            tvDays = itemView.findViewById(R.id.tvAlarmDays);
            switchEnabled = itemView.findViewById(R.id.switchAlarmEnabled);
            btnDelete = itemView.findViewById(R.id.btnDeleteAlarm);
        }

        public void bind(WeatherAlarm alarm) {
            // Icon
            tvIcon.setText(alarm.getType().getIcon());

            // Time
            tvTime.setText(alarm.getFormattedTime());

            // Title
            tvTitle.setText(alarm.getTitle());

            // Description
            tvDescription.setText(alarm.getDescription());

            // Days of week
            String daysText = formatDaysOfWeek(alarm.getDaysOfWeek());
            tvDays.setText(daysText);
            tvDays.setVisibility(daysText.isEmpty() ? View.GONE : View.VISIBLE);

            // Enable switch
            switchEnabled.setOnCheckedChangeListener(null); // Remove listener to avoid triggering
            switchEnabled.setChecked(alarm.isEnabled());
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onAlarmToggled(alarm, isChecked);
                }
            });

            // Delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlarmDeleted(alarm);
                }
            });

            // Click item to edit
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlarmClicked(alarm);
                }
            });
        }

        private String formatDaysOfWeek(boolean[] days) {
            if (days == null || days.length != 7) {
                return "One time";
            }

            // Check if all days are selected
            boolean allDays = true;
            boolean noDays = true;
            for (boolean day : days) {
                if (!day) allDays = false;
                if (day) noDays = false;
            }

            if (noDays) {
                return "One time";
            }

            if (allDays) {
                return "Every day";
            }

            // Check for weekdays (Mon-Fri)
            if (days[0] && days[1] && days[2] && days[3] && days[4] && !days[5] && !days[6]) {
                return "Weekdays";
            }

            // Check for weekends
            if (!days[0] && !days[1] && !days[2] && !days[3] && !days[4] && days[5] && days[6]) {
                return "Weekends";
            }

            // Build custom list
            String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            List<String> selectedDays = new ArrayList<>();
            for (int i = 0; i < days.length; i++) {
                if (days[i]) {
                    selectedDays.add(dayNames[i]);
                }
            }

            return String.join(", ", selectedDays);
        }
    }
}
