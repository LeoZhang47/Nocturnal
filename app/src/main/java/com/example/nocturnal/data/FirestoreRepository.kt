package com.example.nocturnal.data

import com.example.nocturnal.data.model.viewmodel.Bar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.util.UUID

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference


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

    fun storePost(media: String, timestamp: Date, uid: String /*, onSuccess: () -> Unit, onFailure: (Exception) -> Unit*/) {
        val media = hashMapOf("media" to media, "timestamp" to timestamp, "user" to uid )
        db.collection("posts").document()
            .set(media)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getUserPosts(uid: String, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val userImagesRef = storageRef.child("images/${uid}/")

        userImagesRef.listAll()
            .addOnSuccessListener { listResult ->
                val imageUrls = mutableListOf<String>()
                val tasks = listResult.items.map { storageReference ->
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                    }
                }

                // After all URLs are retrieved, call the success callback
                tasks.lastOrNull()?.addOnCompleteListener {
                    onSuccess(imageUrls)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
