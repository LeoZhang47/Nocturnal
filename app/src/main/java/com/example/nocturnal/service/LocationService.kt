import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener

class LocationService {

    private val _locationLiveData = MutableLiveData<Point>()
    val locationLiveData: LiveData<Point> = _locationLiveData
    private var currentLocation: Point? = null
    private val updateIntervalMillis = 30000L // 30 seconds
    private var lastUpdateTime = 0L

    fun startLocationUpdates(locationProvider: (OnIndicatorPositionChangedListener) -> Unit) {
        locationProvider(OnIndicatorPositionChangedListener { point ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime > updateIntervalMillis) {
                currentLocation = point
                _locationLiveData.postValue(point)
                lastUpdateTime = currentTime
            }
        })
    }

    fun getCurrentLocation(): Point? {
        return currentLocation
    }
}
