package com.elishaazaria.sayboard

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.elishaazaria.sayboard.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private var binding: ActivitySettingsBinding? = null
    private var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.createNotificationChannel(this)
        binding = ActivitySettingsBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_settings) as NavHostFragment?
        navController = navHostFragment!!.navController
        if (Tools.isMicrophonePermissionGranted(this) && Tools.isIMEEnabled(this)) {
            permissionsGranted()
        } else {
            navController!!.navigate(R.id.navigation_setup)
            binding!!.navView.visibility = View.GONE
        }
    }

    fun permissionsGranted() {
        binding!!.navView.visibility = View.VISIBLE
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration.Builder( //                R.id.navigation_setup,
                R.id.navigation_models,
                R.id.navigation_ui,
                R.id.navigation_logic
            )
                .build()
        setupActionBarWithNavController(this, navController!!, appBarConfiguration)
        setupWithNavController(binding!!.navView, navController!!)
        navController!!.navigate(R.id.navigation_models)
    }
}