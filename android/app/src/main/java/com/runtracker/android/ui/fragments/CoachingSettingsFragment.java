package com.runtracker.android.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.runtracker.android.R;
import com.runtracker.android.data.CoachingManager;
import com.runtracker.android.data.models.CoachingPlan;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.Constants;

/**
 * Fragment for voice coaching settings
 */
public class CoachingSettingsFragment extends Fragment {

    // UI components
    private SwitchCompat switchEnableCoaching;
    private RadioGroup rgVoice;
    private RadioGroup rgFrequency;
    private SwitchCompat switchMotivational;
    private RadioGroup rgCoachingType;
    private CardView cardCoachingPlan;
    private TextView tvSelectedPlan;
    private TextView tvPlanDetails;
    private Button btnSelectPlan;
    private Button btnViewWorkouts;
    private Button btnSaveCoachingSettings;
    
    // Data
    private SharedPreferences preferences;
    private CoachingManager coachingManager;
    private CoachingPlan selectedPlan;
    
    // State
    private boolean isCoachingEnabled;
    private int coachingVoice;
    private int coachingFrequency;
    private boolean isMotivationalEnabled;
    private int coachingType;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coaching_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize dependencies
        MainActivity activity = (MainActivity) requireActivity();
        coachingManager = activity.getCoachingManager();
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        
        // Initialize UI components
        initUI(view);
        
        // Load current settings
        loadSettings();
        
        // Setup listeners
        setupListeners();
        
