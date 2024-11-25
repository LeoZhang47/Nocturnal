package com.example.nocturnal.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nocturnal.R
import com.example.nocturnal.service.LocationService
import com.mapbox.bindgen.Value
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapOptions
import com.mapbox.maps.TransitionOptions
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        locationService = LocationService(requireContext())

        mapView.setMaximumFps(30)

        mapView.gestures.apply {
            scrollEnabled = false
            pinchToZoomEnabled = false
            rotateEnabled = false
            pitchEnabled = false
        }

        initLocationComponent()
        setDefaultCameraPosition()
        checkLocationPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        locationService.stopLocationUpdates()
    }

    private fun initLocationComponent() {
        mapView.location.apply {
            enabled = true
            pulsingEnabled = false
            locationPuck = LocationPuck2D(
                bearingImage = null,
                shadowImage = ImageHolder.from(R.drawable.ic_shadow_puck),
                topImage = ImageHolder.from(R.drawable.ic_location_puck)
            )
        }
    }

    private fun setDefaultCameraPosition() {
        val osuLatitude = 40.0076
        val osuLongitude = -83.0301

        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(osuLongitude, osuLatitude))
            .zoom(14.0)
            .bearing(0.0)
            .pitch(0.0)
            .build()

        mapView.mapboxMap.setCamera(cameraOptions)
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationService.startLocationUpdates()

        lifecycleScope.launch {
            locationService.locationLiveData.observe(viewLifecycleOwner) { point ->
                point?.let {
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(it)
                            .zoom(15.0)
                            .build()
                    )
                }
            }
        }
    }
}
