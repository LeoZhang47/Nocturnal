package com.example.nocturnal.data

import android.net.Uri
import android.util.Log
import com.example.nocturnal.data.model.Bar
import com.example.nocturnal.data.model.Post
import com.example.nocturnal.data.model.distanceTo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    suspend fun storeUsername(uid: String, username: String) {
        val user = hashMapOf("username" to username)
        db.collection("users").document(uid).set(user).await()
    }

    suspend fun storeScore(uid: String, score: Int) {
        val userScore = hashMapOf("score" to score)
        db.collection("users").document(uid)
            .set(userScore, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun incrementUserScore(uid: String, incrementBy: Int = 1) {
        val userRef = db.collection("users").document(uid)
        val document = userRef.get().await()
        val currentScore = document.getLong("score")?.toInt() ?: 0
        val newScore = currentScore + incrementBy
        userRef.update("score", newScore).await()
    }

    fun getBarsWithinRange(userLocation: Point) = flow {
        try {
            val result = db.collection("bars").get().await()
            val bars = result.mapNotNull { document ->
                val barGeoPoint = document.getGeoPoint("location")
                barGeoPoint?.let {
                    val barLocation = Point.fromLngLat(it.longitude, it.latitude)
                    val distance = userLocation.distanceTo(barLocation)
                    if (distance <= 1) {
                        Bar(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            location = barGeoPoint,
                            postIDs = (document.get("postIDs") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    } else null
                }
            }
            emit(bars) // Emit the list of bars
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList<Bar>()) // Emit an empty list in case of an error
        }
    }

    fun getPosts(): Flow<Post> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val postsCollection = db.collection("posts")

        val listener = postsCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception) // Close the flow in case of an error
                return@addSnapshotListener
            }

            snapshot?.let {
                for (document in it.documents) {
                    val post = Post(
                        id = document.id,
                        media = document.getString("media")
                            ?: "https://firebasestorage.googleapis.com/v0/b/nocturnal-18a34.appspot.com/o/images%2Ferror%2FDefaultImage.png?alt=media&token=6c8e7702-287a-4f08-8c41-9a09aeda8afc",
                        timestamp = document.getTimestamp("timestamp") ?: Timestamp.now(),
                        userID = document.getString("user") ?: "",
                        barID = document.getString("bar") ?: ""
                    )
                    trySend(post) // Emit each post to the flow
                }
            }
        }

        awaitClose { listener.remove() } // Clean up the listener when the flow is canceled
    }

    suspend fun getUsername(uid: String): String? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                document.getString("username")
            } else {
                null
            }
        } catch (e: Exception) {
            e.message?.let { Log.d("Error getting post's username", it) }
            return null
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

        val postRef = db.collection("posts").document()

        postRef.set(postData)
            .addOnSuccessListener {
                onSuccess(postRef.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    suspend fun getNearestBar(userLocation: Point): Bar? {
        val result = db.collection("bars").get().await()
        var nearestBar: Bar? = null
        var shortestDistance = Double.MAX_VALUE

        for (document in result) {
            val barGeoPoint = document.getGeoPoint("location")
            barGeoPoint?.let {
                val barLocation = Point.fromLngLat(it.longitude, it.latitude)
                val distance = userLocation.distanceTo(barLocation)
                if (distance < shortestDistance) {
                    shortestDistance = distance
                    nearestBar = document.toObject(Bar::class.java).copy(id = document.id)
                }
            }
        }
        return nearestBar
    }

    fun getUserPosts(uid: String): Flow<String> = flow {
        val userImagesRef = storageRef.child("images/$uid/")
        val listResult = userImagesRef.listAll().await()
        for (item in listResult.items) {
            val url = item.downloadUrl.await().toString()
            emit(url) // Emit each URL as it is fetched
        }
    }.catch { e ->
        // Handle any errors that occur
        Log.e("getUserPostsAsFlow", "Error fetching user posts", e)
        throw e
    }.flowOn(Dispatchers.IO) // Perform on a background thread


    suspend fun getUserProfilePicture(uid: String): String {
        val userPfpRef = storageRef.child("profile-pictures/pfp-$uid")
        return try {
            userPfpRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            val defaultPfpRef = storageRef.child("profile-pictures/nocturnal_default_pfp.xml/")
            defaultPfpRef.downloadUrl.await().toString()
        }
    }

    suspend fun updateUserProfilePicture(uid: String, imageUri: Uri): String {
        val userPfpRef = storageRef.child("profile-pictures/pfp-$uid")
        userPfpRef.putFile(imageUri).await()
        return userPfpRef.downloadUrl.await().toString()
    }

    fun updateBarPostIds(barId: String, postId: String, onComplete: (Boolean, Exception?) -> Unit) {
        val barRef = db.collection("bars").document(barId)
        barRef.update("postIDs", FieldValue.arrayUnion(postId))
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { exception -> onComplete(false, exception) }
    }

    suspend fun getUserScore(uid: String): String {
        return try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                val score = document.getLong("score")?.toInt() ?: 0
                score.toString()
            } else {
                "0"
            }
        } catch (e: Exception) {
            e.message ?: "Failed to retrieve score"
        }
    }
}
