package com.example.nocturnal.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.nocturnal.R
import com.example.nocturnal.ui.fragment.MediaSelectionFragment
import com.example.nocturnal.ui.fragment.ImagePreviewFragment
import java.io.File
import java.io.IOException
import java.util.Date

class CameraActivity : AppCompatActivity() {

    private var mediaUri: Uri? = null

    // Define the permission launcher
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    // Define the picture launcher
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            mediaUri?.let { uri ->
                showImageFragment(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                // Permission granted
            }
        }

        // Load the ButtonFragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, MediaSelectionFragment())
            }
        }
    }

    fun checkCameraPermission(mediaType: String) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                if (mediaType == "image") {
                    capturePhoto()
                }
            }
            else -> {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun createImageFile(): File {
        return File(applicationContext.filesDir, "IMG_${Date()}.JPG")
    }

    private fun createVideoFile(): File {
        return File(applicationContext.filesDir, "VID_${Date()}.MP4")
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


    private fun showImageFragment(uri: Uri) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, ImagePreviewFragment.newInstance(uri.toString()))
            addToBackStack(null)
        }
    }
}
