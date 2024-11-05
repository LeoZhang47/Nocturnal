package com.example.nocturnal.data

import com.example.nocturnal.data.Bar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import android.net.Uri

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

    fun storeScore(uid: String, score: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userScore = hashMapOf("score" to score)
        db.collection("users").document(uid)
            .set(userScore, com.google.firebase.firestore.SetOptions.merge())  // Merge the score with existing data
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getBars(onResult: (List<Bar>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("bars")
            .get()
            .addOnSuccessListener { result ->
                val barsList = result.map { document ->
                    Bar(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        location = document.getGeoPoint("location"),
                        postIDs = if (document.get("postIDs") is List<*>) {
                            (document.get("postIDs") as List<*>).filterIsInstance<String>()
                        } else {
                            emptyList()
                        }
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
        val userImagesRef = storageRef.child("images/$uid/")

        // Check if the folder exists by listing its contents
        userImagesRef.listAll()
            .addOnSuccessListener { listResult ->
                val imageUrls = mutableListOf<String>()

                // If the folder exists, retrieve image URLs
                if (listResult.items.isNotEmpty()) {
                    val tasks = listResult.items.map { storageReference ->
                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(uri.toString())
                        }
                    }

                    // After all URLs are retrieved, call the success callback
                    tasks.lastOrNull()?.addOnCompleteListener {
                        onSuccess(imageUrls)
                    }
                } else {
                    // No images found, return an empty list
                    onSuccess(imageUrls) // This ensures that the UI renders correctly
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getUserProfilePicture(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        // Reference to the user's profile picture in the "profile-pictures" folder
        val userPfpRef = storageRef.child("profile-pictures/pfp-$uid")

        // Attempt to get the download URL of the user-specific profile picture
        userPfpRef.downloadUrl
            .addOnSuccessListener { uri ->
                // User-specific profile picture exists, return its URL
                onSuccess(uri.toString())
            }
            .addOnFailureListener {
                // If the user-specific profile picture is not found, get the default profile picture
                val defaultPfpRef = storageRef.child("profile-pictures/nocturnal-defualt-pfp.png/")
                defaultPfpRef.downloadUrl
                    .addOnSuccessListener { defaultUri ->
                        // Return the default profile picture URL
                        onSuccess(defaultUri.toString())
                    }
                    .addOnFailureListener { defaultException ->
                        // If both the user-specific and default images fail, report an error
                        onFailure(defaultException)
                    }
            }
    }

    fun updateUserProfilePicture(uid: String, imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        // Reference to where the profile picture will be stored
        val userPfpRef = storageRef.child("profile-pictures/pfp-$uid")

        // Upload the image to Firebase Storage
        userPfpRef.putFile(imageUri)
            .addOnSuccessListener {
                // After upload, get the download URL
                userPfpRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        // Return the download URL as a success callback
                        onSuccess(uri.toString())
                    }
                    .addOnFailureListener { exception ->
                        // Failed to get the download URL after upload
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                // Failed to upload the image
                onFailure(exception)
            }
    }


}
