package com.runtracker.android.ui.fragments;

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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying run statistics
 */
public class StatsFragment extends Fragment {

    private TextView tvTotalDistance;
    private TextView tvTotalDuration;
    private TextView tvAveragePace;
    private TextView tvTotalCalories;
    private BarChart chartWeek;
    private LineChart chartMonth;
    
    private RunRepository runRepository;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance);
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration);
        tvAveragePace = view.findViewById(R.id.tvAveragePace);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        chartWeek = view.findViewById(R.id.chartWeek);
        chartMonth = view.findViewById(R.id.chartMonth);
        
        // Get dependencies
        runRepository = ((MainActivity) requireActivity()).getRunRepository();
        
        // Set up charts
        setupWeekChart();
        setupMonthChart();
        
        // Load stats
        loadStats();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload stats when coming back to this fragment
        loadStats();
    }
    
    /**
     * Load and display run statistics
     */
    private void loadStats() {
        // Get stats from repository
        double totalDistance = runRepository.getTotalDistance();
        long totalDuration = runRepository.getTotalDuration();
        double averagePace = runRepository.getAveragePace();
        int totalCalories = runRepository.getTotalCalories();
        
        // Update UI
        tvTotalDistance.setText(getString(R.string.total_distance, 
                FormatUtils.formatDistance(totalDistance)));
        tvTotalDuration.setText(getString(R.string.total_duration, 
                FormatUtils.formatDuration(totalDuration)));
        tvAveragePace.setText(getString(R.string.average_pace, 
                FormatUtils.formatPace(averagePace)));
        tvTotalCalories.setText(getString(R.string.total_calories, 
                FormatUtils.formatCalories(totalCalories)));
        
        // Update charts
        updateWeekChart();
        updateMonthChart();
    }
    
    /**
     * Set up the weekly runs chart
     */
    private void setupWeekChart() {
        // Basic chart settings
        chartWeek.getDescription().setEnabled(false);
        chartWeek.setDrawGridBackground(false);
        chartWeek.setDrawBarShadow(false);
        chartWeek.setDrawValueAboveBar(true);
        chartWeek.setPinchZoom(false);
        chartWeek.setScaleEnabled(false);
        chartWeek.setDoubleTapToZoomEnabled(false);
        chartWeek.getLegend().setEnabled(false);
        
        // Setup X axis
        XAxis xAxis = chartWeek.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        
        // Setup Y axis
        YAxis leftAxis = chartWeek.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = chartWeek.getAxisRight();
        rightAxis.setEnabled(false);
    }
    
    /**
     * Update the weekly runs chart with data
     */
    private void updateWeekChart() {
        // Get all runs
        List<Run> completedRuns = runRepository.getCompletedRuns();
        
        // Define the days of the week
        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        
        // Prepare data entries
        List<BarEntry> entries = new ArrayList<>();
        Map<Integer, Float> dayDistances = new HashMap<>();
        
        // Get the current week
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_WEEK, -currentDayOfWeek + 1); // Start of week (Monday)
        long startOfWeekMillis = calendar.getTimeInMillis();
        
        // Calculate distances for each day
        for (Run run : completedRuns) {
            long runStartTime = run.getStartTime();
            
            // Check if run is from this week
            if (runStartTime >= startOfWeekMillis) {
                // Get day of week (1-7, Monday-Sunday)
                calendar.setTimeInMillis(runStartTime);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                
                // Convert to 0-6 index
                int dayIndex = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2;
                
                // Add distance to the corresponding day
                float existingDistance = dayDistances.getOrDefault(dayIndex, 0f);
                dayDistances.put(dayIndex, existingDistance + (float) run.getTotalDistance());
            }
        }
        
        // Create entries for all days
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, dayDistances.getOrDefault(i, 0f)));
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Distances");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setValueTextSize(10f);
        
        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        // Set data to chart
        chartWeek.setData(barData);
        chartWeek.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        chartWeek.invalidate();
    }
    
    /**
     * Set up the monthly runs chart
     */
    private void setupMonthChart() {
        // Basic chart settings
        chartMonth.getDescription().setEnabled(false);
        chartMonth.setDrawGridBackground(false);
        chartMonth.setPinchZoom(false);
        chartMonth.setScaleEnabled(false);
        chartMonth.setDoubleTapToZoomEnabled(false);
        chartMonth.getLegend().setEnabled(false);
        
        // Setup X axis
        XAxis xAxis = chartMonth.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(4);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        
        // Setup Y axis
        YAxis leftAxis = chartMonth.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = chartMonth.getAxisRight();
        rightAxis.setEnabled(false);
    }
    
    /**
     * Update the monthly runs chart with data
     */
    private void updateMonthChart() {
        // Get all runs
        List<Run> completedRuns = runRepository.getCompletedRuns();
        
        // Prepare data entries
        List<Entry> entries = new ArrayList<>();
        Map<Integer, Float> weekDistances = new HashMap<>();
        
        // Get the current month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        
        // Set calendar to start of month
        calendar.set(currentYear, currentMonth, 1, 0, 0, 0);
        long startOfMonthMillis = calendar.getTimeInMillis();
        
        // Calculate distances for each week of the month
        for (Run run : completedRuns) {
            long runStartTime = run.getStartTime();
            
            // Check if run is from this month
            if (runStartTime >= startOfMonthMillis) {
                // Get the week of the month (0-based)
                calendar.setTimeInMillis(runStartTime);
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH) - 1;
                
                // Add distance to the corresponding week
                float existingDistance = weekDistances.getOrDefault(weekOfMonth, 0f);
                weekDistances.put(weekOfMonth, existingDistance + (float) run.getTotalDistance());
            }
        }
        
        // Create entries for all weeks (typically 4-5 weeks in a month)
        int maxWeeks = 5;
        for (int i = 0; i < maxWeeks; i++) {
            entries.add(new Entry(i, weekDistances.getOrDefault(i, 0f)));
        }
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Weekly Distances");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.accent));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.accent_light));
        dataSet.setFillAlpha(100);
        
        // Create line data
        LineData lineData = new LineData(dataSet);
        
        // Set data to chart
        chartMonth.setData(lineData);
        
        // Set X axis labels
        final String[] weeks = new String[]{"Week 1", "Week 2", "Week 3", "Week 4", "Week 5"};
        chartMonth.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weeks));
        
        chartMonth.invalidate();
    }
}