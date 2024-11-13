package com.example.nocturnal.ui.activity

import LocationService
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        locationService = LocationService(this)

        // Initialize the BottomNavigationView
        setupBottomNavigation()

        // Initialize the LocationComponent with custom puck
        initLocationComponent()

        // Set default camera position (OSU coordinates) on map load
        setDefaultCameraPosition()

        // Check and request location permissions
        checkLocationPermissions()
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
