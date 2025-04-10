package com.runtracker.android.data.models;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for calculating and storing enhanced run statistics
 */
public class RunStatistics {
    // Overall stats
    private double totalDistance; // km
    private long totalDuration; // seconds
    private double averagePace; // min/km
    private int totalCalories;
    private int totalRuns;
    private long bestPaceRunId;
    private double bestPace;
    private long longestRunId;
    private double longestRunDistance;
    private long longestDurationRunId;
    private long longestDuration;
    
    // Weekly and monthly stats
    private Map<String, Double> weeklyDistances; // Key: YYYY-WW
    private Map<String, Double> monthlyDistances; // Key: YYYY-MM
    private Map<String, Integer> weeklyRunCounts;
    private Map<String, Integer> monthlyRunCounts;
    
    // Time-based stats
    private Map<Integer, Double> hourlyDistances; // Key: Hour of day (0-23)
    private Map<Integer, Integer> weekdayRunCounts; // Key: Day of week (1-7)
    
    // Streak and frequency
    private int currentStreak; // current streak of consecutive days with runs
    private int longestStreak; // longest streak of consecutive days with runs
    private Date lastRunDate;
    private double averageRunsPerWeek;
    
    /**
     * Constructor
     */
    public RunStatistics() {
        resetStats();
    }
    
    /**
     * Reset all statistics
     */
    public void resetStats() {
        totalDistance = 0;
        totalDuration = 0;
        averagePace = 0;
        totalCalories = 0;
        totalRuns = 0;
        bestPaceRunId = -1;
        bestPace = Double.MAX_VALUE;
        longestRunId = -1;
        longestRunDistance = 0;
        longestDurationRunId = -1;
        longestDuration = 0;
        
        weeklyDistances = new HashMap<>();
        monthlyDistances = new HashMap<>();
        weeklyRunCounts = new HashMap<>();
        monthlyRunCounts = new HashMap<>();
        
        hourlyDistances = new HashMap<>();
        weekdayRunCounts = new HashMap<>();
        
        currentStreak = 0;
        longestStreak = 0;
        lastRunDate = null;
        averageRunsPerWeek = 0;
    }
    
    /**
     * Calculate statistics from a list of runs
     * @param runs List of runs
     */
    public void calculateStats(List<Run> runs) {
        // Reset first
        resetStats();
        
        if (runs == null || runs.isEmpty()) {
            return;
        }
        
        totalRuns = runs.size();
        
        // Sort runs by date (oldest first) to calculate streaks correctly
        runs.sort((r1, r2) -> Long.compare(r1.getStartTime(), r2.getStartTime()));
        
        // Track dates for streak calculation
        Calendar calendar = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        Calendar lastRunCal = Calendar.getInstance();
        
        // Track days with runs for streak calculation
        Map<String, Boolean> daysWithRuns = new HashMap<>();
        
        // Process all runs
        for (Run run : runs) {
            // Basic stats
            totalDistance += run.getTotalDistance();
            totalDuration += run.getActiveDuration();
            totalCalories += run.getCaloriesBurned();
            
            // Best pace
            double pace = run.getPace();
            if (pace > 0 && pace < bestPace) {
                bestPace = pace;
                bestPaceRunId = run.getId().hashCode();
            }
            
            // Longest run (distance)
            if (run.getTotalDistance() > longestRunDistance) {
                longestRunDistance = run.getTotalDistance();
                longestRunId = run.getId().hashCode();
            }
            
            // Longest run (duration)
            if (run.getActiveDuration() > longestDuration) {
                longestDuration = run.getActiveDuration();
                longestDurationRunId = run.getId().hashCode();
            }
            
            // Set calendar to run date
            calendar.setTimeInMillis(run.getStartTime());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // 1-12
            int week = calendar.get(Calendar.WEEK_OF_YEAR); // 1-52
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY); // 0-23
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1-7
            
            // For streak calculation - mark this day as having a run
            String dateKey = String.format("%04d-%02d-%02d", year, month, calendar.get(Calendar.DAY_OF_MONTH));
            daysWithRuns.put(dateKey, true);
            
            // Track last run date
            lastRunDate = calendar.getTime();
            
            // Weekly stats
            String weekKey = String.format("%04d-%02d", year, week);
            weeklyDistances.put(weekKey, weeklyDistances.getOrDefault(weekKey, 0.0) + run.getTotalDistance());
            weeklyRunCounts.put(weekKey, weeklyRunCounts.getOrDefault(weekKey, 0) + 1);
            
            // Monthly stats
            String monthKey = String.format("%04d-%02d", year, month);
            monthlyDistances.put(monthKey, monthlyDistances.getOrDefault(monthKey, 0.0) + run.getTotalDistance());
            monthlyRunCounts.put(monthKey, monthlyRunCounts.getOrDefault(monthKey, 0) + 1);
            
            // Hourly stats
            hourlyDistances.put(hourOfDay, hourlyDistances.getOrDefault(hourOfDay, 0.0) + run.getTotalDistance());
            
            // Weekday stats
            weekdayRunCounts.put(dayOfWeek, weekdayRunCounts.getOrDefault(dayOfWeek, 0) + 1);
        }
        
        // Calculate average pace
        if (totalDistance > 0) {
            averagePace = totalDuration / (totalDistance * 60); // min/km
        }
        
        // Calculate average runs per week
        if (totalRuns > 0 && weeklyRunCounts.size() > 0) {
            double totalWeeklyRuns = 0;
            for (int count : weeklyRunCounts.values()) {
                totalWeeklyRuns += count;
            }
            averageRunsPerWeek = totalWeeklyRuns / weeklyRunCounts.size();
        }
        
