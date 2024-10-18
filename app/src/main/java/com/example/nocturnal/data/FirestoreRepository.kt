package com.example.nocturnal.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    //fun getUsers() = db.collection("users").get()
    fun addUser(user: Map<String, Any>) = db.collection("users").add(user)
}