        // Update UI
        updateUI();
    }
    
    /**
     * Initialize UI components
     */
    private void initUI(View view) {
        switchEnableCoaching = view.findViewById(R.id.switchEnableCoaching);
        rgVoice = view.findViewById(R.id.rgVoice);
        rgFrequency = view.findViewById(R.id.rgFrequency);
        switchMotivational = view.findViewById(R.id.switchMotivational);
        rgCoachingType = view.findViewById(R.id.rgCoachingType);
        cardCoachingPlan = view.findViewById(R.id.cardCoachingPlan);
        tvSelectedPlan = view.findViewById(R.id.tvSelectedPlan);
        tvPlanDetails = view.findViewById(R.id.tvPlanDetails);
        btnSelectPlan = view.findViewById(R.id.btnSelectPlan);
        btnViewWorkouts = view.findViewById(R.id.btnViewWorkouts);
        btnSaveCoachingSettings = view.findViewById(R.id.btnSaveCoachingSettings);
    }
    
    /**
     * Load current settings from preferences
     */
    private void loadSettings() {
        // Get settings from preferences
        isCoachingEnabled = preferences.getBoolean(Constants.PREF_COACHING_ENABLED, true);
        coachingVoice = preferences.getInt(Constants.PREF_COACHING_VOICE, Constants.COACHING_VOICE_MALE);
        coachingFrequency = preferences.getInt(Constants.PREF_COACHING_FREQUENCY, Constants.COACHING_FREQUENCY_MEDIUM);
        isMotivationalEnabled = preferences.getBoolean(Constants.PREF_COACHING_MOTIVATIONAL, true);
        coachingType = preferences.getInt(Constants.PREF_COACHING_TYPE, Constants.COACHING_TYPE_BASIC);
        
        // Get active plan
        selectedPlan = coachingManager.getActivePlan();
    }
    
    /**
     * Setup UI listeners
     */
    private void setupListeners() {
        // Enable/disable coaching switch
        switchEnableCoaching.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCoachingEnabled = isChecked;
            updateUI();
        });
        
        // Voice type radio buttons
        rgVoice.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMaleVoice) {
                coachingVoice = Constants.COACHING_VOICE_MALE;
            } else if (checkedId == R.id.rbFemaleVoice) {
                coachingVoice = Constants.COACHING_VOICE_FEMALE;
            }
        });
        
        // Frequency radio buttons
        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLowFrequency) {
                coachingFrequency = Constants.COACHING_FREQUENCY_LOW;
            } else if (checkedId == R.id.rbMediumFrequency) {
                coachingFrequency = Constants.COACHING_FREQUENCY_MEDIUM;
            } else if (checkedId == R.id.rbHighFrequency) {
                coachingFrequency = Constants.COACHING_FREQUENCY_HIGH;
            }
        });
        
        // Motivational feedback switch
        switchMotivational.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMotivationalEnabled = isChecked;
        });
        
        // Coaching type radio buttons
        rgCoachingType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBasicCoaching) {
                coachingType = Constants.COACHING_TYPE_BASIC;
            } else if (checkedId == R.id.rbWorkoutCoaching) {
                coachingType = Constants.COACHING_TYPE_WORKOUT;
            }
            updateUI();
        });
        
        // Select plan button
        btnSelectPlan.setOnClickListener(v -> {
            // Navigate to plan selection screen
            // In a real app, this would navigate to a plan selection screen
            // For this prototype, we'll just select the first plan
            if (!coachingManager.getAllPlans().isEmpty()) {
                selectedPlan = coachingManager.getAllPlans().get(0);
                updateUI();
            }
        });
        
        // View workouts button
        btnViewWorkouts.setOnClickListener(v -> {
            // Navigate to workouts screen
            // In a real app, this would navigate to a workouts screen
            Toast.makeText(requireContext(), "View workouts for " + selectedPlan.getName(), 
                    Toast.LENGTH_SHORT).show();
        });
        
        // Save settings button
        btnSaveCoachingSettings.setOnClickListener(v -> {
            saveSettings();
        });
    }
    
    /**
     * Update UI based on current state
     */
    private void updateUI() {
        // Set enabled state of all controls based on coaching enabled
        rgVoice.setEnabled(isCoachingEnabled);
        rgFrequency.setEnabled(isCoachingEnabled);
        switchMotivational.setEnabled(isCoachingEnabled);
        rgCoachingType.setEnabled(isCoachingEnabled);
        cardCoachingPlan.setEnabled(isCoachingEnabled && coachingType == Constants.COACHING_TYPE_WORKOUT);
        
        // Set checked state of controls based on settings
        switchEnableCoaching.setChecked(isCoachingEnabled);
        
        // Set voice radio button
        if (coachingVoice == Constants.COACHING_VOICE_MALE) {
            ((RadioButton) rgVoice.findViewById(R.id.rbMaleVoice)).setChecked(true);
        } else {
            ((RadioButton) rgVoice.findViewById(R.id.rbFemaleVoice)).setChecked(true);
        }
        
        // Set frequency radio button
        if (coachingFrequency == Constants.COACHING_FREQUENCY_LOW) {
            ((RadioButton) rgFrequency.findViewById(R.id.rbLowFrequency)).setChecked(true);
        } else if (coachingFrequency == Constants.COACHING_FREQUENCY_MEDIUM) {
            ((RadioButton) rgFrequency.findViewById(R.id.rbMediumFrequency)).setChecked(true);
        } else {
            ((RadioButton) rgFrequency.findViewById(R.id.rbHighFrequency)).setChecked(true);
        }
        
        // Set motivational switch
        switchMotivational.setChecked(isMotivationalEnabled);
        
        // Set coaching type radio button
        if (coachingType == Constants.COACHING_TYPE_BASIC) {
            ((RadioButton) rgCoachingType.findViewById(R.id.rbBasicCoaching)).setChecked(true);
        } else {
            ((RadioButton) rgCoachingType.findViewById(R.id.rbWorkoutCoaching)).setChecked(true);
        }
        
        // Show/hide plan card based on coaching type
        cardCoachingPlan.setVisibility(coachingType == Constants.COACHING_TYPE_WORKOUT ? 
                View.VISIBLE : View.GONE);
        
        // Update plan details if a plan is selected
        if (selectedPlan != null) {
            tvSelectedPlan.setText(selectedPlan.getName());
            
            String planDetails = getString(R.string.plan_details_format,
                    selectedPlan.getDurationWeeks(),
                    CoachingPlan.getDifficultyName(selectedPlan.getDifficulty()),
                    CoachingPlan.getGoalName(selectedPlan.getGoal()));
            
            tvPlanDetails.setText(planDetails);
            tvPlanDetails.setVisibility(View.VISIBLE);
            btnViewWorkouts.setVisibility(View.VISIBLE);
        } else {
            tvSelectedPlan.setText(R.string.no_plan_selected);
            tvPlanDetails.setVisibility(View.GONE);
            btnViewWorkouts.setVisibility(View.GONE);
        }
    }
    
    /**
     * Save settings to preferences
     */
    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        
        // Save general coaching settings
        editor.putBoolean(Constants.PREF_COACHING_ENABLED, isCoachingEnabled);
        editor.putInt(Constants.PREF_COACHING_VOICE, coachingVoice);
        editor.putInt(Constants.PREF_COACHING_FREQUENCY, coachingFrequency);
        editor.putBoolean(Constants.PREF_COACHING_MOTIVATIONAL, isMotivationalEnabled);
        editor.putInt(Constants.PREF_COACHING_TYPE, coachingType);
        
        // Save active plan if workout coaching is enabled
        if (coachingType == Constants.COACHING_TYPE_WORKOUT && selectedPlan != null) {
            coachingManager.setActivePlan(selectedPlan);
        } else if (coachingType == Constants.COACHING_TYPE_BASIC) {
            coachingManager.clearActivePlan();
        }
        
        // Apply changes
        editor.apply();
        
        // Show confirmation
        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show();
        
        // Navigate back
        Navigation.findNavController(requireView()).navigateUp();
    }
}