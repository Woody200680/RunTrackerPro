package com.runtracker.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Achievement;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.models.RunStatistics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for handling achievements
 */
public class AchievementManager {
    
    private static final String PREF_ACHIEVEMENTS = "achievements";
    
    private final Context context;
    private final SharedPreferences preferences;
    private Map<String, Achievement> achievements;
    private List<AchievementUpdateListener> listeners;
    
    /**
     * Interface for listening to achievement updates
     */
    public interface AchievementUpdateListener {
        /**
         * Called when an achievement is unlocked
         * @param achievement The unlocked achievement
         */
        void onAchievementUnlocked(Achievement achievement);
    }
    
    /**
     * Constructor
     * @param context The application context
     */
    public AchievementManager(Context context) {
        this.context = context;
        this.preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        this.listeners = new ArrayList<>();
        
        // Load or initialize achievements
        loadAchievements();
    }
    
    /**
     * Add a listener for achievement updates
     * @param listener Listener to add
     */
    public void addListener(AchievementUpdateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     * @param listener Listener to remove
     */
    public void removeListener(AchievementUpdateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Load achievements from preferences, or initialize if not found
     */
    private void loadAchievements() {
        String json = preferences.getString(PREF_ACHIEVEMENTS, null);
        
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Achievement>>(){}.getType();
            achievements = gson.fromJson(json, type);
        } else {
            initializeAchievements();
        }
    }
    
    /**
     * Save achievements to preferences
     */
    private void saveAchievements() {
        Gson gson = new Gson();
        String json = gson.toJson(achievements);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_ACHIEVEMENTS, json);
        editor.apply();
    }
    
