package com.example.nocturnal.data.model

data class User(
    var username: String,
    var postIDs: List<String> = emptyList(),
    var score: Int = 0
)
