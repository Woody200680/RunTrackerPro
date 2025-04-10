package com.runtracker.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting values
 */
public class FormatUtils {

    /**
     * Format distance in kilometers
     * @param kilometers Distance in kilometers
     * @return Formatted distance string
     */
    public static String formatDistance(double kilometers) {
        return String.format(Locale.getDefault(), "%.2f km", kilometers);
    }
    
    /**
     * Format distance based on user's preferred unit
     * @param context Application context
     * @param kilometers Distance in kilometers
     * @return Formatted distance string
     */
    public static String formatDistanceWithUnit(Context context, double kilometers) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int unit = preferences.getInt(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KILOMETERS);
        
        if (unit == Constants.UNIT_MILES) {
            double miles = kilometers * 0.621371;
            return String.format(Locale.getDefault(), "%.2f mi", miles);
        } else {
            return String.format(Locale.getDefault(), "%.2f km", kilometers);
        }
    }
    
    /**
     * Format duration in seconds to HH:MM:SS
     * @param seconds Duration in seconds
     * @return Formatted duration string
     */
    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
        }
    }
    
    /**
     * Format duration in seconds to words (e.g. "5 minutes 30 seconds")
     * @param seconds Duration in seconds
     * @return Formatted duration string
     */
    public static String formatDurationWords(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (hours > 0) {
            result.append(hours).append(" hour");
            if (hours > 1) {
                result.append("s");
            }
            
            if (minutes > 0 || secs > 0) {
                result.append(" ");
            }
        }
        
        if (minutes > 0) {
            result.append(minutes).append(" minute");
            if (minutes > 1) {
                result.append("s");
            }
            
            if (secs > 0) {
                result.append(" ");
            }
        }
        
        if (secs > 0 || (hours == 0 && minutes == 0)) {
            result.append(secs).append(" second");
            if (secs != 1) {
                result.append("s");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Format pace in minutes per kilometer
     * @param pace Pace in minutes per kilometer
     * @return Formatted pace string
     */
    public static String formatPace(double pace) {
        if (pace <= 0) {
            return "--:--";
        }
        
        int minutes = (int) Math.floor(pace);
        int seconds = (int) Math.floor((pace - minutes) * 60);
        
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
    
    /**
     * Format pace range
     * @param minPace Minimum pace in minutes per kilometer
     * @param maxPace Maximum pace in minutes per kilometer
     * @return Formatted pace range string
     */
    public static String formatPaceRange(double minPace, double maxPace) {
        return formatPace(minPace) + " - " + formatPace(maxPace);
    }
    
    /**
     * Format calories burned
     * @param calories Calories burned
     * @return Formatted calories string
     */
    public static String formatCalories(int calories) {
        return String.format(Locale.getDefault(), "%d kcal", calories);
    }
    
    /**
     * Format date and time
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date and time string
     */
    public static String formatDateTime(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        return dateFormat.format(date);
    }
    
    /**
     * Format date
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateFormat.format(date);
    }
    
    /**
     * Format time
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string
     */
    public static String formatTime(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        return timeFormat.format(date);
    }
    
    /**
     * Format elevation gain
     * @param elevationGain Elevation gain in meters
     * @return Formatted elevation gain string
     */
    public static String formatElevation(double elevationGain) {
        return String.format(Locale.getDefault(), "%.1f m", elevationGain);
    }
    
    /**
     * Format relative time (e.g. "5 minutes ago")
     * @param timestamp Timestamp in milliseconds
     * @return Formatted relative time string
     */
    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }
}