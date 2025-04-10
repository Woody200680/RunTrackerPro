package com.runtracker.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.runtracker.android.data.models.CoachingPlan;
import com.runtracker.android.data.models.CoachingWorkout;
import com.runtracker.android.utils.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Manager class for handling coaching plans and workouts
 */
public class CoachingManager {
    
    private static final String PREF_COACHING_PLANS = "coaching_plans";
    private static final String PREF_COACHING_WORKOUTS = "coaching_workouts";
    
    private final Context context;
    private final SharedPreferences preferences;
    private List<CoachingPlan> plans;
    private List<CoachingWorkout> workouts;
    
    /**
     * Constructor
     * @param context The application context
     */
    public CoachingManager(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Load or initialize plans and workouts
        loadPlans();
        loadWorkouts();
        
        // Initialize default plans if none exist
        if (plans.isEmpty()) {
            initializeDefaultPlans();
        }
    }
    
    /**
     * Load coaching plans from preferences
     */
    private void loadPlans() {
        String json = preferences.getString(PREF_COACHING_PLANS, null);
        
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CoachingPlan>>(){}.getType();
            plans = gson.fromJson(json, type);
        } else {
            plans = new ArrayList<>();
        }
    }
    
    /**
     * Save coaching plans to preferences
     */
    private void savePlans() {
        Gson gson = new Gson();
        String json = gson.toJson(plans);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_COACHING_PLANS, json);
        editor.apply();
    }
    
    /**
     * Load workouts from preferences
     */
    private void loadWorkouts() {
        String json = preferences.getString(PREF_COACHING_WORKOUTS, null);
        
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CoachingWorkout>>(){}.getType();
            workouts = gson.fromJson(json, type);
        } else {
            workouts = new ArrayList<>();
        }
    }
    
    /**
     * Save workouts to preferences
     */
    private void saveWorkouts() {
        Gson gson = new Gson();
        String json = gson.toJson(workouts);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_COACHING_WORKOUTS, json);
        editor.apply();
    }
    
    /**
     * Initialize default coaching plans
     */
    private void initializeDefaultPlans() {
        // Create 5K Beginner Plan
        CoachingPlan beginnerPlan = createPlan(
            "5K_BEGINNER",
            "5K Beginner Plan",
            "An 8-week plan for beginners to complete their first 5K race",
            CoachingPlan.TYPE_BEGINNER,
            CoachingPlan.GOAL_5K,
            8); // 8 weeks
        
        // Create 10K Intermediate Plan
        CoachingPlan intermediatePlan = createPlan(
            "10K_INTERMEDIATE",
            "10K Intermediate Plan",
            "A 10-week plan for runners who have completed a 5K and want to step up to 10K",
            CoachingPlan.TYPE_INTERMEDIATE,
            CoachingPlan.GOAL_10K,
            10); // 10 weeks
        
        // Create Half Marathon Advanced Plan
        CoachingPlan advancedPlan = createPlan(
            "HALF_MARATHON_ADVANCED",
            "Half Marathon Advanced Plan",
            "A 12-week plan for experienced runners training for a half marathon",
            CoachingPlan.TYPE_ADVANCED,
            CoachingPlan.GOAL_HALF_MARATHON,
            12); // 12 weeks
        
        // Add workouts to the beginner plan (just a sample for the first week)
        addWorkoutsToBeginner5KPlan(beginnerPlan);
        
        // Add workouts to the intermediate plan (just a sample)
        addWorkoutsToIntermediate10KPlan(intermediatePlan);
        
        // Add workouts to the advanced plan (just a sample)
        addWorkoutsToAdvancedHalfMarathonPlan(advancedPlan);
        
        // Save the new plans and workouts
        savePlans();
        saveWorkouts();
    }
    
    /**
     * Create a new coaching plan
     */
    private CoachingPlan createPlan(String id, String name, String description, int type, int goal, int durationWeeks) {
        CoachingPlan plan = new CoachingPlan(id, name, description, type, goal, durationWeeks);
        plans.add(plan);
        return plan;
    }
    
    /**
     * Add workouts to the beginner 5K plan
     * @param plan The beginner plan
     */
    private void addWorkoutsToBeginner5KPlan(CoachingPlan plan) {
        // Week 1
        
        // Monday - Easy Run
        CoachingWorkout monday = createWorkout(
            UUID.randomUUID().toString(),
            "Easy Run",
            "A gentle introduction to running with walk breaks",
            CoachingWorkout.TYPE_EASY_RUN,
            1, // Week 1
            1, // Monday
            plan.getId());
        
        // Add segments to Monday workout
        monday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_WARMUP,
            300, // 5 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            10.0, // 10 min/km (very slow jog or brisk walk)
            12.0, // 12 min/km
            "Start with a gentle 5-minute warm-up walk to prepare your muscles"));
        
        monday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_ACTIVE,
            60, // 1 minute
            8, // Repeat 8 times
            CoachingWorkout.WorkoutSegment.INTENSITY_MODERATE,
            7.0, // 7 min/km
            9.0, // 9 min/km
            "Run for 1 minute at a comfortable pace"));
        
        monday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_RECOVERY,
            90, // 1.5 minutes
            8, // Repeat 8 times
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            12.0, // 12 min/km (walking pace)
            15.0, // 15 min/km
            "Walk for 1.5 minutes to recover"));
        
        monday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN,
            300, // 5 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            12.0, // 12 min/km
            15.0, // 15 min/km
            "Cool down with a 5-minute walk"));
        
        // Wednesday - Easy Run (same structure as Monday)
        CoachingWorkout wednesday = createWorkout(
            UUID.randomUUID().toString(),
            "Easy Run",
            "Building endurance with run/walk intervals",
            CoachingWorkout.TYPE_EASY_RUN,
            1, // Week 1
            3, // Wednesday
            plan.getId());
        
        // Same segments as Monday
        wednesday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_WARMUP,
            300, // 5 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            10.0, // 10 min/km
            12.0, // 12 min/km
            "Start with a gentle 5-minute warm-up walk"));
        
        wednesday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_ACTIVE,
            60, // 1 minute
            8, // Repeat 8 times
            CoachingWorkout.WorkoutSegment.INTENSITY_MODERATE,
            7.0, // 7 min/km
            9.0, // 9 min/km
            "Run for 1 minute"));
        
        wednesday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_RECOVERY,
            90, // 1.5 minutes
            8, // Repeat 8 times
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            12.0, // 12 min/km
            15.0, // 15 min/km
            "Walk for 1.5 minutes"));
        
        wednesday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN,
            300, // 5 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            12.0, // 12 min/km
            15.0, // 15 min/km
            "Cool down with a 5-minute walk"));
        
        // Friday - Rest Day
        CoachingWorkout friday = createWorkout(
            UUID.randomUUID().toString(),
            "Rest Day",
            "Recovery day - no running",
            CoachingWorkout.TYPE_REST,
            1, // Week 1
            5, // Friday
            plan.getId());
        
        // Saturday - Long Run
        CoachingWorkout saturday = createWorkout(
            UUID.randomUUID().toString(),
            "Long Run",
            "Gradually building distance with run/walk method",
            CoachingWorkout.TYPE_LONG_RUN,
            1, // Week 1
            6, // Saturday
            plan.getId());
        
        saturday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_WARMUP,
            300, // 5 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            10.0, // 10 min/km
            12.0, // 12 min/km
            "Start with a gentle 5-minute warm-up walk"));
        
        saturday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_ACTIVE,
            60, // 1 minute
            10, // Repeat 10 times
            CoachingWorkout.WorkoutSegment.INTENSITY_MODERATE,
            7.0, // 7 min/km
            9.0, // 9 min/km
            "Run for 1 minute"));
        
        saturday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_RECOVERY,
            90, // 1.5 minutes
            10, // Repeat 10 times
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            12.0, // 12 min/km
            15.0, // 15 min/km
            "Walk for 1.5 minutes"));
        
        saturday.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN,
            300, // 5 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            12.0, // 12 min/km
            15.0, // 15 min/km
            "Cool down with a 5-minute walk"));
    }
    
    /**
     * Add workouts to the intermediate 10K plan
     * @param plan The intermediate plan
     */
    private void addWorkoutsToIntermediate10KPlan(CoachingPlan plan) {
        // This would add detailed workouts for the intermediate plan
        // For brevity, we'll add just a sample workout
        
        // Week 1, Monday - Tempo Run
        CoachingWorkout tempoRun = createWorkout(
            UUID.randomUUID().toString(),
            "Tempo Run",
            "Medium-intensity run to build speed and endurance",
            CoachingWorkout.TYPE_TEMPO_RUN,
            1, // Week 1
            1, // Monday
            plan.getId());
        
        tempoRun.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_WARMUP,
            600, // 10 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            7.0, // 7 min/km
            8.0, // 8 min/km
            "Warm up with a 10-minute easy jog"));
        
        tempoRun.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_ACTIVE,
            1200, // 20 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_MODERATE,
            5.5, // 5:30 min/km
            6.0, // 6:00 min/km
            "Run at a comfortably hard pace for 20 minutes"));
        
        tempoRun.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN,
            600, // 10 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            7.0, // 7 min/km
            8.0, // 8 min/km
            "Cool down with a 10-minute easy jog"));
    }
    
    /**
     * Add workouts to the advanced half marathon plan
     * @param plan The advanced plan
     */
    private void addWorkoutsToAdvancedHalfMarathonPlan(CoachingPlan plan) {
        // This would add detailed workouts for the advanced plan
        // For brevity, we'll add just a sample workout
        
        // Week 1, Tuesday - Interval Training
        CoachingWorkout intervalTraining = createWorkout(
            UUID.randomUUID().toString(),
            "Interval Training",
            "High-intensity intervals to improve speed and VO2 max",
            CoachingWorkout.TYPE_INTERVAL,
            1, // Week 1
            2, // Tuesday
            plan.getId());
        
        intervalTraining.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_WARMUP,
            900, // 15 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            6.0, // 6 min/km
            7.0, // 7 min/km
            "Warm up with a 15-minute easy jog"));
        
        intervalTraining.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_ACTIVE,
            400, // 400 seconds (about 400m at 4:10 min/km pace)
            6, // 6 repetitions
            CoachingWorkout.WorkoutSegment.INTENSITY_HARD,
            4.0, // 4 min/km
            4.5, // 4:30 min/km
            "Run hard for about 400 meters"));
        
        intervalTraining.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_RECOVERY,
            200, // 200 seconds
            6, // 6 repetitions
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            6.0, // 6 min/km
            7.0, // 7 min/km
            "Recover with easy jogging for about 200 meters"));
        
        intervalTraining.addSegment(new CoachingWorkout.WorkoutSegment(
            CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN,
            900, // 15 minutes
            1,
            CoachingWorkout.WorkoutSegment.INTENSITY_EASY,
            6.0, // 6 min/km
            7.0, // 7 min/km
            "Cool down with a 15-minute easy jog"));
    }
    
    /**
     * Create a new workout
     */
    private CoachingWorkout createWorkout(String id, String name, String description, 
                                         int type, int week, int dayOfWeek, String planId) {
        CoachingWorkout workout = new CoachingWorkout(id, name, description, type, week, dayOfWeek, planId);
        workouts.add(workout);
        return workout;
    }
    
    /**
     * Get all coaching plans
     * @return List of coaching plans
     */
    public List<CoachingPlan> getAllPlans() {
        return new ArrayList<>(plans);
    }
    
    /**
     * Get coaching plan by ID
     * @param planId Plan ID
     * @return Coaching plan or null if not found
     */
    public CoachingPlan getPlanById(String planId) {
        for (CoachingPlan plan : plans) {
            if (plan.getId().equals(planId)) {
                return plan;
            }
        }
        return null;
    }
    
    /**
     * Get all workouts for a specific plan
     * @param planId Plan ID
     * @return List of workouts
     */
    public List<CoachingWorkout> getWorkoutsForPlan(String planId) {
        List<CoachingWorkout> planWorkouts = new ArrayList<>();
        for (CoachingWorkout workout : workouts) {
            if (workout.getPlanId().equals(planId)) {
                planWorkouts.add(workout);
            }
        }
        return planWorkouts;
    }
    
    /**
     * Get the active coaching plan
     * @return Active coaching plan or null if none active
     */
    public CoachingPlan getActivePlan() {
        String activePlanId = preferences.getString(Constants.PREF_ACTIVE_PLAN_ID, null);
        if (activePlanId == null) {
            return null;
        }
        
        return getPlanById(activePlanId);
    }
    
    /**
     * Set the active coaching plan
     * @param plan Plan to set as active
     */
    public void setActivePlan(CoachingPlan plan) {
        // First, deactivate all plans
        for (CoachingPlan p : plans) {
            p.setActive(false);
        }
        
        // Activate the selected plan
        if (plan != null) {
            plan.setActive(true);
            preferences.edit().putString(Constants.PREF_ACTIVE_PLAN_ID, plan.getId()).apply();
        } else {
            preferences.edit().remove(Constants.PREF_ACTIVE_PLAN_ID).apply();
        }
        
        // Save changes
        savePlans();
    }
    
    /**
     * Get the next scheduled workout based on current date
     * @return The next workout or null if none found
     */
    public CoachingWorkout getNextScheduledWorkout() {
        CoachingPlan activePlan = getActivePlan();
        if (activePlan == null) {
            return null;
        }
        
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Convert to 1-7 (Monday-Sunday) format
        int adjustedDayOfWeek = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        
        // Set current day and check for next workout
        activePlan.setCurrentDay(adjustedDayOfWeek);
        return activePlan.getNextWorkout();
    }
    
    /**
     * Get workout by ID
     * @param workoutId Workout ID
     * @return Workout or null if not found
     */
    public CoachingWorkout getWorkoutById(String workoutId) {
        for (CoachingWorkout workout : workouts) {
            if (workout.getId().equals(workoutId)) {
                return workout;
            }
        }
        return null;
    }
    
    /**
     * Mark a workout as completed
     * @param workoutId Workout ID
     * @param runId Associated run ID
     * @return true if successful
     */
    public boolean markWorkoutCompleted(String workoutId, String runId) {
        CoachingWorkout workout = getWorkoutById(workoutId);
        if (workout == null) {
            return false;
        }
        
        workout.setCompleted(true);
        workout.setRunId(runId);
        saveWorkouts();
        return true;
    }
    
    /**
     * Get the coaching type preference
     * @return Coaching type
     */
    public int getCoachingType() {
        return preferences.getInt(Constants.PREF_COACHING_TYPE, Constants.COACHING_TYPE_BASIC);
    }
    
    /**
     * Set the coaching type preference
     * @param coachingType Coaching type
     */
    public void setCoachingType(int coachingType) {
        preferences.edit().putInt(Constants.PREF_COACHING_TYPE, coachingType).apply();
    }
    
    /**
     * Check if coaching is enabled
     * @return true if enabled
     */
    public boolean isCoachingEnabled() {
        return preferences.getBoolean(Constants.PREF_COACHING_ENABLED, true);
    }
    
    /**
     * Enable or disable coaching
     * @param enabled Enabled state
     */
    public void setCoachingEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.PREF_COACHING_ENABLED, enabled).apply();
    }
}