package com.example.nocturnal.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    //fun getUsers() = db.collection("users").get()
    fun addUser(user: Map<String, Any>) = db.collection("users").add(user)

    fun validateUserCredentials(username: String, password: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    callback(true)  // User exists
                } else {
                    callback(false) // User does not exist
                }
            }
            .addOnFailureListener {
                callback(false) // Error or failure
            }
    }
}