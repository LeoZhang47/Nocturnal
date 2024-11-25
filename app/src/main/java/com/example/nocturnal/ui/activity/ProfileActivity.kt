package com.example.nocturnal.ui.activity

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.example.nocturnal.data.FirestoreRepository
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import com.example.nocturnal.ui.screen.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val repository = FirestoreRepository()
    private lateinit var captureProfilePictureLauncher: ActivityResultLauncher<Intent>
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return

        // Initialize the ActivityResultLauncher for handling results from CameraActivity
        captureProfilePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let { uri ->
                    // Launch a coroutine to update the profile picture in Firestore
                    updateProfilePicture(uid, uri)
                }
            }
        }

        setContent {
            MaterialTheme {
                ProfileScreen(
                    onBackClick = { finish() },
                    fragmentManager = supportFragmentManager,
                    profileActivity = this,
                    onChangeProfilePicture = { launchCameraForProfilePicture() }
                )
            }
        }
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

    // Function to update the profile picture asynchronously using suspend function
    private fun updateProfilePicture(uid: String, uri: Uri) {
        lifecycleScope.launch {
            try {
                // Call suspend function from FirestoreRepository to update the profile picture
                repository.updateUserProfilePicture(uid, uri)
                userViewModel.getUserProfilePicture(uid)
                // Optionally, show a success message or update UI
            } catch (e: Exception) {
                // Optionally handle the error (e.g., show an error message)
            }
        }
    }
}
