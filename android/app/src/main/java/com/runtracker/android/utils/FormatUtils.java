package com.runtracker.android.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting run data
 */
public class FormatUtils {
    
    /**
     * Format distance in kilometers
     * @param distanceKm Distance in kilometers
     * @return Formatted distance string (e.g., "5.23 km")
     */
    public static String formatDistance(double distanceKm) {
        return String.format(Locale.getDefault(), "%.2f km", distanceKm);
    }
    
    /**
     * Format pace in minutes per kilometer
     * @param paceMinPerKm Pace in minutes per kilometer
     * @return Formatted pace string (e.g., "5:30 min/km")
     */
    public static String formatPace(double paceMinPerKm) {
        if (paceMinPerKm <= 0) {
            return "0:00 min/km";
        }
        
        int minutes = (int) paceMinPerKm;
        int seconds = (int) ((paceMinPerKm - minutes) * 60);
        
        return String.format(Locale.getDefault(), "%d:%02d min/km", minutes, seconds);
    }
    
    /**
     * Format duration in seconds
     * @param durationSeconds Duration in seconds
     * @return Formatted duration string (e.g., "01:25:30")
     */
    public static String formatDuration(long durationSeconds) {
        if (durationSeconds <= 0) {
            return "00:00:00";
        }
        
        long hours = TimeUnit.SECONDS.toHours(durationSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(durationSeconds) % 60;
        long seconds = durationSeconds % 60;
        
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    /**
     * Format timestamp as date
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string (e.g., "April 15, 2025")
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp as time
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string (e.g., "14:30")
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp as date and time
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date and time string (e.g., "April 15, 2025 at 14:30")
     */
    public static String formatDateTime(long timestamp) {
        return formatDate(timestamp) + " at " + formatTime(timestamp);
    }
    
    /**
     * Format calories
     * @param calories Calories burned
     * @return Formatted calories string (e.g., "250 kcal")
     */
    public static String formatCalories(int calories) {
        return String.format(Locale.getDefault(), "%d kcal", calories);
    }
}