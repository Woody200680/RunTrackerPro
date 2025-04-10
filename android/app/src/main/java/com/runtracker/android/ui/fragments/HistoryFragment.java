package com.runtracker.android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.ui.adapters.RunAdapter;

import java.util.List;

/**
 * Fragment for displaying run history
 */
public class HistoryFragment extends Fragment implements RunAdapter.RunClickListener {

    private TextView tvTotalRuns;
    private TextView tvNoRuns;
    private RecyclerView rvRuns;
    
    private RunRepository runRepository;
    private RunAdapter runAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvTotalRuns = view.findViewById(R.id.tvTotalRuns);
        tvNoRuns = view.findViewById(R.id.tvNoRuns);
        rvRuns = view.findViewById(R.id.rvRuns);
        
        // Get dependencies
        runRepository = ((MainActivity) requireActivity()).getRunRepository();
        
        // Set up RecyclerView
        rvRuns.setLayoutManager(new LinearLayoutManager(requireContext()));
        runAdapter = new RunAdapter(requireContext(), this);
        rvRuns.setAdapter(runAdapter);
        
        // Load runs
        loadRuns();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload runs when coming back to this fragment
        loadRuns();
    }
    
    /**
     * Load completed runs from repository
     */
    private void loadRuns() {
        List<Run> completedRuns = runRepository.getCompletedRuns();
        
        // Update total runs count
        tvTotalRuns.setText(getString(R.string.total_runs, completedRuns.size()));
        
        // Show/hide empty state
        if (completedRuns.isEmpty()) {
            tvNoRuns.setVisibility(View.VISIBLE);
            rvRuns.setVisibility(View.GONE);
        } else {
            tvNoRuns.setVisibility(View.GONE);
            rvRuns.setVisibility(View.VISIBLE);
            
            // Update adapter
            runAdapter.setRuns(completedRuns);
        }
    }
    
    @Override
    public void onRunClick(String runId) {
        // Navigate to run detail screen with the selected run ID
        HistoryFragmentDirections.ActionHistoryFragmentToRunDetailFragment action =
                HistoryFragmentDirections.actionHistoryFragmentToRunDetailFragment(runId);
        Navigation.findNavController(requireView()).navigate(action);
    }
}