        // Calculate current and longest streaks
        calculateStreaks(daysWithRuns);
    }
    
    /**
     * Calculate current and longest streaks from days with runs
     * @param daysWithRuns Map of days with runs
     */
    private void calculateStreaks(Map<String, Boolean> daysWithRuns) {
        if (daysWithRuns.isEmpty()) {
            return;
        }
        
        int currentStreak = 0;
        int maxStreak = 0;
        
        // Get today's date
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);
        
        // Start from today and go backwards to find current streak
        Calendar checkDate = (Calendar) currentDate.clone();
        
        // Check if today has a run
        String dateKey = String.format("%04d-%02d-%02d", 
                checkDate.get(Calendar.YEAR), 
                checkDate.get(Calendar.MONTH) + 1, 
                checkDate.get(Calendar.DAY_OF_MONTH));
        
        boolean streakActive = daysWithRuns.containsKey(dateKey);
        
        if (streakActive) {
            currentStreak = 1;
        }
        
        // Check previous days
        while (streakActive) {
            checkDate.add(Calendar.DAY_OF_MONTH, -1);
            dateKey = String.format("%04d-%02d-%02d", 
                    checkDate.get(Calendar.YEAR), 
                    checkDate.get(Calendar.MONTH) + 1, 
                    checkDate.get(Calendar.DAY_OF_MONTH));
            
            if (daysWithRuns.containsKey(dateKey)) {
                currentStreak++;
            } else {
                break;
            }
        }
        
        // Now find longest streak
        // Sort all dates and check for consecutive days
        String[] dates = daysWithRuns.keySet().toArray(new String[0]);
        java.util.Arrays.sort(dates);
        
        int currentCount = 1;
        maxStreak = 1;
        
        Calendar prevDate = Calendar.getInstance();
        Calendar currDate = Calendar.getInstance();
        
        for (int i = 1; i < dates.length; i++) {
            // Parse previous date
            String[] prevParts = dates[i-1].split("-");
            prevDate.set(
                    Integer.parseInt(prevParts[0]),
                    Integer.parseInt(prevParts[1]) - 1,
                    Integer.parseInt(prevParts[2]));
            
            // Parse current date
            String[] currParts = dates[i].split("-");
            currDate.set(
                    Integer.parseInt(currParts[0]),
                    Integer.parseInt(currParts[1]) - 1,
                    Integer.parseInt(currParts[2]));
            
            // Calculate days between
            long diffMillis = currDate.getTimeInMillis() - prevDate.getTimeInMillis();
            int diffDays = (int) (diffMillis / (24 * 60 * 60 * 1000));
            
            if (diffDays == 1) {
                // Consecutive day
                currentCount++;
                maxStreak = Math.max(maxStreak, currentCount);
            } else {
                // Break in streak
                currentCount = 1;
            }
        }
        
        this.currentStreak = currentStreak;
        this.longestStreak = maxStreak;
    }
    
    // Getters
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public long getTotalDuration() {
        return totalDuration;
    }
    
    public double getAveragePace() {
        return averagePace;
    }
    
    public int getTotalCalories() {
        return totalCalories;
    }
    
    public int getTotalRuns() {
        return totalRuns;
    }
    
    public long getBestPaceRunId() {
        return bestPaceRunId;
    }
    
    public double getBestPace() {
        return bestPace;
    }
    
    public long getLongestRunId() {
        return longestRunId;
    }
    
    public double getLongestRunDistance() {
        return longestRunDistance;
    }
    
    public long getLongestDurationRunId() {
        return longestDurationRunId;
    }
    
    public long getLongestDuration() {
        return longestDuration;
    }
    
    public Map<String, Double> getWeeklyDistances() {
        return weeklyDistances;
    }
    
    public Map<String, Double> getMonthlyDistances() {
        return monthlyDistances;
    }
    
    public Map<String, Integer> getWeeklyRunCounts() {
        return weeklyRunCounts;
    }
    
    public Map<String, Integer> getMonthlyRunCounts() {
        return monthlyRunCounts;
    }
    
    public Map<Integer, Double> getHourlyDistances() {
        return hourlyDistances;
    }
    
    public Map<Integer, Integer> getWeekdayRunCounts() {
        return weekdayRunCounts;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public int getLongestStreak() {
        return longestStreak;
    }
    
    public Date getLastRunDate() {
        return lastRunDate;
    }
    
    public double getAverageRunsPerWeek() {
        return averageRunsPerWeek;
    }
    
    /**
     * Find the most active day of the week
     * @return Day of week index (1=Sunday, 2=Monday, ..., 7=Saturday) or -1 if no data
     */
    public int getMostActiveDayOfWeek() {
        int mostActiveDay = -1;
        int maxRuns = 0;
        
        for (Map.Entry<Integer, Integer> entry : weekdayRunCounts.entrySet()) {
            if (entry.getValue() > maxRuns) {
                maxRuns = entry.getValue();
                mostActiveDay = entry.getKey();
            }
        }
        
        return mostActiveDay;
    }
    
    /**
     * Find the most active hour of the day
     * @return Hour of day (0-23) or -1 if no data
     */
    public int getMostActiveHour() {
        int mostActiveHour = -1;
        double maxDistance = 0;
        
        for (Map.Entry<Integer, Double> entry : hourlyDistances.entrySet()) {
            if (entry.getValue() > maxDistance) {
                maxDistance = entry.getValue();
                mostActiveHour = entry.getKey();
            }
        }
        
        return mostActiveHour;
    }
}