    /**
     * Initialize the achievement list with default achievements
     */
    private void initializeAchievements() {
        achievements = new HashMap<>();
        
        // Distance achievements
        addAchievement(new Achievement(
                "distance_bronze", 
                context.getString(R.string.achievement_distance_bronze_title), 
                context.getString(R.string.achievement_distance_bronze_desc), 
                Achievement.TYPE_DISTANCE, 
                Achievement.LEVEL_BRONZE,
                10.0)); // 10 km
        
        addAchievement(new Achievement(
                "distance_silver", 
                context.getString(R.string.achievement_distance_silver_title), 
                context.getString(R.string.achievement_distance_silver_desc), 
                Achievement.TYPE_DISTANCE, 
                Achievement.LEVEL_SILVER,
                50.0)); // 50 km
        
        addAchievement(new Achievement(
                "distance_gold", 
                context.getString(R.string.achievement_distance_gold_title), 
                context.getString(R.string.achievement_distance_gold_desc), 
                Achievement.TYPE_DISTANCE, 
                Achievement.LEVEL_GOLD,
                100.0)); // 100 km
        
        // Run count achievements
        addAchievement(new Achievement(
                "runs_bronze", 
                context.getString(R.string.achievement_runs_bronze_title), 
                context.getString(R.string.achievement_runs_bronze_desc), 
                Achievement.TYPE_RUNS, 
                Achievement.LEVEL_BRONZE,
                5.0)); // 5 runs
        
        addAchievement(new Achievement(
                "runs_silver", 
                context.getString(R.string.achievement_runs_silver_title), 
                context.getString(R.string.achievement_runs_silver_desc), 
                Achievement.TYPE_RUNS, 
                Achievement.LEVEL_SILVER,
                20.0)); // 20 runs
        
        addAchievement(new Achievement(
                "runs_gold", 
                context.getString(R.string.achievement_runs_gold_title), 
                context.getString(R.string.achievement_runs_gold_desc), 
                Achievement.TYPE_RUNS, 
                Achievement.LEVEL_GOLD,
                50.0)); // 50 runs
        
        // Streak achievements
        addAchievement(new Achievement(
                "streak_bronze", 
                context.getString(R.string.achievement_streak_bronze_title), 
                context.getString(R.string.achievement_streak_bronze_desc), 
                Achievement.TYPE_STREAK, 
                Achievement.LEVEL_BRONZE,
                3.0)); // 3 day streak
        
        addAchievement(new Achievement(
                "streak_silver", 
                context.getString(R.string.achievement_streak_silver_title), 
                context.getString(R.string.achievement_streak_silver_desc), 
                Achievement.TYPE_STREAK, 
                Achievement.LEVEL_SILVER,
                7.0)); // 7 day streak
        
        addAchievement(new Achievement(
                "streak_gold", 
                context.getString(R.string.achievement_streak_gold_title), 
                context.getString(R.string.achievement_streak_gold_desc), 
                Achievement.TYPE_STREAK, 
                Achievement.LEVEL_GOLD,
                14.0)); // 14 day streak
        
        // Pace achievements
        addAchievement(new Achievement(
                "pace_bronze", 
                context.getString(R.string.achievement_pace_bronze_title), 
                context.getString(R.string.achievement_pace_bronze_desc), 
                Achievement.TYPE_PACE, 
                Achievement.LEVEL_BRONZE,
                7.0)); // 7 min/km pace
        
        addAchievement(new Achievement(
                "pace_silver", 
                context.getString(R.string.achievement_pace_silver_title), 
                context.getString(R.string.achievement_pace_silver_desc), 
                Achievement.TYPE_PACE, 
                Achievement.LEVEL_SILVER,
                6.0)); // 6 min/km pace
        
        addAchievement(new Achievement(
                "pace_gold", 
                context.getString(R.string.achievement_pace_gold_title), 
                context.getString(R.string.achievement_pace_gold_desc), 
                Achievement.TYPE_PACE, 
                Achievement.LEVEL_GOLD,
                5.0)); // 5 min/km pace
        
        // Duration achievements
        addAchievement(new Achievement(
                "duration_bronze", 
                context.getString(R.string.achievement_duration_bronze_title), 
                context.getString(R.string.achievement_duration_bronze_desc), 
                Achievement.TYPE_DURATION, 
                Achievement.LEVEL_BRONZE,
                1800.0)); // 30 minutes
        
        addAchievement(new Achievement(
                "duration_silver", 
                context.getString(R.string.achievement_duration_silver_title), 
                context.getString(R.string.achievement_duration_silver_desc), 
                Achievement.TYPE_DURATION, 
                Achievement.LEVEL_SILVER,
                3600.0)); // 60 minutes
        
        addAchievement(new Achievement(
                "duration_gold", 
                context.getString(R.string.achievement_duration_gold_title), 
                context.getString(R.string.achievement_duration_gold_desc), 
                Achievement.TYPE_DURATION, 
                Achievement.LEVEL_GOLD,
                7200.0)); // 120 minutes
        
        // Save the initialized achievements
        saveAchievements();
    }
    
    /**
     * Add an achievement to the map
     * @param achievement Achievement to add
     */
    private void addAchievement(Achievement achievement) {
        if (achievement != null && !achievements.containsKey(achievement.getId())) {
            achievements.put(achievement.getId(), achievement);
        }
    }
    
