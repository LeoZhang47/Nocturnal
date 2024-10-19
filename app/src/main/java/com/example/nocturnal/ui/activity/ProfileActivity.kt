package com.example.nocturnal.ui.activity

import ProfileScreen
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import android.content.Intent
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth


class ProfileActivity : AppCompatActivity() {
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return

        repository.getUserPosts(uid,
            onSuccess = { imageUrls ->
                setContent {
                    MaterialTheme {
                        ProfileScreen(
                            imageUrls = imageUrls, // Pass image URLs to ProfileScreen
                            onBackClick = { finish() },
                            fragmentManager = supportFragmentManager,
                            profileActivity = this
                        )
                    }
                }
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
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
