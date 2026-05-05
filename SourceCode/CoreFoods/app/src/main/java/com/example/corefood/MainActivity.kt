package com.example.corefood

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Profile Picture Setup
        profileImage = findViewById<ImageView?>(R.id.profile_image)
        findViewById<View?>(R.id.profile_image).setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(this, ProfilePage::class.java)
            )
        })

        bottomNav = findViewById(R.id.bottom_navigation)

        // Loads DashboardFragment when app opens (AKA my first screen)
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_main
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }
    }

    //Catches the Intent when MainActivity is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity intent

        // Update the navigation bar selection to Dashboard
        bottomNav.selectedItemId = R.id.nav_main

        // Ensure the Dashboard fragment is loaded
        loadFragment(DashboardFragment())
    }

    // Links the Menu IDs to my specific Fragment classes (this swaps between screens on the bottom taskbar)
    private fun handleNavigation(itemId: Int) {
        when (itemId) {
            R.id.nav_main -> loadFragment(DashboardFragment())
            R.id.nav_forum -> startActivity(Intent(this, ForumPage::class.java))
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