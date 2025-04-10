package com.runtracker.android.utils;

/**
 * Constants used throughout the app
 */
public class Constants {

    // Shared Preferences Keys
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_WEIGHT = "user_weight";
    public static final String PREF_USER_HEIGHT = "user_height";
    public static final String PREF_USER_AGE = "user_age";
    public static final String PREF_USER_GENDER = "user_gender";
    
    public static final String PREF_DISTANCE_UNIT = "distance_unit";
    public static final String PREF_WEIGHT_UNIT = "weight_unit";
    
    public static final String PREF_AUDIO_CUES = "audio_cues";
    public static final String PREF_MILESTONE_ALERTS = "milestone_alerts";
    
    // Coaching Preferences Keys
    public static final String PREF_COACHING_ENABLED = "coaching_enabled";
    public static final String PREF_COACHING_TYPE = "coaching_type";
    public static final String PREF_ACTIVE_PLAN_ID = "active_plan_id";
    public static final String PREF_COACHING_VOICE = "coaching_voice";
    public static final String PREF_COACHING_FREQUENCY = "coaching_frequency";
    public static final String PREF_COACHING_MOTIVATIONAL = "coaching_motivational";
    
    // Unit Constants
    public static final String UNIT_KM = "km";
    public static final String UNIT_MILES = "mi";
    public static final String UNIT_KG = "kg";
    public static final String UNIT_LB = "lb";
    
    // Gender Constants
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";
    
    // Milestone Constants
    public static final double MILESTONE_KM = 1.0; // 1 km milestone
    public static final double MILESTONE_MILE = 1.0; // 1 mile milestone
    
    // Location Service Constants
    public static final int LOCATION_UPDATE_INTERVAL = 5000; // 5 seconds
    public static final String ACTION_START_TRACKING_SERVICE = "com.runtracker.android.ACTION_START_TRACKING_SERVICE";
    public static final String ACTION_STOP_TRACKING_SERVICE = "com.runtracker.android.ACTION_STOP_TRACKING_SERVICE";
    public static final String ACTION_PAUSE_TRACKING_SERVICE = "com.runtracker.android.ACTION_PAUSE_TRACKING_SERVICE";
    public static final String ACTION_RESUME_TRACKING_SERVICE = "com.runtracker.android.ACTION_RESUME_TRACKING_SERVICE";
    
    // Audio Cue Constants
    public static final int AUDIO_CUE_INTERVAL = 60000; // 1 minute
    
    // Coaching Constants
    public static final String COACHING_VOICE_MALE = "male";
    public static final String COACHING_VOICE_FEMALE = "female";
    
    public static final int COACHING_FREQUENCY_LOW = 1;    // Less frequent updates
    public static final int COACHING_FREQUENCY_MEDIUM = 2;  // Medium frequency updates
    public static final int COACHING_FREQUENCY_HIGH = 3;   // More frequent updates
    
    // Coaching types
    public static final int COACHING_TYPE_NONE = 0;        // No coaching
    public static final int COACHING_TYPE_BASIC = 1;       // Basic pace and distance updates
    public static final int COACHING_TYPE_WORKOUT = 2;     // Structured workout coaching
}