package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import java.util.Date

class PostViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    fun storePost(media: String, timestamp: Date) {
        repository.storePost(media, timestamp)
    }
}