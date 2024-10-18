package com.example.nocturnal.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.example.nocturnal.ProfileScreen
import android.content.Intent


class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Pass 'this' as the ProfileActivity instance
                ProfileScreen(
                    onBackClick = { finish() },
                    fragmentManager = supportFragmentManager, // Pass FragmentManager if needed
                    profileActivity = this // Pass the current activity instance
                )
            }
        }
    }

    // Method to log out and navigate to LoginFragment
    fun logOutAndNavigateToLogin() {
        // Log the user out (you can do this in the ViewModel or here)

        // After logging out, navigate back to MainActivity (or the Activity that hosts LoginFragment)
        val intent = Intent(this, MainActivity::class.java) // Adjust MainActivity if necessary
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // Clear the back stack
        startActivity(intent)
        finish() // Close the ProfileActivity
    }
}
