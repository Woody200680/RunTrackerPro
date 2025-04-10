package com.runtracker.android.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.runtracker.android.R;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    
    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        
        navController = Navigation.findNavController(this, R.id.navHostFragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_track) {
            navController.navigate(R.id.trackFragment);
            return true;
        } else if (itemId == R.id.navigation_history) {
            navController.navigate(R.id.historyFragment);
            return true;
        } else if (itemId == R.id.navigation_stats) {
            navController.navigate(R.id.statsFragment);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onBackPressed() {
        // If we're not on the track fragment, navigate to it
        if (navController.getCurrentDestination().getId() != R.id.trackFragment) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_track);
        } else {
            // If we're already on the track fragment, standard back behavior
            super.onBackPressed();
        }
    }
}