package com.example.comp2003_prototype

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.comp2003_prototype.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Loads DashboardFragment when app opens (AKA my first screen)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            // Links the Menu IDs to my specific Fragment classes (this swaps between screens on the bottom taskbar)
            when (item.itemId) {
                R.id.nav_main -> selectedFragment = DashboardFragment()
                R.id.nav_exercises -> selectedFragment = ExercisesFragment()
                R.id.nav_calories -> selectedFragment = CaloriesFragment()
                R.id.nav_food -> selectedFragment = FoodFragment()
                R.id.nav_settings -> selectedFragment = SettingsFragment()
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }
    }
}