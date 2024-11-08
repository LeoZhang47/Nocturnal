package com.example.nocturnal.data.model.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UserViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository = FirestoreRepository()

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)  // Login successful
                } else {
                    callback(false, task.exception?.message)  // Login failed
                }
            }
    }

    // Function to sign out the current user
    fun signOut() {
        auth.signOut()
        // You may want to clear any locally stored user data or update UI state as needed
    }

    fun registerUser(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    callback(true, null, uid)
                } else {
                    callback(false, task.exception?.message, null)
                }
            }
    }


    fun storeUsername(uid: String, username: String) {
        repository.storeUsername(uid, username,
            onSuccess = {
                Log.d("storeUsername","Username stored successfully")
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
    }

    fun storeScore(uid: String, score: Int) {
        repository.storeScore(uid, score,
            onSuccess = {
                // Success logic
            },
            onFailure = { exception ->
                // Handle error
            }
        )
    }

    // Expose the current FirebaseAuth instance
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Fetch the username using the FirestoreRepository
    fun getUsername(uid: String): StateFlow<String> {
        val usernameFlow = MutableStateFlow("Loading...")  // Initial state as loading

        repository.getUsername(
            uid = uid,
            onSuccess = { username ->
                usernameFlow.value = username
            },
            onFailure = { exception ->
                usernameFlow.value = "Error: ${exception.message}"
            }
        )

        return usernameFlow
    }

    fun getUserScore(onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            val firestore = FirebaseFirestore.getInstance()

            // Access user's document in Firestore
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Check if the document has a "score" field
                        val score = document.getLong("score")?.toInt() ?: 0
                        onSuccess(score)  // Return the score or 0 if score is null
                    } else {
                        onSuccess(0)  // Document does not exist, return 0
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception.message ?: "Failed to retrieve score")
                }
        } else {
            onFailure("No user logged in")
        }
    }

    // Method to change the username
    fun changeUsername(newUsername: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            repository.storeUsername(uid, newUsername,
                onSuccess = {
                    onSuccess()  // Username updated successfully
                },
                onFailure = { exception ->
                    onFailure(exception.message ?: "Failed to update username")
                }
            )
        } else {
            onFailure("No user logged in")
        }
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()  // Password updated successfully
                    } else {
                        onFailure(task.exception?.message ?: "Failed to update password")
                    }
                }
        } else {
            onFailure("No user logged in")
        }
    }

    fun getUserProfilePicture(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        repository.getUserProfilePicture(
            uid,
            onSuccess = { url -> onSuccess(url) },
            onFailure = { exception -> onFailure(exception) }
        )
    }

    private val _imageUrls = MutableLiveData<List<String>>()
    val imageUrls: LiveData<List<String>> get() = _imageUrls

    fun getUserPosts() {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            repository.getUserPosts(
                uid,
                onSuccess = { urls ->
                    _imageUrls.value = urls  // Update LiveData with retrieved image URLs
                },
                onFailure = { exception ->
                    Log.e("UserViewModel", "Failed to retrieve user posts: ${exception.message}")
                    _imageUrls.value = emptyList() // Set empty list on failure
                }
            )
        } else {
            Log.e("UserViewModel", "No user logged in")
            _imageUrls.value = emptyList() // Set empty list if no user is logged in
        }
    }
}

