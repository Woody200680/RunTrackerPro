package com.runtracker.android.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.runtracker.android.R;
import com.runtracker.android.data.CoachingManager;
import com.runtracker.android.data.models.CoachingPlan;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for configuring voice coaching settings
 */
public class CoachingSettingsFragment extends Fragment {

    private SwitchMaterial switchCoachingEnabled;
    private RadioGroup rgCoachingType;
    private RadioButton rbBasicCoaching;
    private RadioButton rbWorkoutCoaching;
    private RadioGroup rgCoachingVoice;
    private RadioButton rbMaleVoice;
    private RadioButton rbFemaleVoice;
    private RadioGroup rgCoachingFrequency;
    private RadioButton rbLowFrequency;
    private RadioButton rbMediumFrequency;
    private RadioButton rbHighFrequency;
    private SwitchMaterial switchMotivational;
    private Spinner spinnerCoachingPlan;
    private TextView tvSelectedPlanName;
    private TextView tvSelectedPlanDescription;
    private TextView tvSelectedPlanDetails;
    private Button btnViewWorkouts;
    private Button btnSaveCoachingSettings;
    
    // Section labels
    private TextView tvCoachingVoiceLabel;
    private TextView tvCoachingFrequencyLabel;
    private TextView tvCoachingPlanLabel;
    private View cardPlanDetails;

    private SharedPreferences preferences;
    private CoachingManager coachingManager;
    private List<CoachingPlan> coachingPlans;
    private ArrayAdapter<String> planAdapter;
    private CoachingPlan selectedPlan;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coaching_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Get dependencies
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        coachingManager = ((MainActivity) requireActivity()).getCoachingManager();

        // Load coaching plans
        loadCoachingPlans();

        // Load current settings
        loadSettings();

