package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import androidx.lifecycle.liveData


class UserViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    fun addUser(user: Map<String, Any>) {
        repository.addUser(user)
    }
    fun validateCredentials(username: String, password: String, callback: (Boolean) -> Unit) {
        repository.validateUserCredentials(username, password, callback)
    }
}
