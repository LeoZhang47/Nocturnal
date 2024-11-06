package com.example.nocturnal.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nocturnal.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default selection (Camera) and start CameraActivity
        bottomNavigationView.selectedItemId = R.id.navigation_camera
        startActivity(Intent(this, CameraActivity::class.java))

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_bar -> {
                    startActivity(Intent(this, BarListActivity::class.java))
                    overridePendingTransition(0, 0)  // No animation
                    true
                }
                R.id.navigation_camera -> {
                    // Only start CameraActivity if it's not already the current activity
                    if (bottomNavigationView.selectedItemId != R.id.navigation_camera) {
                        startActivity(Intent(this, CameraActivity::class.java))
                        overridePendingTransition(0, 0)  // No animation
                    }
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    overridePendingTransition(0, 0)  // No animation
                    true
                }
                else -> false
            }
        }
    }
}
