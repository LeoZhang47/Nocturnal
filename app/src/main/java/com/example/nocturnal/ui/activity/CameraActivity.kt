package com.example.nocturnal.ui.activity

import ImageDisplayActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.nocturnal.R // Make sure this import is there for the drawable
import com.example.nocturnal.ui.theme.NocturnalTheme
import java.io.File
import java.io.IOException

class CameraActivity : ComponentActivity() {

    private var mediaUri: Uri? = null

    // Define the permission launcher
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    // Define the picture launcher
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            mediaUri?.let { uri ->
                // Start ImageDisplayActivity to show the captured photo
                showImageDisplayActivity(uri)
            }
        }
    }

    // Define the video launcher using Intent
    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mediaUri?.let { uri ->
                // Start ImageDisplayActivity to show the captured video
                showImageDisplayActivity(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CameraActivity", "onCreate called")

        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.CAMERA] == true -> {
                    // Permission granted, show options for capturing image or video
                    showCaptureOptions()
                }
                else -> {
                    // Handle permission denied case
                }
            }
        }
        @OptIn(ExperimentalMaterial3Api::class)
        setContent {
            NocturnalTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Camera Activity") },
                            actions = {
                                IconButton(onClick = {
                                    // Handle settings button click (e.g., show settings screen)
                                    Log.d("CameraActivity", "Settings button clicked")
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.settings_24px), // Ensure your settings icon is available
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    GreetingWithCamera(
                        modifier = Modifier.padding(innerPadding),
                        onCapturePhoto = { checkCameraPermission("image") },
                        onCaptureVideo = { checkCameraPermission("video") }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("CameraActivity", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("CameraActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("CameraActivity", "onPause called")
    }

    private fun showCaptureOptions() {
        // This function should show options for capturing either an image or a video
        // You can use an AlertDialog or any other UI element to ask the user
    }

    private fun checkCameraPermission(mediaType: String) {
        when {
            // Check if the camera permission is granted
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                if (mediaType == "image") {
                    capturePhoto() // Permission granted for image capture
                } else if (mediaType == "video") {
                    captureVideo() // Permission granted for video capture
                }
            }
            else -> {
                // Request camera permission
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun capturePhoto() {
        val photoFile = try {
            createImageFile() // Ensure file creation doesn't fail
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }

        mediaUri = photoFile?.let { file ->
            FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
        }

        mediaUri?.let { uri ->
            takePictureLauncher.launch(uri)
        } ?: run {
            // Handle the error where URI couldn't be created
            // Log error or display a message to the user
        }
    }

    private fun captureVideo() {
        val videoFile = try {
            createVideoFile() // Ensure file creation doesn't fail
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }

        mediaUri = videoFile?.let { file ->
            FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
        }

        mediaUri?.let { uri ->
            // Create an Intent to capture video
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri) // Save video to the provided URI
            }
            takeVideoLauncher.launch(intent) // Launch the video capture intent
        } ?: run {
            // Handle the error where URI couldn't be created
            // Log error or display a message to the user
        }
    }

    private fun createImageFile(): File {
        val storageDir = cacheDir
        return File.createTempFile("temp_image", ".jpg", storageDir)
    }

    private fun createVideoFile(): File {
        val storageDir = cacheDir
        return File.createTempFile("temp_video", ".mp4", storageDir)
    }

    private fun showImageDisplayActivity(uri: Uri) {
        val intent = Intent(this, ImageDisplayActivity::class.java).apply {
            putExtra("mediaUri", uri.toString()) // Pass the URI as a String
        }
        startActivity(intent)
    }
}

@Composable
fun GreetingWithCamera(modifier: Modifier = Modifier, onCapturePhoto: () -> Unit, onCaptureVideo: () -> Unit) {
    Column(modifier = modifier.fillMaxSize()) {
        Button(onClick = onCapturePhoto) {
            Text(text = "Take a Photo")
        }
        Button(onClick = onCaptureVideo) {
            Text(text = "Record a Video")
        }
    }
}
