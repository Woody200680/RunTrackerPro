package com.runtracker.android.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting various data types for display
 */
public class FormatUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
    
    /**
     * Format a distance in meters to a readable string in kilometers
     */
    public static String formatDistance(float distanceInMeters) {
        float distanceKm = distanceInMeters / 1000f;
        return String.format(Locale.getDefault(), "%.2f km", distanceKm);
    }
    
    /**
     * Format duration in seconds to readable string
     */
    public static String formatDuration(int durationSeconds) {
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;
        
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Format pace (minutes per kilometer) as readable string
     */
    public static String formatPace(float paceMinPerKm) {
        int paceMinutes = (int) paceMinPerKm;
        int paceSeconds = (int) ((paceMinPerKm - paceMinutes) * 60);
        
        return String.format(Locale.getDefault(), "%d:%02d /km", paceMinutes, paceSeconds);
    }
    
    /**
     * Format calories as readable string
     */
    public static String formatCalories(int calories) {
        return String.format(Locale.getDefault(), "%d kcal", calories);
    }
    
    /**
     * Format date to readable string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Format date and time to readable string
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }
    
    /**
     * Format milliseconds to duration string for timer display
     */
    public static String formatTimerDisplay(long millis) {
        int hours = (int) TimeUnit.MILLISECONDS.toHours(millis);
        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(millis) % 60);
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
        
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}