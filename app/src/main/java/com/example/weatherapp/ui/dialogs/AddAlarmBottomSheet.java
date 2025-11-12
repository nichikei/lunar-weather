package com.example.weatherapp.ui.dialogs;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.AlarmCondition;
import com.example.weatherapp.domain.model.AlarmType;
import com.example.weatherapp.domain.model.WeatherAlarm;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AddAlarmBottomSheet extends BottomSheetDialogFragment {

    private TextView tvSelectedTime;
    private Spinner spinnerAlarmType;
    private EditText etAlarmTitle;
    private Button btnSave;
    private LinearLayout layoutDaysOfWeek;
    private CheckBox[] dayCheckBoxes = new CheckBox[7];

    private int selectedHour = 7;
    private int selectedMinute = 0;
    
    private OnAlarmSavedListener listener;

    public interface OnAlarmSavedListener {
        void onAlarmSaved(WeatherAlarm alarm);
    }

    public static AddAlarmBottomSheet newInstance() {
        return new AddAlarmBottomSheet();
    }

    public void setListener(OnAlarmSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAlarmTypeSpinner();
        setupTimePickerDialog();
        setupDaysOfWeek();
        setupSaveButton();
    }

    private void initViews(View view) {
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        spinnerAlarmType = view.findViewById(R.id.spinnerAlarmType);
        etAlarmTitle = view.findViewById(R.id.etAlarmTitle);
        btnSave = view.findViewById(R.id.btnSave);
        layoutDaysOfWeek = view.findViewById(R.id.layoutDaysOfWeek);

        // Initialize day checkboxes
        dayCheckBoxes[0] = view.findViewById(R.id.cbMonday);
        dayCheckBoxes[1] = view.findViewById(R.id.cbTuesday);
        dayCheckBoxes[2] = view.findViewById(R.id.cbWednesday);
        dayCheckBoxes[3] = view.findViewById(R.id.cbThursday);
        dayCheckBoxes[4] = view.findViewById(R.id.cbFriday);
        dayCheckBoxes[5] = view.findViewById(R.id.cbSaturday);
        dayCheckBoxes[6] = view.findViewById(R.id.cbSunday);

        updateTimeDisplay();
    }

    private void setupAlarmTypeSpinner() {
        AlarmType[] types = AlarmType.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            typeNames[i] = types[i].getIcon() + " " + types[i].getDisplayName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                typeNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlarmType.setAdapter(adapter);
    }

    private void setupTimePickerDialog() {
        tvSelectedTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        updateTimeDisplay();
                    },
                    selectedHour,
                    selectedMinute,
                    false // 12-hour format
            );
            timePickerDialog.show();
        });
    }

    private void setupDaysOfWeek() {
        // Default: weekdays enabled
        for (int i = 0; i < 5; i++) {
            dayCheckBoxes[i].setChecked(true);
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String title = etAlarmTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter alarm title", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected days
            boolean[] selectedDays = new boolean[7];
            for (int i = 0; i < 7; i++) {
                selectedDays[i] = dayCheckBoxes[i].isChecked();
            }

            // Get selected alarm type
            int typeIndex = spinnerAlarmType.getSelectedItemPosition();
            AlarmType type = AlarmType.values()[typeIndex];

            // Create alarm condition based on type
            AlarmCondition condition = createConditionForType(type);

            // Create alarm
            WeatherAlarm alarm = new WeatherAlarm(title, selectedHour, selectedMinute, type, condition);
            alarm.setDaysOfWeek(selectedDays);
            alarm.setEnabled(true);

            if (listener != null) {
                listener.onAlarmSaved(alarm);
            }

            dismiss();
        });
    }

    private AlarmCondition createConditionForType(AlarmType type) {
        switch (type) {
            case WAKE_UP_EARLY:
                return AlarmCondition.forWakeUpEarly(AlarmCondition.WeatherConditionType.RAIN, 30); // 30 minutes early if rain
            case UMBRELLA_REMINDER:
                return AlarmCondition.forUmbrellaReminder(); // Rain check
            case UV_ALERT:
                return AlarmCondition.forUvAlert(8); // UV index > 8
            case AIR_QUALITY_ALERT:
                return AlarmCondition.forAirQualityAlert(150); // AQI > 150
            case ICY_ROADS_ALERT:
                return AlarmCondition.forIcyRoads(0.0); // Temp <= 0°C
            case TEMPERATURE_ALERT:
                return AlarmCondition.forTemperature(35.0, AlarmCondition.ComparisonOperator.GREATER_THAN); // > 35°C
            default:
                return AlarmCondition.forWakeUpEarly(AlarmCondition.WeatherConditionType.ANY_BAD_WEATHER, 30);
        }
    }

    private void updateTimeDisplay() {
        String amPm = selectedHour >= 12 ? "PM" : "AM";
        int displayHour = selectedHour % 12;
        if (displayHour == 0) displayHour = 12;
        String timeText = String.format("%d:%02d %s", displayHour, selectedMinute, amPm);
        tvSelectedTime.setText(timeText);
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
