import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationLiveData = MutableLiveData<Point?>()
    val locationLiveData: LiveData<Point?> = _locationLiveData

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            location?.let {
                // Convert Location to Point and post to LiveData
                val point = Point.fromLngLat(it.longitude, it.latitude)
                _locationLiveData.postValue(point)
            }
        }
    }

    // Configure the location request settings
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 60000 // Set to 1 minute (60,000 ms) for lower frequency updates
        fastestInterval = 30000 // Fastest update interval to handle rapid updates if available
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Starts location updates and sets an initial value if available
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // Get last known location, convert to Point, and set as initial value if available
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            lastLocation?.let {
                val initialPoint = Point.fromLngLat(it.longitude, it.latitude)
                _locationLiveData.value = initialPoint
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // Call this function to stop location updates when no longer needed
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getLastKnownLocation(callback: (Point?) -> Unit) {
        locationLiveData.value?.let { callback(it) } ?: callback(null)
    }
}
