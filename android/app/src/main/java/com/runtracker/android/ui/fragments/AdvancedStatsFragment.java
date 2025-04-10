package com.runtracker.android.ui.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.models.RunStatistics;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.Constants;
import com.runtracker.android.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment for displaying advanced run statistics
 */
public class AdvancedStatsFragment extends Fragment {

    private TextView tvLongestRun;
    private TextView tvLongestDuration;
    private TextView tvBestPace;
    private TextView tvCurrentStreak;
    private TextView tvLongestStreak;
    private TextView tvAvgRunsPerWeek;
    private TextView tvMostActiveDay;
    private BarChart chartMonthly;
    private BarChart chartHourly;
    private BarChart chartWeekday;

    private RunRepository runRepository;
    private RunStatistics runStatistics;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advanced_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Get dependencies
        runRepository = ((MainActivity) requireActivity()).getRunRepository();
        runStatistics = new RunStatistics();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Set up charts
        setupMonthlyChart();
        setupHourlyChart();
        setupWeekdayChart();

        // Load stats
        loadAdvancedStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload stats when coming back to this fragment
        loadAdvancedStats();
    }

    /**
     * Initialize all views
     */
    private void initViews(View view) {
        tvLongestRun = view.findViewById(R.id.tvLongestRun);
        tvLongestDuration = view.findViewById(R.id.tvLongestDuration);
        tvBestPace = view.findViewById(R.id.tvBestPace);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvAvgRunsPerWeek = view.findViewById(R.id.tvAvgRunsPerWeek);
        tvMostActiveDay = view.findViewById(R.id.tvMostActiveDay);
        chartMonthly = view.findViewById(R.id.chartMonthly);
        chartHourly = view.findViewById(R.id.chartHourly);
        chartWeekday = view.findViewById(R.id.chartWeekday);
    }

    /**
     * Load and display advanced run statistics
     */
    private void loadAdvancedStats() {
        // Get all completed runs
        List<Run> runs = runRepository.getCompletedRuns();

        // Calculate stats
        runStatistics.calculateStats(runs);

        // Update UI with stats
        updateStats();
        updateCharts();
    }

    /**
     * Update UI with statistics
     */
    private void updateStats() {
        // Get distance unit preference
        String distanceUnit = sharedPreferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
        boolean useMetric = distanceUnit.equals(Constants.UNIT_KM);

        // Update records section
        double longestDistance = runStatistics.getLongestRunDistance();
        if (!useMetric) {
            longestDistance *= 0.621371; // Convert to miles
        }
        tvLongestRun.setText(FormatUtils.formatDistance(longestDistance));
        tvLongestDuration.setText(FormatUtils.formatDuration(runStatistics.getLongestDuration()));
        tvBestPace.setText(FormatUtils.formatPace(runStatistics.getBestPace()));

        // Update streaks section
        tvCurrentStreak.setText(getString(R.string.days_count, runStatistics.getCurrentStreak()));
        tvLongestStreak.setText(getString(R.string.days_count, runStatistics.getLongestStreak()));
        tvAvgRunsPerWeek.setText(String.format(Locale.getDefault(), "%.1f %s", 
                runStatistics.getAverageRunsPerWeek(), getString(R.string.runs)));

        // Update most active day
        int mostActiveDay = runStatistics.getMostActiveDayOfWeek();
        String[] weekdays = getResources().getStringArray(R.array.weekdays);
        if (mostActiveDay >= 1 && mostActiveDay <= 7) {
            // Convert from Calendar day of week (1-7, Sunday-Saturday) to array index (0-6)
            int dayIndex = mostActiveDay == Calendar.SUNDAY ? 0 : mostActiveDay - 1;
            tvMostActiveDay.setText(weekdays[dayIndex]);
        } else {
            tvMostActiveDay.setText(R.string.not_enough_data);
        }
    }

    /**
     * Update all charts
     */
    private void updateCharts() {
        updateMonthlyChart();
        updateHourlyChart();
        updateWeekdayChart();
    }

    /**
     * Set up the monthly distance chart
     */
    private void setupMonthlyChart() {
        // Basic chart settings
        chartMonthly.getDescription().setEnabled(false);
        chartMonthly.setDrawGridBackground(false);
        chartMonthly.setDrawBarShadow(false);
        chartMonthly.setDrawValueAboveBar(true);
        chartMonthly.setPinchZoom(false);
        chartMonthly.setScaleEnabled(false);
        chartMonthly.setDoubleTapToZoomEnabled(false);
        chartMonthly.getLegend().setEnabled(false);
        
        // Setup X axis
        XAxis xAxis = chartMonthly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        
        // Setup Y axis
        YAxis leftAxis = chartMonthly.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = chartMonthly.getAxisRight();
        rightAxis.setEnabled(false);
    }
    
    /**
     * Update the monthly distance chart with data
     */
    private void updateMonthlyChart() {
        // Get monthly distances
        Map<String, Double> monthlyDistances = runStatistics.getMonthlyDistances();
        
        // Check if we have data
        if (monthlyDistances.isEmpty()) {
            chartMonthly.setData(null);
            chartMonthly.invalidate();
            return;
        }
        
        // Get distance unit preference
        String distanceUnit = sharedPreferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
        boolean useMetric = distanceUnit.equals(Constants.UNIT_KM);
        
        // Prepare data entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        // Sort keys for proper ordering
        List<String> sortedKeys = new ArrayList<>(monthlyDistances.keySet());
        java.util.Collections.sort(sortedKeys);
        
        // Only show the last 6 months
        int maxMonths = Math.min(sortedKeys.size(), 6);
        int startIndex = Math.max(0, sortedKeys.size() - maxMonths);
        
        for (int i = 0; i < maxMonths; i++) {
            String key = sortedKeys.get(startIndex + i);
            double distance = monthlyDistances.get(key);
            
            // Convert distance if needed
            if (!useMetric) {
                distance *= 0.621371; // Convert to miles
            }
            
            entries.add(new BarEntry(i, (float) distance));
            
            // Format month label (YYYY-MM -> MMM)
            String[] parts = key.split("-");
            if (parts.length == 2) {
                try {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month - 1);
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
                    labels.add(sdf.format(cal.getTime()));
                } catch (NumberFormatException e) {
                    labels.add(key);
                }
            } else {
                labels.add(key);
            }
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Monthly Distances");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return FormatUtils.formatDistance((double) value);
            }
        });
        
        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        
        // Set data to chart
        chartMonthly.setData(barData);
        chartMonthly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartMonthly.invalidate();
    }

    /**
     * Set up the hourly distribution chart
     */
    private void setupHourlyChart() {
        // Basic chart settings
        chartHourly.getDescription().setEnabled(false);
        chartHourly.setDrawGridBackground(false);
        chartHourly.setDrawBarShadow(false);
        chartHourly.setDrawValueAboveBar(true);
        chartHourly.setPinchZoom(false);
        chartHourly.setScaleEnabled(false);
        chartHourly.setDoubleTapToZoomEnabled(false);
        chartHourly.getLegend().setEnabled(false);
        
        // Setup X axis
        XAxis xAxis = chartHourly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        
        // Setup Y axis
        YAxis leftAxis = chartHourly.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = chartHourly.getAxisRight();
        rightAxis.setEnabled(false);
    }
    
    /**
     * Update the hourly distribution chart with data
     */
    private void updateHourlyChart() {
        // Get hourly distances
        Map<Integer, Double> hourlyDistances = runStatistics.getHourlyDistances();
        
        // Check if we have data
        if (hourlyDistances.isEmpty()) {
            chartHourly.setData(null);
            chartHourly.invalidate();
            return;
        }
        
        // Get distance unit preference
        String distanceUnit = sharedPreferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
        boolean useMetric = distanceUnit.equals(Constants.UNIT_KM);
        
        // Prepare data entries and labels
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        // Create entries for all hours (0-23)
        for (int hour = 0; hour < 24; hour++) {
            double distance = hourlyDistances.getOrDefault(hour, 0.0);
            
            // Convert distance if needed
            if (!useMetric) {
                distance *= 0.621371; // Convert to miles
            }
            
            entries.add(new BarEntry(hour, (float) distance));
            
            // Format hour label (24h -> 12h)
            if (hour == 0) {
                labels.add("12am");
            } else if (hour < 12) {
                labels.add(hour + "am");
            } else if (hour == 12) {
                labels.add("12pm");
            } else {
                labels.add((hour - 12) + "pm");
            }
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Hourly Distances");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.accent));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Only show values for non-zero entries to avoid clutter
                return value > 0 ? FormatUtils.formatDistance((double) value) : "";
            }
        });
        
        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        
        // Set data to chart
        chartHourly.setData(barData);
        chartHourly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartHourly.getXAxis().setLabelCount(8, true); // Show fewer labels
        chartHourly.invalidate();
    }

    /**
     * Set up the weekday distribution chart
     */
    private void setupWeekdayChart() {
        // Basic chart settings
        chartWeekday.getDescription().setEnabled(false);
        chartWeekday.setDrawGridBackground(false);
        chartWeekday.setDrawBarShadow(false);
        chartWeekday.setDrawValueAboveBar(true);
        chartWeekday.setPinchZoom(false);
        chartWeekday.setScaleEnabled(false);
        chartWeekday.setDoubleTapToZoomEnabled(false);
        chartWeekday.getLegend().setEnabled(false);
        
        // Setup X axis
        XAxis xAxis = chartWeekday.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        
        // Setup Y axis
        YAxis leftAxis = chartWeekday.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = chartWeekday.getAxisRight();
        rightAxis.setEnabled(false);
    }
    
    /**
     * Update the weekday distribution chart with data
     */
    private void updateWeekdayChart() {
        // Get weekday run counts
        Map<Integer, Integer> weekdayRunCounts = runStatistics.getWeekdayRunCounts();
        
        // Check if we have data
        if (weekdayRunCounts.isEmpty()) {
            chartWeekday.setData(null);
            chartWeekday.invalidate();
            return;
        }
        
        // Prepare data entries and labels
        List<BarEntry> entries = new ArrayList<>();
        
        // Get weekday names
        String[] weekdays = getResources().getStringArray(R.array.weekdays);
        
        // Create entries for all days (1-7 corresponds to Sunday-Saturday in Calendar)
        for (int day = 0; day < 7; day++) {
            // Convert from day index (0-6) to Calendar day of week (1-7)
            int calendarDay = (day == 0) ? Calendar.SUNDAY : day + 1;
            int count = weekdayRunCounts.getOrDefault(calendarDay, 0);
            entries.add(new BarEntry(day, count));
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Weekday Distribution");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setValueTextSize(10f);
        
        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        // Set data to chart
        chartWeekday.setData(barData);
        chartWeekday.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weekdays));
        chartWeekday.invalidate();
    }
}