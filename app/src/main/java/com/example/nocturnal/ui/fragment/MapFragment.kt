package com.example.nocturnal.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nocturnal.R
import com.example.nocturnal.service.LocationService
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationService: LocationService
    private lateinit var noPermissionText: TextView

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
        noPermissionText = view.findViewById(R.id.noPermissionText)
        locationService = LocationService(requireContext())

        if (hasLocationPermission()) {
            showMap()
            enableLocationFeatures()
        } else {
            showNoPermissionMessage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        locationService.stopLocationUpdates()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableLocationFeatures() {
        mapView.setMaximumFps(30)
        mapView.mapboxMap.loadStyle(Style.LIGHT)
        setDefaultCameraPosition()
        initLocationComponent()
        startLocationUpdates()
    }

    private fun showMap() {
        mapView.visibility = View.VISIBLE
        noPermissionText.visibility = View.GONE
    }

    private fun showNoPermissionMessage() {
        mapView.visibility = View.GONE
        noPermissionText.visibility = View.VISIBLE
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
