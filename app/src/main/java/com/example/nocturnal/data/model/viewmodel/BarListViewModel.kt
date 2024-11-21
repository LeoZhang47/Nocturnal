package com.example.nocturnal.data.model.viewmodel

import LocationService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.nocturnal.data.model.Bar
import com.example.nocturnal.data.FirestoreRepository
import com.example.nocturnal.data.model.Post
import com.example.nocturnal.data.model.distanceTo
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BarListViewModel(
    val repository: FirestoreRepository,
    private val savedStateHandle: SavedStateHandle,
    val locationService: LocationService
) : ViewModel() {

    private val _bars = MutableStateFlow<List<Bar>>(emptyList())
    val bars: StateFlow<List<Bar>> = _bars

    private val _nearestBar = MutableLiveData<Bar?>()
    val nearestBar: LiveData<Bar?> get() = _nearestBar

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private var lastLocation: Point? = null

    init {
        startLocationUpdates()  // Start location updates when ViewModel is created
        fetchPosts()
    }

    private fun startLocationUpdates() {
        locationService.startLocationUpdates()

        // Observe location updates from LocationService (only once)
        locationService.locationLiveData.observeForever { location ->
            location?.let {
                if (lastLocation == null) {
                    fetchBars(it)
                    fetchNearestBar(it)
                    lastLocation = it
                } else {
                    val distance = location.distanceTo(lastLocation!!)
                    if (distance > 0.1) {  // 0.1 mile threshold
                        fetchBars(it)
                        fetchNearestBar(it)
                        lastLocation = it
                    }
                }
            }
        }
    }

    // Fetches the nearest bar using the user's current location
    fun fetchNearestBar(location: Point) {
        viewModelScope.launch {
            try {
                val nearestBar = repository.getNearestBar(location)
                _nearestBar.postValue(nearestBar)
            } catch (e: Exception) {
                e.printStackTrace()
                _nearestBar.postValue(null)
            }
        }
    }

    // Fetch bars within range based on user location
    fun fetchBars(userLocation: Point) {
        viewModelScope.launch {
            repository.getBarsWithinRange(userLocation)
                .catch { e -> e.printStackTrace() }
                .collect { barsList: List<Bar> ->
                    _bars.value = barsList
                }
        }
    }

    fun getDistanceToBar(barLocation: Point): String? {
        lastLocation?.let { userLocation ->
            val distance = userLocation.distanceTo(barLocation)
            return String.format("%.2f miles", distance)
        }
        return null
    }

    fun fetchPosts() {
        viewModelScope.launch {
            try {
                val postsList = repository.getPosts()
                _posts.value = postsList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBarByID(id: String?): Bar? {
        return _bars.value.find { it.id == id }
    }

    fun getPostById(id: String?): Post? {
        return _posts.value.find { it.id == id }
    }

    suspend fun getUsername(userID: String): String? {
        return repository.getUsername(userID)
    }

    suspend fun getUserProfilePicture(uid: String) {
        try {
            repository.getUserProfilePicture(uid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Make sure to remove observer when ViewModel is cleared to avoid memory leaks
    override fun onCleared() {
        super.onCleared()
        locationService.locationLiveData.removeObserver { location -> }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val savedStateHandle = extras.createSavedStateHandle()
                val applicationContext = extras[APPLICATION_KEY]?.applicationContext
                val locationService = applicationContext?.let { LocationService(it) }

                // Return BarListViewModel with FirestoreRepository and LocationService
                return BarListViewModel(
                    repository = FirestoreRepository(),
                    savedStateHandle = savedStateHandle,
                    locationService = locationService!!
                ) as T
            }
        }
    }
}
