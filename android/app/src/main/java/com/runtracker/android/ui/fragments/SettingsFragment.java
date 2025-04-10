package com.runtracker.android.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.runtracker.android.R;
import com.runtracker.android.utils.Constants;

/**
 * Fragment for user settings
 */
public class SettingsFragment extends Fragment {

    // UI Elements
    private TextInputEditText etName;
    private TextInputEditText etWeight;
    private TextInputEditText etHeightCm;
    private TextInputEditText etAge;
    private RadioGroup rbgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private RadioGroup rbgDistanceUnit;
    private RadioButton rbKilometers;
    private RadioButton rbMiles;
    private RadioGroup rbgWeightUnit;
    private RadioButton rbKilograms;
    private RadioButton rbPounds;
    private SwitchMaterial switchAudioCues;
    private SwitchMaterial switchMilestoneAlerts;
    private MaterialButton btnSaveSettings;

    // Shared Preferences
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Initialize shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Load current settings
        loadSettings();

        // Set up save button
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    /**
     * Initialize all views
     */
    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etWeight = view.findViewById(R.id.etWeight);
        etHeightCm = view.findViewById(R.id.etHeightCm);
        etAge = view.findViewById(R.id.etAge);
        rbgGender = view.findViewById(R.id.rbgGender);
        rbMale = view.findViewById(R.id.rbMale);
        rbFemale = view.findViewById(R.id.rbFemale);
        rbgDistanceUnit = view.findViewById(R.id.rbgDistanceUnit);
        rbKilometers = view.findViewById(R.id.rbKilometers);
        rbMiles = view.findViewById(R.id.rbMiles);
        rbgWeightUnit = view.findViewById(R.id.rbgWeightUnit);
        rbKilograms = view.findViewById(R.id.rbKilograms);
        rbPounds = view.findViewById(R.id.rbPounds);
        switchAudioCues = view.findViewById(R.id.switchAudioCues);
        switchMilestoneAlerts = view.findViewById(R.id.switchMilestoneAlerts);
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings);
    }

    /**
     * Load current settings from shared preferences
     */
    private void loadSettings() {
        // Personal info
        etName.setText(sharedPreferences.getString(Constants.PREF_USER_NAME, ""));
        etWeight.setText(String.valueOf(sharedPreferences.getFloat(Constants.PREF_USER_WEIGHT, 70.0f)));
        etHeightCm.setText(String.valueOf(sharedPreferences.getInt(Constants.PREF_USER_HEIGHT, 170)));
        etAge.setText(String.valueOf(sharedPreferences.getInt(Constants.PREF_USER_AGE, 30)));

        // Gender
        if (sharedPreferences.getString(Constants.PREF_USER_GENDER, Constants.GENDER_MALE).equals(Constants.GENDER_MALE)) {
            rbMale.setChecked(true);
        } else {
            rbFemale.setChecked(true);
        }

        // Units
        if (sharedPreferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM).equals(Constants.UNIT_KM)) {
            rbKilometers.setChecked(true);
        } else {
            rbMiles.setChecked(true);
        }

        if (sharedPreferences.getString(Constants.PREF_WEIGHT_UNIT, Constants.UNIT_KG).equals(Constants.UNIT_KG)) {
            rbKilograms.setChecked(true);
        } else {
            rbPounds.setChecked(true);
        }

        // Notifications
        switchAudioCues.setChecked(sharedPreferences.getBoolean(Constants.PREF_AUDIO_CUES, true));
        switchMilestoneAlerts.setChecked(sharedPreferences.getBoolean(Constants.PREF_MILESTONE_ALERTS, true));
    }

    /**
     * Save settings to shared preferences
     */
    private void saveSettings() {
        // Validate inputs
        if (validateInputs()) {
            // Get editor
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Save personal info
            editor.putString(Constants.PREF_USER_NAME, etName.getText().toString().trim());
            editor.putFloat(Constants.PREF_USER_WEIGHT, Float.parseFloat(etWeight.getText().toString()));
            editor.putInt(Constants.PREF_USER_HEIGHT, Integer.parseInt(etHeightCm.getText().toString()));
            editor.putInt(Constants.PREF_USER_AGE, Integer.parseInt(etAge.getText().toString()));

            // Save gender
            editor.putString(Constants.PREF_USER_GENDER, 
                rbMale.isChecked() ? Constants.GENDER_MALE : Constants.GENDER_FEMALE);

            // Save units
            editor.putString(Constants.PREF_DISTANCE_UNIT, 
                rbKilometers.isChecked() ? Constants.UNIT_KM : Constants.UNIT_MILES);
            editor.putString(Constants.PREF_WEIGHT_UNIT, 
                rbKilograms.isChecked() ? Constants.UNIT_KG : Constants.UNIT_LB);

            // Save notification settings
            editor.putBoolean(Constants.PREF_AUDIO_CUES, switchAudioCues.isChecked());
            editor.putBoolean(Constants.PREF_MILESTONE_ALERTS, switchMilestoneAlerts.isChecked());

            // Apply changes
            editor.apply();

            // Show success message
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate user inputs
     * @return true if all inputs are valid
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Name can be empty, no validation needed

        // Weight validation
        if (etWeight.getText().toString().isEmpty()) {
            etWeight.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            try {
                float weight = Float.parseFloat(etWeight.getText().toString());
                if (weight <= 0 || weight > 500) {
                    etWeight.setError(getString(R.string.error_invalid_weight));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etWeight.setError(getString(R.string.error_invalid_number));
                isValid = false;
            }
        }

        // Height validation
        if (etHeightCm.getText().toString().isEmpty()) {
            etHeightCm.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            try {
                int height = Integer.parseInt(etHeightCm.getText().toString());
                if (height <= 0 || height > 300) {
                    etHeightCm.setError(getString(R.string.error_invalid_height));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etHeightCm.setError(getString(R.string.error_invalid_number));
                isValid = false;
            }
        }

        // Age validation
        if (etAge.getText().toString().isEmpty()) {
            etAge.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(etAge.getText().toString());
                if (age <= 0 || age > 120) {
                    etAge.setError(getString(R.string.error_invalid_age));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etAge.setError(getString(R.string.error_invalid_number));
                isValid = false;
            }
        }

        return isValid;
    }
}