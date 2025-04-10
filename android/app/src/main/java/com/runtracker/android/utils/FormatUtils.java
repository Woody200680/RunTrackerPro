package com.runtracker.android.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting run data
 */
public class FormatUtils {
    
    private static final DateFormat dateFormat = 
            new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    
    private static final DateFormat timeFormat = 
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    private static final DateFormat dateTimeFormat = 
            new SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.getDefault());
    
    /**
     * Format a timestamp to a date string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
    
    /**
     * Format a timestamp to a time string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string
     */
    public static String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }
    
    /**
     * Format a timestamp to a date and time string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date and time string
     */
    public static String formatDateTime(long timestamp) {
        return dateTimeFormat.format(new Date(timestamp));
    }
    
    /**
     * Format a duration in milliseconds to a string (HH:MM:SS)
     * @param durationMs Duration in milliseconds
     * @return Formatted duration string
     */
    public static String formatDuration(long durationMs) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(durationMs),
                TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60,
                TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60);
    }
    
    /**
     * Format a distance in kilometers
     * @param distanceKm Distance in kilometers
     * @return Formatted distance string
     */
    public static String formatDistance(double distanceKm) {
        return String.format(Locale.getDefault(), "%.2f km", distanceKm);
    }
    
    /**
     * Format a pace in minutes per kilometer
     * @param paceMinPerKm Pace in minutes per kilometer
     * @return Formatted pace string
     */
    public static String formatPace(double paceMinPerKm) {
        if (paceMinPerKm <= 0) {
            return "0:00";
        }
        
        int minutes = (int) paceMinPerKm;
        int seconds = (int) ((paceMinPerKm - minutes) * 60);
        
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
    
    /**
     * Format calories
     * @param calories Calories burned
     * @return Formatted calories string
     */
    public static String formatCalories(int calories) {
        return String.format(Locale.getDefault(), "%d kcal", calories);
    }
}