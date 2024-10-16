package com.example.nocturnal.ui.activity

import androidx.compose.material3.*
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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nocturnal.R
import com.example.nocturnal.ui.theme.NocturnalTheme
import com.example.nocturnal.ProfileScreen
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
                showImageDisplayActivity(uri)
            }
        }
    }

    // Define the video launcher using Intent
    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mediaUri?.let { uri ->
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
                    showCaptureOptions()
                }
                else -> {
                    // Handle permission denied case
                }
            }
        }

        setContent {
            NocturnalTheme {
                val navController = rememberNavController()
                @OptIn(ExperimentalMaterial3Api::class)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Camera Activity") },
                            actions = {
                                IconButton(onClick = {
                                    navController.navigate("profile")
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.settings_24px),
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavigationHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        onCapturePhoto = { checkCameraPermission("image") },
                        onCaptureVideo = { checkCameraPermission("video") }
                    )
                }
            }
        }
    }

    private fun showCaptureOptions() {
        // Show options for capturing either an image or a video
    }

    private fun checkCameraPermission(mediaType: String) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                if (mediaType == "image") {
                    capturePhoto()
                } else if (mediaType == "video") {
                    captureVideo()
                }
            }
            else -> {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun capturePhoto() {
        val photoFile = try {
            createImageFile()
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
        }
    }

    private fun captureVideo() {
        val videoFile = try {
            createVideoFile()
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
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
            takeVideoLauncher.launch(intent)
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
            putExtra("mediaUri", uri.toString())
        }
        startActivity(intent)
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onCapturePhoto: () -> Unit,
    onCaptureVideo: () -> Unit
) {
    NavHost(navController = navController, startDestination = "camera", modifier = modifier) {
        composable("camera") {
            GreetingWithCamera(
                onCapturePhoto = onCapturePhoto,
                onCaptureVideo = onCaptureVideo
            )
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}

@Composable
fun GreetingWithCamera(
    modifier: Modifier = Modifier,
    onCapturePhoto: () -> Unit,
    onCaptureVideo: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Button(onClick = onCapturePhoto) {
            Text(text = "Take a Photo")
        }
        Button(onClick = onCaptureVideo) {
            Text(text = "Record a Video")
        }
    }
}
