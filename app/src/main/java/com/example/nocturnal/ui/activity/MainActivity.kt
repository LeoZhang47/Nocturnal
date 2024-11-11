package com.example.nocturnal.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.nocturnal.R
import com.example.nocturnal.ui.fragment.LoginFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        // Check if the user is already logged in
        if (auth.currentUser != null) {
            // User is logged in, navigate to MainScreenActivity
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()  // Close MainActivity so it doesn't remain in the back stack
            return
        }

        // If not logged in, set content view and show LoginFragment
        setContentView(R.layout.activity_main)

        // Add the LoginFragment to the fragment container if it's not already added
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
