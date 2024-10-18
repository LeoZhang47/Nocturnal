package com.example.nocturnal.data

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

    fun storePost(media: String, timestamp: Date) {
        val media = hashMapOf("media" to media, "timestamp" to timestamp)
        db.collection("posts").document()
            .set(media)
    }
}
