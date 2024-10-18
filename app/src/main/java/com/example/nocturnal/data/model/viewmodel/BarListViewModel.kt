package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Bar(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: GeoPoint ?= null
)

class BarListViewModel() : ViewModel() {
    private val repository = FirestoreRepository()
    private val _bars = MutableStateFlow<List<Bar>>(emptyList())
    val bars: StateFlow<List<Bar>> = _bars

    init {
        fetchBars()
    }

    private fun fetchBars() {
        viewModelScope.launch {
            repository.getBars(
                onResult = { barsList ->
                    _bars.value = barsList
                },
                onError = { exception ->
                    // Handle the error
                }
            )
        }
    }
}