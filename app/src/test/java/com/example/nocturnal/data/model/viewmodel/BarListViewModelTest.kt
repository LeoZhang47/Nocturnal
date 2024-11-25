package com.example.nocturnal.data.model.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.nocturnal.data.FirestoreRepository
import com.example.nocturnal.data.model.Bar
import com.example.nocturnal.service.LocationService
import com.example.nocturnal.util.distanceTo
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BarListViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepository: FirestoreRepository
    private lateinit var mockLocationService: LocationService
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var barListViewModel: BarListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Set Main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Mock dependencies
        mockRepository = mock(FirestoreRepository::class.java)
        mockLocationService = mock(LocationService::class.java)
        savedStateHandle = SavedStateHandle()

        // Mock getLocationLiveData to return a valid LiveData object
        val mockLiveData = MutableLiveData<Point>()
        mockLiveData.value = Point.fromLngLat(-83.0301, 40.0076)
        whenever(mockLocationService.locationLiveData).thenReturn(mockLiveData)

        // Create the ViewModel with mocks
        barListViewModel = BarListViewModel(
            repository = mockRepository,
            savedStateHandle = savedStateHandle,
            locationService = mockLocationService
        )
    }


    @After
    fun tearDown() {
        // Reset Main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchNearestBar sets nearestBar LiveData successfully`() = runTest {
        // Arrange
        val mockLocation = GeoPoint(-83.0301, 40.0076)
        val expectedNearestBar = Bar(id = "1", name = "Test Bar", location = mockLocation)
        val mockPoint = Point.fromLngLat(-83.0301, 40.0076)
        whenever(mockRepository.getNearestBar(mockPoint)).thenReturn(expectedNearestBar)

        // Act
        barListViewModel.fetchNearestBar(mockPoint)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(expectedNearestBar, barListViewModel.nearestBar.value)
    }

    @Test
    fun `fetchBars sets bars StateFlow successfully`() = runTest {
        // Arrange
        val mockLocation = GeoPoint(-83.0301, 40.0076)
        val mockPoint = Point.fromLngLat(-83.0301, 40.0076)
        val expectedBars = listOf(
            Bar(id = "1", name = "Bar One", location = mockLocation),
            Bar(id = "2", name = "Bar Two", location = mockLocation)
        )
        whenever(mockRepository.getBarsWithinRange(mockPoint)).thenReturn(flow { emit(expectedBars) })

        // Act
        barListViewModel.fetchBars(mockPoint)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(expectedBars, barListViewModel.bars.value)
    }

    @Test
    fun `getDistanceToBar calculates correct distance`() {
        // Arrange
        val userLocation = Point.fromLngLat(-83.0301, 40.0076)
        val barLocation = Point.fromLngLat(-83.0305, 40.0090)
        barListViewModel.lastLocation = userLocation
        val expectedDistance = userLocation.distanceTo(barLocation)

        // Act
        val result = barListViewModel.getDistanceToBar(barLocation)

        // Assert
        assertEquals(String.format("%.2f miles", expectedDistance), result)
    }

    @Test
    fun `getBarByID returns correct Bar`() {
        // Arrange
        val mockBars = listOf(
            Bar(id = "1", name = "Bar One", location = GeoPoint(-83.0301, 40.0076)),
            Bar(id = "2", name = "Bar Two", location = GeoPoint(-83.0305, 40.0090))
        )
        barListViewModel._bars.value = mockBars

        // Act
        val result = barListViewModel.getBarByID("1")

        // Assert
        assertEquals(mockBars[0], result)
    }

    @Test
    fun `fetchNearestBar handles exception gracefully`() = runTest {
        // Arrange
        val mockLocation = Point.fromLngLat(-83.0301, 40.0076)
        whenever(mockRepository.getNearestBar(mockLocation)).thenThrow(RuntimeException("Error fetching nearest bar"))

        // Act
        barListViewModel.fetchNearestBar(mockLocation)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(null, barListViewModel.nearestBar.value)
    }
}
