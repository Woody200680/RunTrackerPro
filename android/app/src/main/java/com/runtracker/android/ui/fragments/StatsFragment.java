package com.runtracker.android.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatsFragment extends Fragment {
    
    private RunRepository runRepository;
    private TextView tvTotalDistance, tvTotalTime, tvAveragePace, tvTotalCalories;
    private BarChart barChart;
    private LinearLayout emptyState;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance);
        tvTotalTime = view.findViewById(R.id.tvTotalTime);
        tvAveragePace = view.findViewById(R.id.tvAveragePace);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        barChart = view.findViewById(R.id.barChart);
        emptyState = view.findViewById(R.id.emptyState);
        
        // Initialize repository
        runRepository = new RunRepository(requireContext());
        
        // Load data
        loadStatistics();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning to the fragment
        loadStatistics();
    }
    
    private void loadStatistics() {
        List<Run> runs = runRepository.getAllRuns();
        
        if (runs.isEmpty()) {
            // Show empty state
            barChart.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            tvTotalDistance.setText("0.00 km");
            tvTotalTime.setText("0h 0m");
            tvAveragePace.setText("0:00 /km");
            tvTotalCalories.setText("0 kcal");
            return;
        }
        
        // Show content
        barChart.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        
        // Calculate statistics
        float[] stats = runRepository.getStatistics();
        float totalDistance = stats[0]; // meters
        int totalDuration = (int) stats[1]; // seconds
        float averagePace = stats[2]; // min/km
        float totalCalories = stats[3]; // kcal
        
        // Format and display statistics
        tvTotalDistance.setText(FormatUtils.formatDistance(totalDistance));
        
        // Format total time as hours and minutes
        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        tvTotalTime.setText(String.format("%dh %dm", hours, minutes));
        
        tvAveragePace.setText(FormatUtils.formatPace(averagePace));
        tvTotalCalories.setText(FormatUtils.formatCalories((int) totalCalories));
        
        // Create weekly distance chart
        setupBarChart(runs);
    }
    
    private void setupBarChart(List<Run> allRuns) {
        // Get the last 7 days
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Set the calendar to 6 days ago to get 7 days total
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        
        // Map to store daily distances
        float[] dailyDistances = new float[7];
        String[] dayLabels = new String[7];
        
        // Setup day labels and zero the distances
        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            
            // Format day of week (e.g., "Mon", "Tue")
            dayLabels[i] = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                    .format(date);
            
            // Initialize distance for this day to 0
            dailyDistances[i] = 0;
            
            // Move to next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        // Calendar for determining which day each run belongs to
        calendar.add(Calendar.DAY_OF_YEAR, -7); // Reset to first day
        long startOfWeekMillis = calendar.getTimeInMillis();
        
        // Calculate the distance for each day
        for (Run run : allRuns) {
            if (run.getStartTime() != null && run.getStartTime().getTime() >= startOfWeekMillis) {
                // Calculate days since start of week
                int dayIndex = (int) TimeUnit.MILLISECONDS.toDays(
                        run.getStartTime().getTime() - startOfWeekMillis);
                
                // Add distance to the appropriate day (if within our 7-day window)
                if (dayIndex >= 0 && dayIndex < 7) {
                    dailyDistances[dayIndex] += run.getDistanceInMeters() / 1000f; // to km
                }
            }
        }
        
        // Create bar entries and labels from our data
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, dailyDistances[i]));
            labels.add(dayLabels[i]);
        }
        
        // Configure the chart
        BarDataSet dataSet = new BarDataSet(entries, "Distance (km)");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        
        // X-axis configuration
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}