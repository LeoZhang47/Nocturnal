package com.example.nocturnal.ui.activity

import LocationService
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nocturnal.R
import com.example.nocturnal.data.model.distanceTo
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.plugin.LocationPuck2D
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.nocturnal.ui.activity.CameraActivity.Companion.MIN_DISTANCE
import kotlinx.coroutines.launch
import kotlin.math.abs

class MapActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    private lateinit var gestureDetector: GestureDetector
    private var x1 = 0.0f
    private var x2 = 0.0f
    private var y1 = 0.0f
    private var y2 = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Set up the ActionBar to include the settings menu
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(Color.parseColor("#3c0142"))

        mapView = findViewById(R.id.mapView)
        locationService = LocationService(this)
        gestureDetector = GestureDetector(this, this)

        // Initialize the BottomNavigationView
        setupBottomNavigation()

        // Initialize the LocationComponent with custom puck
        initLocationComponent()

        // Set default camera position (OSU coordinates) on map load
        setDefaultCameraPosition()

        // Check and request location permissions
        checkLocationPermissions()
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

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_map
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_bar -> {
                    startActivity(Intent(this, BarListActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_camera -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_map -> true
                else -> false
            }
        }
    }

    private fun initLocationComponent() {
        mapView.location.apply {
            enabled = true
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(R.drawable.ic_bearing_puck),
                shadowImage = ImageHolder.from(R.drawable.ic_shadow_puck),
                topImage = ImageHolder.from(R.drawable.ic_location_puck)
            )
        }
    }

    private fun setDefaultCameraPosition() {
        // OSU coordinates (latitude, longitude)
        val osuLatitude = 40.0076
        val osuLongitude = -83.0301

        // Set the default camera position and zoom level to OSU
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(osuLongitude, osuLatitude)) // Set the default center point
            .zoom(14.0) // Set the zoom level (you can adjust this value)
            .build()

        // Apply the camera options to the map
        mapView.mapboxMap.setCamera(cameraOptions)
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationService.startLocationUpdates()

        // Observe the location updates and zoom to current location
        locationService.locationLiveData.observe(this) { point ->
            if (point != null) {
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(15.0)
                        .build()
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        when (event?.action) {
            0 -> {
                x1=event.x
                y1=event.y
            }
            1 -> {
                x2=event.x
                y2 = event.y
                val valueX = x2-x1
                if (abs(valueX) > MIN_DISTANCE) {
                    if (x2 > x1) {
                        startActivity(Intent(this, CameraActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) { }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
