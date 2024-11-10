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
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.FirestoreRepository
import com.example.nocturnal.data.model.Post
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        startLocationUpdates()  // Start location updates when ViewModel is created
        fetchBars()
        fetchPosts()
    }

    private fun startLocationUpdates() {
        locationService.startLocationUpdates()
        // Observe location updates from LocationService
        locationService.locationLiveData.observeForever { location ->
            location?.let {
                fetchNearestBar(it)
            }
        }
    }

    // Fetches the nearest bar using the user's current location
    fun fetchNearestBar(location: Point) {
        viewModelScope.launch {
            repository.getNearestBar(
                userLocation = location,
                onResult = { bar ->
                    _nearestBar.postValue(bar)
                },
                onError = { e ->
                    e.printStackTrace()
                    _nearestBar.postValue(null)
                }
            )
        }
    }

    private fun fetchBars() {
        viewModelScope.launch {
            repository.getBars(
                onResult = { barsList ->
                    _bars.value = barsList
                },
                onError = { e ->
                    e.printStackTrace()
                }
            )
        }
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            repository.getPosts(
                onResult = { postsList ->
                    _posts.value = postsList
                },
                onError = { e ->
                    e.printStackTrace()
                }
            )
        }
    }

    fun getBarByID(id: String?): Bar? {
        return _bars.value.find { it.id == id }
    }

    fun getPostById(id: String?): Post? {
        return _posts.value.find { it.id == id }
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
