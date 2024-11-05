package com.example.nocturnal.ui.activity

import ProfileScreen
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth


class ProfileActivity : AppCompatActivity() {
    private val repository = FirestoreRepository()
    private lateinit var captureProfilePictureLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the ActivityResultLauncher for handling results from CameraActivity
        captureProfilePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    // Update the user's profile picture in the repository or ViewModel
                    //repository.updateUserProfilePicture(it.toString()) // Adjust method as needed
                }
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return

        repository.getUserPosts(uid,
            onSuccess = { imageUrls ->
                setContent {
                    MaterialTheme {
                        ProfileScreen(
                            imageUrls = imageUrls,
                            onBackClick = { finish() },
                            fragmentManager = supportFragmentManager,
                            profileActivity = this,
                            onChangeProfilePicture = { launchCameraForProfilePicture() }
                        )
                    }
                }
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
    }

    private fun launchCameraForProfilePicture() {
        val intent = Intent(this, CameraActivity::class.java).apply {
            putExtra("EXTRA_PROFILE_PICTURE", true)
        }
        captureProfilePictureLauncher.launch(intent)
    }

    // Method to log out and navigate to LoginFragment
    fun logOutAndNavigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}