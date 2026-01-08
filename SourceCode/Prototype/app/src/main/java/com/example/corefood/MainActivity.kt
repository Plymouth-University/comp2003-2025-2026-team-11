package com.example.corefood

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.corefood.R

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Loads DashboardFragment when app opens (AKA my first screen)
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_main
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        //Handle the intent if the activity is being created for the first time
        handleIncomingIntent(intent)

        bottomNav.setOnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }
    }

    //Catches the Intent when MainActivity is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity intent
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val shouldOpenSettings = intent?.getBooleanExtra("OPEN_SETTINGS", false) ?: false
        if (shouldOpenSettings) {
            // Force the UI to show Settings
            bottomNav.selectedItemId = R.id.nav_settings
            loadFragment(SettingsFragment())
        } else {
            bottomNav.selectedItemId = R.id.nav_main
            loadFragment(DashboardFragment())
        }
    }

    // Links the Menu IDs to my specific Fragment classes (this swaps between screens on the bottom taskbar)
    private fun handleNavigation(itemId: Int) {
        when (itemId) {
            R.id.nav_main -> loadFragment(DashboardFragment())
            R.id.nav_settings -> loadFragment(SettingsFragment())
            R.id.nav_exercises -> startActivity(Intent(this, ExerciseActivity::class.java))
            R.id.nav_calories -> startActivity(Intent(this, CaloriesActivity::class.java))
            R.id.nav_food -> startActivity(Intent(this, FoodActivity::class.java))
            R.id.ai_menu -> startActivity(Intent(this, ChatBotActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}