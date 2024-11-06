package com.example.nocturnal.ui.activity

import LocationService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nocturnal.R
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set Map as the selected item
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
                R.id.navigation_map -> true  // Already on Map screen
                else -> false
            }
        }

        mapView = findViewById(R.id.mapView)

        locationService = LocationService(this, mapView)

        // Observe location updates
        locationService.locationLiveData.observe(this) { point ->
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build()
            )
        }

        // Request location permissions and start location updates
        locationService.requestLocationPermission()
    }
}

