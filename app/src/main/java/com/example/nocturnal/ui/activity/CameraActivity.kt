package com.example.nocturnal.ui.activity

import ImageDisplayActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.nocturnal.R
import java.io.File
import java.io.IOException

class CameraActivity : AppCompatActivity() {

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

        // Set the content view to the XML layout
        setContentView(R.layout.activity_camera)

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

        // Set click listeners for buttons
        findViewById<View>(R.id.picture_button).setOnClickListener {
            checkCameraPermission("image")
        }

        findViewById<View>(R.id.video_button).setOnClickListener {
            checkCameraPermission("video")
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
