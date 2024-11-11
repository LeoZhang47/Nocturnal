package com.example.nocturnal.ui.activity

import LocationService
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        locationService = LocationService(this)

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

        // Initialize the LocationComponent with custom puck
        initLocationComponent()

        // Request location permissions and start location updates
        requestLocationPermissionAndInitLocation()
    }

    private fun initLocationComponent() {
        val locationComponent = mapView.location.apply {
            enabled = true
            // Check if LocationPuck2D is available directly
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(R.drawable.ic_bearing_puck),
                shadowImage = ImageHolder.from(R.drawable.ic_shadow_puck),
                topImage = ImageHolder.from(R.drawable.ic_location_puck)
            )
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startLocationUpdates()
        }
    }

    private fun requestLocationPermissionAndInitLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun startLocationUpdates() {
        locationService.startLocationUpdates()

        // Observe location updates and zoom into user's current location
        locationService.locationLiveData.observe(this) { point ->
            if (point != null) {
                zoomToCurrentLocation(point)
            }
        }
    }

    private fun zoomToCurrentLocation(point: Point) {
        val currentCameraPosition = mapView.mapboxMap.cameraState.center
        if (point.distanceTo(currentCameraPosition) > 0.1) {
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build()
            )
        }
    }
}
