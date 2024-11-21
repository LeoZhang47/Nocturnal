package com.example.nocturnal.data.model.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository = FirestoreRepository()

    // Login user
    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    // Sign out user
    fun signOut() {
        auth.signOut()
    }

    // Register a new user
    fun registerUser(
        email: String,
        password: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    callback(true, null, uid)
                } else {
                    callback(false, task.exception?.message, null)
                }
            }
    }

    // Store username in Firestore
    fun storeUsername(uid: String, username: String) {
        viewModelScope.launch {
            try {
                repository.storeUsername(uid, username)
            } catch (e: Exception) {
                e.message?.let { Log.d("Error storing username", it) }
            }
        }
    }

    // Store user's score in Firestore
    fun storeScore(uid: String, score: Int) {
        viewModelScope.launch {
            try {
                repository.storeScore(uid, score)
            } catch (e: Exception) {
                e.message?.let { Log.d("Error storing score", it) }
            }
        }
    }

    // Increment user's score
    fun incrementUserScore(incrementBy: Int = 1, callback: (Boolean, String?) -> Unit) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    repository.incrementUserScore(currentUser.uid, incrementBy)
                    callback(true, null)
                } catch (e: Exception) {
                    callback(false, e.message)
                }
            }
        }

    }

    // Get current Firebase user
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Fetch username from Firestore
    fun getUsername(uid: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val username = repository.getUsername(uid)
                callback(username)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    // Change username
    fun changeUsername(uid: String, newUsername: String) {
        viewModelScope.launch {
            try {
                repository.storeUsername(uid, newUsername)
            } catch (e: Exception) {
                e.message?.let { Log.d("Error changing username:", it) }
            }
        }
    }

    // Change user's password
    fun changePassword(newPassword: String) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    } else {
                        task.exception?.message?.let { Log.d("Error changing password", it) }
                    }
                }
        } else {
            Log.d("Error changing password", "No user logged in")
        }
    }

    // Get user's profile picture URL
    fun getUserProfilePicture(uid: String) {
        viewModelScope.launch {
            try {
                val url = repository.getUserProfilePicture(uid)
            } catch (e: Exception) {
                e.message?.let { Log.d("Error getting user profile picture", it) }
            }
        }
    }

    private val _imageUrls = MutableLiveData<List<String>>()
    val imageUrls: LiveData<List<String>> get() = _imageUrls

    fun getUserPosts() {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            // Use viewModelScope to launch a coroutine for a suspend function
            viewModelScope.launch {
                try {
                    // Call the suspend function from repository
                    val posts = repository.getUserPosts(uid)
                    _imageUrls.value = posts // Set the fetched posts to _imageUrls
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error fetching posts: ${e.message}")
                    _imageUrls.value = emptyList() // Set empty list on error
                }
            }
        } else {
            Log.e("UserViewModel", "No user logged in")
            _imageUrls.value = emptyList() // Set empty list if no user is logged
        }
    }

}
