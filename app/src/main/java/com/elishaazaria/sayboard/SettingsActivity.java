package com.elishaazaria.sayboard;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.elishaazaria.sayboard.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tools.createNotificationChannel(this);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_settings);
        navController = navHostFragment.getNavController();

        if (Tools.isMicrophonePermissionGranted(this) && Tools.isIMEEnabled(this)) {
            permissionsGranted();
        } else {
            navController.navigate(R.id.navigation_setup);
            binding.navView.setVisibility(View.GONE);
        }
    }

    public void permissionsGranted() {
        binding.navView.setVisibility(View.VISIBLE);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_setup,
                R.id.navigation_models,
                R.id.navigation_ui,
                R.id.navigation_logic)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navController.navigate(R.id.navigation_models);
    }

}