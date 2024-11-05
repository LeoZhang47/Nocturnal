package com.example.nocturnal.ui.activity

import LocationService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nocturnal.R
import com.example.nocturnal.data.model.distanceTo
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mapView = findViewById(R.id.mapView)

        locationService = LocationService(this, mapView)

        // Observe location updates
        locationService.locationLiveData.observe(this) { point ->
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


        // Request location permissions and start location updates
        locationService.requestLocationPermission()
    }
}

