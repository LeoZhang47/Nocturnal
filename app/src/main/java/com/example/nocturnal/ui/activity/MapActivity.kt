package com.example.nocturnal.ui.activity

import LocationService
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nocturnal.R
import com.example.nocturnal.data.model.distanceTo
import com.example.nocturnal.ui.activity.BarListActivity
import com.example.nocturnal.ui.activity.CameraActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.location
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        locationService = LocationService()

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

        // Request location permissions and start location updates
        requestLocationPermissionAndInitLocation()
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
        locationService.startLocationUpdates { listener ->
            mapView.location.updateSettings { enabled = true }
            mapView.location.addOnIndicatorPositionChangedListener(listener)
        }

        // Observe location updates and zoom into user's current location
        locationService.locationLiveData.observe(this) { point ->
            zoomToCurrentLocation(point)
        }
    }

    private fun zoomToCurrentLocation(point: Point) {
        val currentCameraPosition = mapView.mapboxMap.cameraState.center
        // Update camera only if the distance exceeds 10 meters
        if (point.distanceTo(currentCameraPosition) > 10.0) {
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build()
            )
        }
    }
}
