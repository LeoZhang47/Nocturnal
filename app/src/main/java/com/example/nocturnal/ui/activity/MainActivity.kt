package com.example.nocturnal.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nocturnal.R
import com.example.nocturnal.ui.login.LoginFragment
import com.example.nocturnal.ui.theme.NocturnalTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        // Use the original fragment-based approach first
        setContentView(R.layout.activity_main)

        // Apply window insets to adjust the layout for system UI (optional, only if needed)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add the LoginFragment to the fragment container if it's not already added
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

        // Use Jetpack Compose after the login flow
        // Uncomment the below block if you need to introduce Compose after login
        /*
        setContent {
            NocturnalTheme {
                // Add Compose content, like a settings screen or app content
                MyComposeContent()
            }
        }
        */
    }
}
