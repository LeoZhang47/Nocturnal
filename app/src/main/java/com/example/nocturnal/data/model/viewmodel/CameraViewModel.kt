package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nocturnal.R

class CameraViewModel : ViewModel() {
    private val _isWithinRange = MutableLiveData<Boolean>()
    val isWithinRange: LiveData<Boolean> get() = _isWithinRange

    var fragment: Int = R.id.navigation_camera

    fun setWithinRange(value: Boolean) {
        _isWithinRange.value = value
    }
}
