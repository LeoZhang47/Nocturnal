package com.example.nocturnal.data.model

import com.google.firebase.Timestamp

data class Post(
    val id: String? = null,
    val media: String = "",
    val timestamp: Timestamp,
    val userID: String,
    val barID: String
)
