package com.runtracker.android.utils;

/**
 * Application-wide constants
 */
public class Constants {

    // Location tracking
    public static final long LOCATION_UPDATE_INTERVAL = 5000; // 5 seconds
    
    // Distance units
    public static final int UNIT_KILOMETERS = 0;
    public static final int UNIT_MILES = 1;
    
    // Audio cue settings
    public static final String PREF_AUDIO_CUES_ENABLED = "audio_cues_enabled";
    public static final String PREF_MILESTONE_CUES = "milestone_cues";
    public static final String PREF_PERIODIC_CUES = "periodic_cues";
    public static final String PREF_CUE_INTERVAL = "cue_interval";
    
    // Coaching settings
    public static final String PREF_COACHING_ENABLED = "coaching_enabled";
    public static final String PREF_COACHING_TYPE = "coaching_type";
    public static final String PREF_COACHING_VOICE = "coaching_voice";
    public static final String PREF_COACHING_FREQUENCY = "coaching_frequency";
    public static final String PREF_COACHING_MOTIVATIONAL = "coaching_motivational";
    public static final String PREF_ACTIVE_PLAN_ID = "active_plan_id";
    
    // Coaching types
    public static final int COACHING_TYPE_BASIC = 0;
    public static final int COACHING_TYPE_WORKOUT = 1;
    
    // Coaching voice
    public static final int COACHING_VOICE_MALE = 0;
    public static final int COACHING_VOICE_FEMALE = 1;
    
    // Coaching frequency
    public static final int COACHING_FREQUENCY_LOW = 0;
    public static final int COACHING_FREQUENCY_MEDIUM = 1;
    public static final int COACHING_FREQUENCY_HIGH = 2;
    
    // User settings
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_WEIGHT = "user_weight";
    public static final String PREF_USER_HEIGHT = "user_height";
    public static final String PREF_USER_AGE = "user_age";
    public static final String PREF_USER_GENDER = "user_gender";
    public static final String PREF_DISTANCE_UNIT = "distance_unit";
    
    // Milestones
    public static final double[] DISTANCE_MILESTONES_KM = {1, 2, 3, 5, 10, 15, 20, 21.1, 42.2};
    public static final long[] TIME_MILESTONES_SECONDS = {
            5 * 60,      // 5 minutes
            10 * 60,     // 10 minutes
            15 * 60,     // 15 minutes
            20 * 60,     // 20 minutes
            30 * 60,     // 30 minutes
            45 * 60,     // 45 minutes
            60 * 60,     // 1 hour
            90 * 60,     // 1.5 hours
            120 * 60     // 2 hours
    };
    
    // Achievement types
    public static final int ACHIEVEMENT_TYPE_DISTANCE = 0;
    public static final int ACHIEVEMENT_TYPE_TIME = 1;
    public static final int ACHIEVEMENT_TYPE_RUNS = 2;
    public static final int ACHIEVEMENT_TYPE_PACE = 3;
    public static final int ACHIEVEMENT_TYPE_STREAK = 4;
    
    // Achievement tiers
    public static final int ACHIEVEMENT_TIER_BRONZE = 0;
    public static final int ACHIEVEMENT_TIER_SILVER = 1;
    public static final int ACHIEVEMENT_TIER_GOLD = 2;

}