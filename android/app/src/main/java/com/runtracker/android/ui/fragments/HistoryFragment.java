package com.runtracker.android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.adapters.RunAdapter;

import java.util.List;

public class HistoryFragment extends Fragment implements RunAdapter.OnRunClickListener {
    
    private RunRepository runRepository;
    private RecyclerView rvRuns;
    private LinearLayout emptyState;
    private Button btnStartTracking;
    private RunAdapter runAdapter;
    private NavController navController;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        rvRuns = view.findViewById(R.id.rvRuns);
        emptyState = view.findViewById(R.id.emptyState);
        btnStartTracking = view.findViewById(R.id.btnStartTracking);
        
        // Initialize navigation controller
        navController = Navigation.findNavController(view);
        
        // Initialize repository
        runRepository = new RunRepository(requireContext());
        
        // Set up RecyclerView
        runAdapter = new RunAdapter(this);
        rvRuns.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRuns.setAdapter(runAdapter);
        
        // Set click listener for start tracking button
        btnStartTracking.setOnClickListener(v -> 
                navController.navigate(R.id.trackFragment));
        
        // Load data
        loadRuns();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning to the fragment
        loadRuns();
    }
    
    private void loadRuns() {
        List<Run> runs = runRepository.getAllRuns();
        
        // Update UI based on whether we have runs
        if (runs.isEmpty()) {
            rvRuns.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvRuns.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            runAdapter.setRuns(runs);
        }
    }
    
    @Override
    public void onRunClick(Run run) {
        // Navigate to run details screen
        Bundle bundle = new Bundle();
        bundle.putString("runId", run.getId());
        navController.navigate(R.id.runDetailFragment, bundle);
    }
}