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



    fun storePost(media: String, timestamp: Date) {
        val media = hashMapOf("media" to media, "timestamp" to timestamp)
        db.collection("posts").document()
            .set(media)
    }
}
