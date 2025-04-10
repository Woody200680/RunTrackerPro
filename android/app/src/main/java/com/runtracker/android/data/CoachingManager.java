package com.runtracker.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.runtracker.android.data.models.CoachingPlan;
import com.runtracker.android.data.models.CoachingWorkout;
import com.runtracker.android.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manager for coaching plans and workouts
 */
public class CoachingManager {

    private static final String TAG = "CoachingManager";
    
    // Context
    private final Context context;
    
    // Data storage
    private final Map<String, CoachingPlan> plans = new HashMap<>();
    private final Map<String, CoachingWorkout> workouts = new HashMap<>();
    
    // Active data
    private String activePlanId;
    private CoachingPlan activePlan;
    
    // Preferences
    private final SharedPreferences preferences;
    
    /**
     * Constructor
     * @param context Application context
     */
    public CoachingManager(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Load data
        loadData();
        loadActivePlan();
        
        // Create sample data if needed
        createSampleDataIfNeeded();
    }
    
    /**
     * Load coaching data
     */
    private void loadData() {
        // In a real app, this would load from a database or file
        // For this prototype, we'll create sample data in memory
    }
    
    /**
     * Load active plan
     */
    private void loadActivePlan() {
        activePlanId = preferences.getString(Constants.PREF_ACTIVE_PLAN_ID, null);
        if (activePlanId != null) {
            activePlan = plans.get(activePlanId);
        }
    }
    
    /**
     * Create sample coaching plans and workouts if none exist
     */
    private void createSampleDataIfNeeded() {
        if (plans.isEmpty()) {
            createSamplePlans();
        }
    }
    
    /**
     * Create sample coaching plans
     */
    private void createSamplePlans() {
        // Create beginner 5K plan
        CoachingPlan beginner5k = new CoachingPlan();
        beginner5k.setName("Beginner 5K Plan");
        beginner5k.setDescription("An 8-week plan designed for beginners to complete their first 5K race.");
        beginner5k.setDifficulty(CoachingPlan.DIFFICULTY_BEGINNER);
        beginner5k.setGoal(CoachingPlan.GOAL_5K);
        beginner5k.setDurationWeeks(8);
        beginner5k.setWorkoutsPerWeek(3);
        addSampleWorkoutsToPlan(beginner5k);
        
        // Create intermediate 10K plan
        CoachingPlan intermediate10k = new CoachingPlan();
        intermediate10k.setName("Intermediate 10K Plan");
        intermediate10k.setDescription("A 10-week plan for runners who want to improve their 10K time.");
        intermediate10k.setDifficulty(CoachingPlan.DIFFICULTY_INTERMEDIATE);
        intermediate10k.setGoal(CoachingPlan.GOAL_10K);
        intermediate10k.setDurationWeeks(10);
        intermediate10k.setWorkoutsPerWeek(4);
        addSampleWorkoutsToPlan(intermediate10k);
        
        // Create advanced half marathon plan
        CoachingPlan advancedHalf = new CoachingPlan();
        advancedHalf.setName("Advanced Half Marathon");
        advancedHalf.setDescription("A 12-week plan for experienced runners training for a half marathon.");
        advancedHalf.setDifficulty(CoachingPlan.DIFFICULTY_ADVANCED);
        advancedHalf.setGoal(CoachingPlan.GOAL_HALF_MARATHON);
        advancedHalf.setDurationWeeks(12);
        advancedHalf.setWorkoutsPerWeek(5);
        addSampleWorkoutsToPlan(advancedHalf);
        
        // Add plans to map
        addPlan(beginner5k);
        addPlan(intermediate10k);
        addPlan(advancedHalf);
    }
    
