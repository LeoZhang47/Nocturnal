package com.example.nocturnal.data

import com.example.nocturnal.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import android.net.Uri
import com.example.nocturnal.data.model.distanceTo
import com.mapbox.geojson.Point
import android.util.Log


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

    fun getPosts(onResult: (List<Post>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                val postsList = result.map { document ->
                    Post(
                        id = document.id,
                        media = document.getString("media") ?: "https://firebasestorage.googleapis.com/v0/b/nocturnal-18a34.appspot.com/o/images%2Ferror%2FDefaultImage.png?alt=media&token=6c8e7702-287a-4f08-8c41-9a09aeda8afc",
                        timestamp = document.getTimestamp("timestamp") ?: Timestamp.now(),
                        userID = document.getString("userID") ?: "",
                        barID = document.getString("barID") ?: ""
                    )
                }
                onResult(postsList)
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


    fun storePost(
        media: String,
        timestamp: Date,
        uid: String,
        barID: String,
        onSuccess: (postId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val postData = hashMapOf(
            "media" to media,
            "timestamp" to timestamp,
            "user" to uid,
            "bar" to barID
        )

        // Generate a document reference for the post
        val postRef = db.collection("posts").document()

        // Set the data and handle the success and failure cases
        postRef.set(postData)
            .addOnSuccessListener {
                // Return the postId back to the caller
                onSuccess(postRef.id)
            }
            .addOnFailureListener { exception ->
                // Handle the error and pass the exception to the failure callback
                onFailure(exception)
            }
    }


    // Fetch the nearest bar based on the provided location
    fun getNearestBar(userLocation: Point, onResult: (Bar?) -> Unit, onError: (Exception) -> Unit) {
        // Step 2: Fetch bars from Firestore
        db.collection("bars").get()
            .addOnSuccessListener { result ->
                var nearestBar: Bar? = null
                var shortestDistance = Double.MAX_VALUE

                // Iterate through all bars and find the nearest one
                for (document in result) {
                    val barGeoPoint = document.getGeoPoint("location")
                    if (barGeoPoint != null) {
                        // Convert GeoPoint to Point for distance calculation
                        val barLocation = Point.fromLngLat(barGeoPoint.longitude, barGeoPoint.latitude)
                        val distance = userLocation.distanceTo(barLocation)

                        // Update the nearest bar if this one is closer
                        if (distance < shortestDistance) {
                            shortestDistance = distance
                            nearestBar = document.toObject(Bar::class.java).copy(id = document.id)
                        }
                    }
                }

                onResult(nearestBar) // Return the nearest bar

            }
            .addOnFailureListener { exception ->
                onError(exception) // Handle errors
            }
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

    fun updateBarPostIds(barId: String, postId: String, onComplete: (Boolean, Exception?) -> Unit) {
        val barRef = db.collection("bars").document(barId)
        barRef.update("postIDs", com.google.firebase.firestore.FieldValue.arrayUnion(postId))
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { exception -> onComplete(false, exception) }
    }

}
