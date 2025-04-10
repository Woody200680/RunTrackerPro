package com.runtracker.android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.runtracker.android.R;
import com.runtracker.android.data.AchievementManager;
import com.runtracker.android.data.models.Achievement;
import com.runtracker.android.data.models.RunStatistics;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.ui.adapters.AchievementAdapter;

import java.util.List;

/**
 * Fragment for displaying user achievements
 */
public class AchievementsFragment extends Fragment implements AchievementManager.AchievementUpdateListener {

    private TextView tvAchievementsSummary;
    private LinearProgressIndicator progressAchievements;
    private ChipGroup chipGroupFilter;
    private Chip chipAll;
    private Chip chipUnlocked;
    private Chip chipLocked;
    private RecyclerView rvAchievements;
    private TextView tvNoAchievements;

    private AchievementManager achievementManager;
    private AchievementAdapter adapter;
    private RunRepository runRepository;
    private RunStatistics statistics;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Get dependencies
        MainActivity activity = (MainActivity) requireActivity();
        runRepository = activity.getRunRepository();
        achievementManager = activity.getAchievementManager();
        statistics = new RunStatistics();

        // Register for achievement updates
        achievementManager.addListener(this);

        // Initialize RecyclerView
        setupRecyclerView();

        // Setup filter chips
        setupFilterChips();

        // Load achievements
        loadAchievements();
    }

    @Override
    public void onDestroyView() {
        // Unregister listener when fragment is destroyed
        if (achievementManager != null) {
            achievementManager.removeListener(this);
        }
        super.onDestroyView();
    }

    /**
     * Initialize all views
     */
    private void initViews(View view) {
        tvAchievementsSummary = view.findViewById(R.id.tvAchievementsSummary);
        progressAchievements = view.findViewById(R.id.progressAchievements);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipUnlocked = view.findViewById(R.id.chipUnlocked);
        chipLocked = view.findViewById(R.id.chipLocked);
        rvAchievements = view.findViewById(R.id.rvAchievements);
        tvNoAchievements = view.findViewById(R.id.tvNoAchievements);
    }

    /**
     * Set up the RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new AchievementAdapter(requireContext(), statistics);
        rvAchievements.setAdapter(adapter);
        rvAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Set up filter chips
     */
    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            int filter = AchievementAdapter.FILTER_ALL;

            if (checkedId == R.id.chipUnlocked) {
                filter = AchievementAdapter.FILTER_UNLOCKED;
            } else if (checkedId == R.id.chipLocked) {
                filter = AchievementAdapter.FILTER_LOCKED;
            }

            adapter.applyFilter(filter);
            updateEmptyState();
        });
    }

    /**
     * Load achievements and statistics
     */
    private void loadAchievements() {
        // Calculate statistics from runs
        statistics.calculateStats(runRepository.getCompletedRuns());

        // Update achievements based on statistics
        achievementManager.updateAchievementsForStats(statistics);

        // Get achievements list
        List<Achievement> achievements = achievementManager.getAllAchievements();

        // Set data to adapter
        adapter.setAchievements(achievements);

        // Update UI
        updateSummary();
        updateEmptyState();
    }

    /**
     * Update the summary text and progress bar
     */
    private void updateSummary() {
        int total = achievementManager.getTotalAchievementCount();
        int unlocked = achievementManager.getUnlockedAchievementCount();

        // Update summary text
        tvAchievementsSummary.setText(getString(R.string.achievements_summary, unlocked, total));

        // Update progress bar
        int progress = total > 0 ? (unlocked * 100 / total) : 0;
        progressAchievements.setProgress(progress);
    }

    /**
     * Update empty state visibility
     */
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            rvAchievements.setVisibility(View.GONE);
            tvNoAchievements.setVisibility(View.VISIBLE);
        } else {
            rvAchievements.setVisibility(View.VISIBLE);
            tvNoAchievements.setVisibility(View.GONE);
        }
    }

    /**
     * Called when an achievement is unlocked
     */
    @Override
    public void onAchievementUnlocked(Achievement achievement) {
        if (isAdded()) {
            // Show toast notification
            Toast.makeText(requireContext(),
                    getString(R.string.achievement_unlocked, achievement.getTitle()),
                    Toast.LENGTH_LONG).show();

            // Reload achievements
            loadAchievements();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload when returning to fragment
        loadAchievements();
    }
}