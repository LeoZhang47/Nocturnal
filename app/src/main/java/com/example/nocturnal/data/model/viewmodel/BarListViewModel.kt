package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BarListViewModel(
    private val repository: FirestoreRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
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
                onError = { e ->
                    e.printStackTrace()
                }
            )
        }
    }

    fun getBarByID(id: String? ): Bar? {
        return _bars.value.find { it.id == id }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val savedStateHandle = extras.createSavedStateHandle()

                return BarListViewModel(
                    FirestoreRepository(),
                    savedStateHandle
                ) as T
            }
        }
    }
}

