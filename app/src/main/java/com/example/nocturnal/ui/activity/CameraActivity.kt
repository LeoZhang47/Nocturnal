package com.example.nocturnal.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.nocturnal.R
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import com.example.nocturnal.ui.fragment.MediaSelectionFragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import com.example.nocturnal.ui.fragment.ImagePreviewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.Color
import com.example.nocturnal.data.model.viewmodel.CameraViewModel
import androidx.activity.viewModels
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.nocturnal.service.LocationService
import com.example.nocturnal.ui.fragment.BarListFragment
import com.example.nocturnal.ui.fragment.MapFragment
import kotlin.math.abs

class CameraActivity : AppCompatActivity() {

    private val cameraViewModel: CameraViewModel by viewModels()
    private lateinit var locationService: LocationService
    private var mediaUri: Uri? = null

    // Define the permission launchers
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var barListViewModel: BarListViewModel
    private lateinit var gestureDetector: GestureDetector
    private var x1 = 0.0f
    private var x2 = 0.0f
    private var y1 = 0.0f
    private var y2 = 0.0f

    companion object {
        const val MIN_DISTANCE = 150
    }


    // Define the picture launcher
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            mediaUri?.let { uri ->
                // Check if this is a profile picture update
                val isProfilePictureUpdate = intent.getBooleanExtra("EXTRA_PROFILE_PICTURE", false)
                if (isProfilePictureUpdate) {
                    // Directly return the URI to ProfileActivity without showing preview
                    setResultAndFinish(uri)
                } else {
                    // Show preview for normal camera usage
                    showImageFragment(uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barListViewModel = ViewModelProvider(this, BarListViewModel.Factory)[BarListViewModel::class.java]
        setContentView(R.layout.activity_camera)

        locationService = LocationService(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_bar -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, BarListFragment())
                        addToBackStack(null)
                        cameraViewModel.fragment = R.id.navigation_bar
                    }
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_camera -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, MediaSelectionFragment())
                        addToBackStack(null)
                        cameraViewModel.fragment = R.id.navigation_camera
                    }
                    overridePendingTransition(0, 0)
                    true
                }  // Already on Camera screen
                R.id.navigation_map -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, MapFragment())
                        addToBackStack(null)
                        cameraViewModel.fragment = R.id.navigation_map
                    }
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                //if (intent.getBooleanExtra("EXTRA_PROFILE_PICTURE", false)) {
                capturePhoto()
                //}
            }
        }

        // Initialize location permission launcher
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationUpdates()
            }
        }

        // Check if the activity was launched for profile picture update
        val isProfilePictureUpdate = intent.getBooleanExtra("EXTRA_PROFILE_PICTURE", false)
        if (isProfilePictureUpdate) {
            checkCameraPermission("image")
        }

        // Request location permission
        requestLocationPermissionAndInitLocation()

        // Set up the ActionBar to include the settings menu
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(Color.parseColor("#3c0142"))

        // Observe isWithinRange from SharedViewModel to update toolbar color
        cameraViewModel.isWithinRange.observe(this) { isWithinRange ->
            if (isWithinRange) {
                //toolbar.setBackgroundColor(Color.parseColor("#006400"))  // Dark green
            } else {
                //toolbar.setBackgroundColor(Color.parseColor("#3c0142"))
            }

        }
    }

    override fun onResume() {
        super.onResume()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = cameraViewModel.fragment
    }

    // Inflate the settings menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.camera_menu, menu)
        return true
    }

    // Handle the settings icon click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(applicationContext.filesDir, "IMG_${timeStamp}.JPG")
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
        val imagePreviewFragment = ImagePreviewFragment.newInstance(uri.toString())
        imagePreviewFragment.show(supportFragmentManager, "ImagePreviewFragment")
    }

    private fun setResultAndFinish(uri: Uri) {
        val resultIntent = Intent().apply { data = uri }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun requestLocationPermissionAndInitLocation() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun startLocationUpdates() {
        locationService.startLocationUpdates()

        // Observe location updates if needed
        locationService.locationLiveData.observe(this) { point ->
            // Handle location update (e.g., logging or other UI updates)
        }
    }
}