    /**
     * Get all achievements
     * @return List of all achievements
     */
    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }
    
    /**
     * Get all unlocked achievements
     * @return List of unlocked achievements
     */
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked;
    }
    
    /**
     * Get achievement by ID
     * @param id Achievement ID
     * @return Achievement or null if not found
     */
    public Achievement getAchievementById(String id) {
        return achievements.get(id);
    }
    
    /**
     * Update achievements based on a run
     * @param run The run to check
     */
    public void updateAchievementsForRun(Run run) {
        if (run == null) return;
        
        // Check pace achievements (only for completed runs with sufficient distance)
        if (run.getStatus() == Run.STATUS_COMPLETED && run.getTotalDistance() >= 1.0) {
            double pace = run.getPace();
            checkPaceAchievements(pace);
        }
        
        // Check duration achievements for this run
        checkDurationAchievements(run.getActiveDuration());
    }
    
    /**
     * Update achievements based on statistics
     * @param stats Run statistics
     */
    public void updateAchievementsForStats(RunStatistics stats) {
        if (stats == null) return;
        
        // Check distance achievements
        checkDistanceAchievements(stats.getTotalDistance());
        
        // Check run count achievements
        checkRunCountAchievements(stats.getTotalRuns());
        
        // Check streak achievements
        checkStreakAchievements(stats.getLongestStreak());
    }
    
    /**
     * Check and update distance achievements
     * @param totalDistance Total distance in km
     */
    private void checkDistanceAchievements(double totalDistance) {
        checkAchievement("distance_bronze", totalDistance);
        checkAchievement("distance_silver", totalDistance);
        checkAchievement("distance_gold", totalDistance);
    }
    
    /**
     * Check and update run count achievements
     * @param runCount Number of runs
     */
    private void checkRunCountAchievements(int runCount) {
        checkAchievement("runs_bronze", runCount);
        checkAchievement("runs_silver", runCount);
        checkAchievement("runs_gold", runCount);
    }
    
    /**
     * Check and update streak achievements
     * @param longestStreak Longest streak in days
     */
    private void checkStreakAchievements(int longestStreak) {
        checkAchievement("streak_bronze", longestStreak);
        checkAchievement("streak_silver", longestStreak);
        checkAchievement("streak_gold", longestStreak);
    }
    
    /**
     * Check and update pace achievements
     * @param pace Pace in min/km
     */
    private void checkPaceAchievements(double pace) {
        // For pace, lower is better, so we invert the check
        if (pace <= 0) return;
        
        Achievement bronze = achievements.get("pace_bronze");
        Achievement silver = achievements.get("pace_silver");
        Achievement gold = achievements.get("pace_gold");
        
        if (pace <= gold.getTargetValue() && !gold.isUnlocked()) {
            unlockAchievement(gold);
        }
        
        if (pace <= silver.getTargetValue() && !silver.isUnlocked()) {
            unlockAchievement(silver);
        }
        
        if (pace <= bronze.getTargetValue() && !bronze.isUnlocked()) {
            unlockAchievement(bronze);
        }
    }
    
    /**
     * Check and update duration achievements
     * @param duration Duration in seconds
     */
    private void checkDurationAchievements(long duration) {
        checkAchievement("duration_bronze", duration);
        checkAchievement("duration_silver", duration);
        checkAchievement("duration_gold", duration);
    }
    
    /**
     * Check and update a single achievement
     * @param id Achievement ID
     * @param currentValue Current value to check
     */
    private void checkAchievement(String id, double currentValue) {
        Achievement achievement = achievements.get(id);
        if (achievement != null && !achievement.isUnlocked() && 
            achievement.checkIsAchieved(currentValue)) {
            unlockAchievement(achievement);
        }
    }
    
    /**
     * Unlock an achievement
     * @param achievement Achievement to unlock
     */
    private void unlockAchievement(Achievement achievement) {
        if (achievement == null || achievement.isUnlocked()) return;
        
        // Set unlocked status
        achievement.setUnlocked(true);
        achievement.setUnlockedDate(System.currentTimeMillis());
        
        // Update in the map
        achievements.put(achievement.getId(), achievement);
        
        // Save changes
        saveAchievements();
        
        // Notify listeners
        for (AchievementUpdateListener listener : listeners) {
            listener.onAchievementUnlocked(achievement);
        }
    }
    
    /**
     * Reset all achievements (for testing)
     */
    public void resetAchievements() {
        for (Achievement achievement : achievements.values()) {
            achievement.setUnlocked(false);
            achievement.setUnlockedDate(0L);
        }
        saveAchievements();
    }
    
    /**
     * Get the total number of achievements
     * @return Total achievement count
     */
    public int getTotalAchievementCount() {
        return achievements.size();
    }
    
    /**
     * Get the number of unlocked achievements
     * @return Unlocked achievement count
     */
    public int getUnlockedAchievementCount() {
        int count = 0;
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                count++;
            }
        }
        return count;
    }
}