    /**
     * Add sample workouts to a plan
     * @param plan The coaching plan
     */
    private void addSampleWorkoutsToPlan(CoachingPlan plan) {
        int weeks = plan.getDurationWeeks();
        int workoutsPerWeek = plan.getWorkoutsPerWeek();
        
        for (int week = 1; week <= weeks; week++) {
            for (int day = 1; day <= workoutsPerWeek; day++) {
                // Create workout
                CoachingWorkout workout = new CoachingWorkout();
                
                // Set basic properties
                workout.setWeek(week);
                workout.setDayOfWeek(day);
                
                // Set type based on day of week
                if (day == 1) {
                    // Monday: Easy run
                    workout.setName("Easy Run");
                    workout.setType(CoachingWorkout.TYPE_ENDURANCE);
                    
                    // Add warm-up segment
                    CoachingWorkout.WorkoutSegment warmup = new CoachingWorkout.WorkoutSegment();
                    warmup.setType(CoachingWorkout.WorkoutSegment.TYPE_WARMUP);
                    warmup.setDuration(5 * 60); // 5 minutes
                    warmup.setInstructions("Start with a gentle warm-up");
                    workout.addSegment(warmup);
                    
                    // Add main segment
                    CoachingWorkout.WorkoutSegment main = new CoachingWorkout.WorkoutSegment();
                    main.setType(CoachingWorkout.WorkoutSegment.TYPE_ACTIVE);
                    main.setDuration((20 + (week * 2)) * 60); // Increases with weeks
                    main.setTargetPaceMin(6.0); // 6:00 min/km
                    main.setTargetPaceMax(7.0); // 7:00 min/km
                    main.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_EASY);
                    main.setInstructions("Keep a conversational pace");
                    workout.addSegment(main);
                    
                    // Add cool-down segment
                    CoachingWorkout.WorkoutSegment cooldown = new CoachingWorkout.WorkoutSegment();
                    cooldown.setType(CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN);
                    cooldown.setDuration(5 * 60); // 5 minutes
                    cooldown.setInstructions("Cool down with a gentle jog or walk");
                    workout.addSegment(cooldown);
                    
                } else if (day == workoutsPerWeek - 1) {
                    // Second-to-last day: Tempo or interval run
                    if (week % 2 == 0) {
                        // Even weeks: Tempo run
                        workout.setName("Tempo Run");
                        workout.setType(CoachingWorkout.TYPE_TEMPO);
                        
                        // Add warm-up segment
                        CoachingWorkout.WorkoutSegment warmup = new CoachingWorkout.WorkoutSegment();
                        warmup.setType(CoachingWorkout.WorkoutSegment.TYPE_WARMUP);
                        warmup.setDuration(10 * 60); // 10 minutes
                        warmup.setInstructions("Start with a gentle warm-up");
                        workout.addSegment(warmup);
                        
                        // Add tempo segment
                        CoachingWorkout.WorkoutSegment tempo = new CoachingWorkout.WorkoutSegment();
                        tempo.setType(CoachingWorkout.WorkoutSegment.TYPE_ACTIVE);
                        tempo.setDuration((15 + (week)) * 60); // Increases with weeks
                        tempo.setTargetPaceMin(5.0); // 5:00 min/km
                        tempo.setTargetPaceMax(5.5); // 5:30 min/km
                        tempo.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_MODERATE);
                        tempo.setInstructions("Maintain a challenging but sustainable pace");
                        workout.addSegment(tempo);
                        
                        // Add cool-down segment
                        CoachingWorkout.WorkoutSegment cooldown = new CoachingWorkout.WorkoutSegment();
                        cooldown.setType(CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN);
                        cooldown.setDuration(10 * 60); // 10 minutes
                        cooldown.setInstructions("Cool down with a gentle jog or walk");
                        workout.addSegment(cooldown);
                        
                    } else {
                        // Odd weeks: Interval run
                        workout.setName("Interval Training");
                        workout.setType(CoachingWorkout.TYPE_INTERVAL);
                        
                        // Add warm-up segment
                        CoachingWorkout.WorkoutSegment warmup = new CoachingWorkout.WorkoutSegment();
                        warmup.setType(CoachingWorkout.WorkoutSegment.TYPE_WARMUP);
                        warmup.setDuration(10 * 60); // 10 minutes
                        warmup.setInstructions("Start with a gentle warm-up");
                        workout.addSegment(warmup);
                        
                        // Add intervals
                        int intervals = 4 + (week / 2); // Increases with weeks
                        for (int i = 0; i < intervals; i++) {
                            // Active interval
                            CoachingWorkout.WorkoutSegment active = new CoachingWorkout.WorkoutSegment();
                            active.setType(CoachingWorkout.WorkoutSegment.TYPE_ACTIVE);
                            active.setDuration(60); // 1 minute
                            active.setTargetPaceMin(4.0); // 4:00 min/km
                            active.setTargetPaceMax(4.5); // 4:30 min/km
                            active.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_HARD);
                            active.setInstructions("Push hard for this interval");
                            workout.addSegment(active);
                            
                            // Recovery interval
                            CoachingWorkout.WorkoutSegment recovery = new CoachingWorkout.WorkoutSegment();
                            recovery.setType(CoachingWorkout.WorkoutSegment.TYPE_RECOVERY);
                            recovery.setDuration(90); // 1.5 minutes
                            recovery.setTargetPaceMin(7.0); // 7:00 min/km
                            recovery.setTargetPaceMax(8.0); // 8:00 min/km
                            recovery.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_EASY);
                            recovery.setInstructions("Recover with a gentle jog");
                            workout.addSegment(recovery);
                        }
                        
                        // Add cool-down segment
                        CoachingWorkout.WorkoutSegment cooldown = new CoachingWorkout.WorkoutSegment();
                        cooldown.setType(CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN);
                        cooldown.setDuration(10 * 60); // 10 minutes
                        cooldown.setInstructions("Cool down with a gentle jog or walk");
                        workout.addSegment(cooldown);
                    }
                } else if (day == workoutsPerWeek) {
                    // Last day: Long run
                    workout.setName("Long Run");
                    workout.setType(CoachingWorkout.TYPE_ENDURANCE);
                    
                    // Add warm-up segment
                    CoachingWorkout.WorkoutSegment warmup = new CoachingWorkout.WorkoutSegment();
                    warmup.setType(CoachingWorkout.WorkoutSegment.TYPE_WARMUP);
                    warmup.setDuration(10 * 60); // 10 minutes
                    warmup.setInstructions("Start with a gentle warm-up");
                    workout.addSegment(warmup);
                    
                    // Add main segment
                    CoachingWorkout.WorkoutSegment main = new CoachingWorkout.WorkoutSegment();
                    main.setType(CoachingWorkout.WorkoutSegment.TYPE_ACTIVE);
                    // Duration increases with weeks, longer for more advanced plans
                    int baseDuration = 30;
                    if (plan.getDifficulty() == CoachingPlan.DIFFICULTY_INTERMEDIATE) {
                        baseDuration = 40;
                    } else if (plan.getDifficulty() == CoachingPlan.DIFFICULTY_ADVANCED) {
                        baseDuration = 50;
                    }
                    main.setDuration((baseDuration + (week * 5)) * 60);
                    main.setTargetPaceMin(6.0); // 6:00 min/km
                    main.setTargetPaceMax(7.0); // 7:00 min/km
                    main.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_EASY);
                    main.setInstructions("Keep a comfortable, sustainable pace");
                    workout.addSegment(main);
                    
                    // Add cool-down segment
                    CoachingWorkout.WorkoutSegment cooldown = new CoachingWorkout.WorkoutSegment();
                    cooldown.setType(CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN);
                    cooldown.setDuration(10 * 60); // 10 minutes
                    cooldown.setInstructions("Cool down with a gentle jog or walk");
                    workout.addSegment(cooldown);
                    
                } else {
                    // Other days: Recovery or speed work
                    if (week % 2 == 0) {
                        // Even weeks: Recovery run
                        workout.setName("Recovery Run");
                        workout.setType(CoachingWorkout.TYPE_RECOVERY);
                        
                        // Add warm-up segment
                        CoachingWorkout.WorkoutSegment warmup = new CoachingWorkout.WorkoutSegment();
                        warmup.setType(CoachingWorkout.WorkoutSegment.TYPE_WARMUP);
                        warmup.setDuration(5 * 60); // 5 minutes
                        warmup.setInstructions("Start with a gentle warm-up");
                        workout.addSegment(warmup);
                        
                        // Add main segment
                        CoachingWorkout.WorkoutSegment main = new CoachingWorkout.WorkoutSegment();
                        main.setType(CoachingWorkout.WorkoutSegment.TYPE_ACTIVE);
                        main.setDuration(25 * 60); // 25 minutes
                        main.setTargetPaceMin(6.5); // 6:30 min/km
                        main.setTargetPaceMax(7.5); // 7:30 min/km
                        main.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_EASY);
                        main.setInstructions("Keep it very easy, focus on recovery");
                        workout.addSegment(main);
                        
                        // Add cool-down segment
                        CoachingWorkout.WorkoutSegment cooldown = new CoachingWorkout.WorkoutSegment();
                        cooldown.setType(CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN);
                        cooldown.setDuration(5 * 60); // 5 minutes
                        cooldown.setInstructions("Cool down with a gentle jog or walk");
                        workout.addSegment(cooldown);
                        
                    } else {
                        // Odd weeks: Speed work
                        workout.setName("Speed Work");
                        workout.setType(CoachingWorkout.TYPE_SPEED);
                        
                        // Add warm-up segment
                        CoachingWorkout.WorkoutSegment warmup = new CoachingWorkout.WorkoutSegment();
                        warmup.setType(CoachingWorkout.WorkoutSegment.TYPE_WARMUP);
                        warmup.setDuration(10 * 60); // 10 minutes
                        warmup.setInstructions("Start with a thorough warm-up");
                        workout.addSegment(warmup);
                        
                        // Add main segment with strides
                        CoachingWorkout.WorkoutSegment main = new CoachingWorkout.WorkoutSegment();
                        main.setType(CoachingWorkout.WorkoutSegment.TYPE_ACTIVE);
                        main.setDuration(20 * 60); // 20 minutes
                        main.setTargetPaceMin(6.0); // 6:00 min/km
                        main.setTargetPaceMax(6.5); // 6:30 min/km
                        main.setIntensity(CoachingWorkout.WorkoutSegment.INTENSITY_MODERATE);
                        main.setInstructions("Include 6-8 strides of 20 seconds during this run");
                        workout.addSegment(main);
                        
                        // Add cool-down segment
                        CoachingWorkout.WorkoutSegment cooldown = new CoachingWorkout.WorkoutSegment();
                        cooldown.setType(CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN);
                        cooldown.setDuration(10 * 60); // 10 minutes
                        cooldown.setInstructions("Cool down with a gentle jog or walk");
                        workout.addSegment(cooldown);
                    }
                }
                
                // Calculate scheduled date (hypothetical - actual implementation would be more complex)
                // For this example, we'll just set it based on current date plus offset
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.WEEK_OF_YEAR, week - 1);
                cal.set(Calendar.DAY_OF_WEEK, day + 1); // +1 because Calendar.SUNDAY is 1
                workout.setScheduledDate(cal.getTimeInMillis());
                
                // Add workout to plan
                plan.addWorkout(workout);
                
                // Also add to workouts map
                workouts.put(workout.getId(), workout);
            }
        }
    }
    
    /**
     * Get all coaching plans
     * @return List of coaching plans
     */
    public List<CoachingPlan> getAllPlans() {
        return new ArrayList<>(plans.values());
    }
    
    /**
     * Get a coaching plan by ID
     * @param planId Plan ID
     * @return Coaching plan or null if not found
     */
    public CoachingPlan getPlan(String planId) {
        return plans.get(planId);
    }
    
    /**
     * Add a coaching plan
     * @param plan Coaching plan to add
     */
    public void addPlan(CoachingPlan plan) {
        plans.put(plan.getId(), plan);
        
        // Add all workouts to workouts map
        for (CoachingWorkout workout : plan.getWorkouts()) {
            workouts.put(workout.getId(), workout);
        }
    }
    
    /**
     * Get a workout by ID
     * @param workoutId Workout ID
     * @return Coaching workout or null if not found
     */
    public CoachingWorkout getWorkout(String workoutId) {
        return workouts.get(workoutId);
    }
    
    /**
     * Get the active plan
     * @return Active coaching plan or null if none set
     */
    public CoachingPlan getActivePlan() {
        return activePlan;
    }
    
    /**
     * Set the active plan
     * @param plan Coaching plan to set as active
     */
    public void setActivePlan(CoachingPlan plan) {
        if (plan != null) {
            activePlanId = plan.getId();
            activePlan = plan;
            
            // Save to preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREF_ACTIVE_PLAN_ID, activePlanId);
            editor.apply();
        }
    }
    
    /**
     * Clear the active plan
     */
    public void clearActivePlan() {
        activePlanId = null;
        activePlan = null;
        
        // Save to preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREF_ACTIVE_PLAN_ID);
        editor.apply();
    }
    
    /**
     * Get the next scheduled workout
     * @return Next scheduled workout or null if none
     */
    public CoachingWorkout getNextScheduledWorkout() {
        if (activePlan == null) {
            return null;
        }
        
        // Get current time
        long now = System.currentTimeMillis();
        
        // Get all workouts from active plan that are scheduled and not completed/skipped
        List<CoachingWorkout> scheduledWorkouts = new ArrayList<>();
        for (CoachingWorkout workout : activePlan.getWorkouts()) {
            if (workout.getState() == CoachingWorkout.STATE_SCHEDULED && 
                    workout.getScheduledDate() >= now) {
                scheduledWorkouts.add(workout);
            }
        }
        
        // Sort by scheduled date
        Collections.sort(scheduledWorkouts, new Comparator<CoachingWorkout>() {
            @Override
            public int compare(CoachingWorkout w1, CoachingWorkout w2) {
                return Long.compare(w1.getScheduledDate(), w2.getScheduledDate());
            }
        });
        
        // Return the first one, or null if none
        return scheduledWorkouts.isEmpty() ? null : scheduledWorkouts.get(0);
    }
    
    /**
     * Get workout progress for the active plan
     * @return Completion percentage (0-100)
     */
    public int getWorkoutProgress() {
        if (activePlan == null) {
            return 0;
        }
        
        return activePlan.getCompletionPercentage();
    }
    
    /**
     * Mark a workout as completed with a run
     * @param workoutId Workout ID
     * @param runId Run ID
     */
    public void markWorkoutCompleted(String workoutId, String runId) {
        CoachingWorkout workout = workouts.get(workoutId);
        if (workout != null) {
            workout.setCompletedRunId(runId);
            workout.setState(CoachingWorkout.STATE_COMPLETED);
            
            // In a real app, this would save to a database
            Log.d(TAG, "Workout " + workoutId + " marked as completed with run " + runId);
        }
    }
    
    /**
     * Mark a workout as skipped
     * @param workoutId Workout ID
     */
    public void markWorkoutSkipped(String workoutId) {
        CoachingWorkout workout = workouts.get(workoutId);
        if (workout != null) {
            workout.setState(CoachingWorkout.STATE_SKIPPED);
            
            // In a real app, this would save to a database
            Log.d(TAG, "Workout " + workoutId + " marked as skipped");
        }
    }
}