        // Set up listeners
        setupListeners();
    }

    /**
     * Initialize all views
     * @param view Root view
     */
    private void initViews(View view) {
        switchCoachingEnabled = view.findViewById(R.id.switchCoachingEnabled);
        rgCoachingType = view.findViewById(R.id.rgCoachingType);
        rbBasicCoaching = view.findViewById(R.id.rbBasicCoaching);
        rbWorkoutCoaching = view.findViewById(R.id.rbWorkoutCoaching);
        rgCoachingVoice = view.findViewById(R.id.rgCoachingVoice);
        rbMaleVoice = view.findViewById(R.id.rbMaleVoice);
        rbFemaleVoice = view.findViewById(R.id.rbFemaleVoice);
        rgCoachingFrequency = view.findViewById(R.id.rgCoachingFrequency);
        rbLowFrequency = view.findViewById(R.id.rbLowFrequency);
        rbMediumFrequency = view.findViewById(R.id.rbMediumFrequency);
        rbHighFrequency = view.findViewById(R.id.rbHighFrequency);
        switchMotivational = view.findViewById(R.id.switchMotivational);
        spinnerCoachingPlan = view.findViewById(R.id.spinnerCoachingPlan);
        tvSelectedPlanName = view.findViewById(R.id.tvSelectedPlanName);
        tvSelectedPlanDescription = view.findViewById(R.id.tvSelectedPlanDescription);
        tvSelectedPlanDetails = view.findViewById(R.id.tvSelectedPlanDetails);
        btnViewWorkouts = view.findViewById(R.id.btnViewWorkouts);
        btnSaveCoachingSettings = view.findViewById(R.id.btnSaveCoachingSettings);
        
        // Section labels
        tvCoachingVoiceLabel = view.findViewById(R.id.tvCoachingVoiceLabel);
        tvCoachingFrequencyLabel = view.findViewById(R.id.tvCoachingFrequencyLabel);
        tvCoachingPlanLabel = view.findViewById(R.id.tvCoachingPlanLabel);
        cardPlanDetails = view.findViewById(R.id.cardPlanDetails);
    }

    /**
     * Load available coaching plans
     */
    private void loadCoachingPlans() {
        // Get all coaching plans
        coachingPlans = coachingManager.getAllPlans();

        // Create list of plan names for spinner
        List<String> planNames = new ArrayList<>();
        planNames.add(getString(R.string.select_a_plan));

        for (CoachingPlan plan : coachingPlans) {
            planNames.add(plan.getName());
        }

        // Create adapter for spinner
        planAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                planNames
        );
        planAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
        spinnerCoachingPlan.setAdapter(planAdapter);
    }

    /**
     * Load current settings from preferences
     */
    private void loadSettings() {
        // Load coaching enabled
        boolean coachingEnabled = preferences.getBoolean(Constants.PREF_COACHING_ENABLED, true);
        switchCoachingEnabled.setChecked(coachingEnabled);
        updateSettingsVisibility(coachingEnabled);

        // Load coaching type
        int coachingType = preferences.getInt(Constants.PREF_COACHING_TYPE, Constants.COACHING_TYPE_BASIC);
        switch (coachingType) {
            case Constants.COACHING_TYPE_WORKOUT:
                rbWorkoutCoaching.setChecked(true);
                break;
            case Constants.COACHING_TYPE_BASIC:
            default:
                rbBasicCoaching.setChecked(true);
                break;
        }

        // Load coaching voice
        String coachingVoice = preferences.getString(
                Constants.PREF_COACHING_VOICE,
                Constants.COACHING_VOICE_MALE
        );
        if (coachingVoice.equals(Constants.COACHING_VOICE_FEMALE)) {
            rbFemaleVoice.setChecked(true);
        } else {
            rbMaleVoice.setChecked(true);
        }

        // Load coaching frequency
        int coachingFrequency = preferences.getInt(
                Constants.PREF_COACHING_FREQUENCY,
                Constants.COACHING_FREQUENCY_MEDIUM
        );
        switch (coachingFrequency) {
            case Constants.COACHING_FREQUENCY_LOW:
                rbLowFrequency.setChecked(true);
                break;
            case Constants.COACHING_FREQUENCY_HIGH:
                rbHighFrequency.setChecked(true);
                break;
            case Constants.COACHING_FREQUENCY_MEDIUM:
            default:
                rbMediumFrequency.setChecked(true);
                break;
        }

        // Load motivational preference
        boolean motivational = preferences.getBoolean(Constants.PREF_COACHING_MOTIVATIONAL, true);
        switchMotivational.setChecked(motivational);

        // Load active plan
        String activePlanId = preferences.getString(Constants.PREF_ACTIVE_PLAN_ID, null);
        if (activePlanId != null) {
            // Find the plan in the list
            for (int i = 0; i < coachingPlans.size(); i++) {
                if (coachingPlans.get(i).getId().equals(activePlanId)) {
                    selectedPlan = coachingPlans.get(i);
                    // Add 1 because the first item is "Select a plan"
                    spinnerCoachingPlan.setSelection(i + 1);
                    updatePlanDetails(selectedPlan);
                    break;
                }
            }
        } else {
            // No active plan selected
            spinnerCoachingPlan.setSelection(0);
            clearPlanDetails();
        }
    }

    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Coaching enabled switch
        switchCoachingEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSettingsVisibility(isChecked);
        });

        // Coaching type radio group
        rgCoachingType.setOnCheckedChangeListener((group, checkedId) -> {
            // If workout coaching is selected, show plan details
            boolean isWorkoutSelected = (checkedId == R.id.rbWorkoutCoaching);
            spinnerCoachingPlan.setEnabled(isWorkoutSelected);
            btnViewWorkouts.setEnabled(isWorkoutSelected && selectedPlan != null);
        });

        // Plan spinner
        spinnerCoachingPlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "Select a plan" option
                    selectedPlan = null;
                    clearPlanDetails();
                } else {
                    // Adjust position because of "Select a plan" option
                    selectedPlan = coachingPlans.get(position - 1);
                    updatePlanDetails(selectedPlan);
                }
                
                // Update button state
                btnViewWorkouts.setEnabled(selectedPlan != null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPlan = null;
                clearPlanDetails();
                btnViewWorkouts.setEnabled(false);
            }
        });

        // View workouts button
        btnViewWorkouts.setOnClickListener(v -> {
            if (selectedPlan != null) {
                // Navigate to workouts fragment with plan ID
                CoachingSettingsFragmentDirections.ActionCoachingSettingsToWorkouts action =
                        CoachingSettingsFragmentDirections.actionCoachingSettingsToWorkouts(
                                selectedPlan.getId()
                        );
                Navigation.findNavController(requireView()).navigate(action);
            }
        });

        // Save button
        btnSaveCoachingSettings.setOnClickListener(v -> {
            saveSettings();
        });
    }

    /**
     * Save settings to preferences
     */
    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();

        // Save coaching enabled
        boolean coachingEnabled = switchCoachingEnabled.isChecked();
        editor.putBoolean(Constants.PREF_COACHING_ENABLED, coachingEnabled);

        // Save coaching type
        int coachingType = rbWorkoutCoaching.isChecked() ?
                Constants.COACHING_TYPE_WORKOUT : Constants.COACHING_TYPE_BASIC;
        editor.putInt(Constants.PREF_COACHING_TYPE, coachingType);

        // Save coaching voice
        String coachingVoice = rbFemaleVoice.isChecked() ?
                Constants.COACHING_VOICE_FEMALE : Constants.COACHING_VOICE_MALE;
        editor.putString(Constants.PREF_COACHING_VOICE, coachingVoice);

        // Save coaching frequency
        int coachingFrequency;
        if (rbLowFrequency.isChecked()) {
            coachingFrequency = Constants.COACHING_FREQUENCY_LOW;
        } else if (rbHighFrequency.isChecked()) {
            coachingFrequency = Constants.COACHING_FREQUENCY_HIGH;
        } else {
            coachingFrequency = Constants.COACHING_FREQUENCY_MEDIUM;
        }
        editor.putInt(Constants.PREF_COACHING_FREQUENCY, coachingFrequency);

        // Save motivational preference
        boolean motivational = switchMotivational.isChecked();
        editor.putBoolean(Constants.PREF_COACHING_MOTIVATIONAL, motivational);

        // Save active plan
        if (coachingType == Constants.COACHING_TYPE_WORKOUT && selectedPlan != null) {
            editor.putString(Constants.PREF_ACTIVE_PLAN_ID, selectedPlan.getId());
            coachingManager.setActivePlan(selectedPlan);
        } else {
            editor.remove(Constants.PREF_ACTIVE_PLAN_ID);
            coachingManager.setActivePlan(null);
        }

        // Apply changes
        editor.apply();

        // Show success message
        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update visibility of settings based on coaching enabled state
     * @param coachingEnabled Whether coaching is enabled
     */
    private void updateSettingsVisibility(boolean coachingEnabled) {
        int visibility = coachingEnabled ? View.VISIBLE : View.GONE;
        
        rgCoachingType.setVisibility(visibility);
        tvCoachingVoiceLabel.setVisibility(visibility);
        rgCoachingVoice.setVisibility(visibility);
        tvCoachingFrequencyLabel.setVisibility(visibility);
        rgCoachingFrequency.setVisibility(visibility);
        switchMotivational.setVisibility(visibility);
        
        // Only show coaching plan section if workout coaching is selected
        boolean showPlanSection = coachingEnabled && rbWorkoutCoaching.isChecked();
        tvCoachingPlanLabel.setVisibility(showPlanSection ? View.VISIBLE : View.GONE);
        spinnerCoachingPlan.setVisibility(showPlanSection ? View.VISIBLE : View.GONE);
        cardPlanDetails.setVisibility(showPlanSection ? View.VISIBLE : View.GONE);
    }

    /**
     * Update plan details UI
     * @param plan Selected coaching plan
     */
    private void updatePlanDetails(CoachingPlan plan) {
        if (plan != null) {
            tvSelectedPlanName.setText(plan.getName());
            tvSelectedPlanDescription.setText(plan.getDescription());
            
            String details = getString(R.string.plan_details_format,
                    plan.getDurationWeeks(),
                    CoachingPlan.getTypeName(plan.getType()),
                    CoachingPlan.getGoalName(plan.getGoal()));
            
            tvSelectedPlanDetails.setText(details);
            btnViewWorkouts.setEnabled(true);
        } else {
            clearPlanDetails();
        }
    }

    /**
     * Clear plan details UI
     */
    private void clearPlanDetails() {
        tvSelectedPlanName.setText(R.string.no_plan_selected);
        tvSelectedPlanDescription.setText("");
        tvSelectedPlanDetails.setText("");
        btnViewWorkouts.setEnabled(false);
    }
}