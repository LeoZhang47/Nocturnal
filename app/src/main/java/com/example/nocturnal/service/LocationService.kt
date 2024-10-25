import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nocturnal.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener

class LocationService(private val context: Context, private val mapView: MapView) {

    private val _locationLiveData = MutableLiveData<Point>()
    val locationLiveData: LiveData<Point> = _locationLiveData

    fun requestLocationPermission() {
        Dexter.withContext(context)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    enableLocationComponent()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        context,
                        "Location permission is needed to show the map.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun enableLocationComponent() {
        mapView.location.updateSettings {
            enabled = true
            locationPuck = LocationPuck2D(
                topImage = ImageHolder.from(R.drawable.ic_location_puck),
                bearingImage = ImageHolder.from(R.drawable.ic_bearing_puck),
                shadowImage = ImageHolder.from(R.drawable.ic_shadow_puck)
            )
        }

        mapView.location.addOnIndicatorPositionChangedListener(OnIndicatorPositionChangedListener { point ->
            _locationLiveData.postValue(point)
        })
        mapView.mapboxMap.
    }
}