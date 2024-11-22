package com.example.nocturnal.data.model.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _imageUrls = MutableStateFlow<List<String>>(emptyList())
    val imageUrls: StateFlow<List<String>> get() = _imageUrls

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

//    private val _profilePicture = MutableLiveData<String>("")
//    val profilePicture: LiveData<String> get() = _profilePicture

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

    fun signOut() {
        auth.signOut()
    }

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

    fun storeUsername(uid: String, username: String) {
        viewModelScope.launch {
            try {
                repository.storeUsername(uid, username)
            } catch (e: Exception) {
                e.message?.let { Log.d("Error storing username", it) }
            }
        }
    }

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
    fun changeUsername(uid: String, newUsername: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.storeUsername(uid, newUsername)
                callback(true, "")
            } catch (e: Exception) {
                e.message?.let {
                    Log.d("Error changing username:", it)
                    callback(false, it)
                }

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
                _profilePictureUrl.value = url
            } catch (e: Exception) {
                e.message?.let { Log.d("Error getting user profile picture", it) }
            }
        }
    }

    fun getUserScore(onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            viewModelScope.launch {
                try {
                    val result = repository.getUserScore(uid)
                    onSuccess(result.toInt())
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error fetching posts: ${e.message}")
                    e.message?.let { onFailure(it) }
                }
            }

        } else {
            onFailure("No user logged in")
        }
    }

    fun getUserPosts() {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            val uid = currentUser.uid
            viewModelScope.launch {
                try {
                    // Collect the flow and update _imageUrls incrementally
                    repository.getUserPosts(uid)
                        .collect { imageUrl ->
                            // Append each imageUrl to the existing list
                            _imageUrls.value = _imageUrls.value.plus(imageUrl)
                        }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error fetching posts: ${e.message}")
                    _imageUrls.value = emptyList() // Clear the list on error
                }
            }
        } else {
            Log.e("UserViewModel", "No user logged in")
            _imageUrls.value = emptyList() // Clear the list if no user is logged in
        }
    }


}
