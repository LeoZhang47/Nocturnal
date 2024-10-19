package com.example.nocturnal.data

import com.google.firebase.firestore.GeoPoint

data class Bar(
    val id: String ?= null,
    val name: String = "",
    val description: String = "",
    val location: GeoPoint?= null,
    var postIDs: List<String> = emptyList()
)
