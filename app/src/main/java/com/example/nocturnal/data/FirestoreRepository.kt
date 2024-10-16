package com.example.nocturnal.data

import com.example.nocturnal.data.model.viewmodel.Bar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun storeUsername(uid: String, username: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = hashMapOf("username" to username)
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getBars(onResult: (List<Bar>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("bars")
            .get()
            .addOnSuccessListener { result ->
                val barsList = result.map { document ->
                    Bar(
                        name = document.getString("name") ?: "",
                        location = document.getGeoPoint("location")
                    )
                }
                onResult(barsList)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }



    // Fetch username from Firestore using uid
    fun getUsername(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown User"
                    onSuccess(username)
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun storePost(media: String, timestamp: Date) {
        val media = hashMapOf("media" to media, "timestamp" to timestamp)
        db.collection("posts").document()
            .set(media)
    }
}
