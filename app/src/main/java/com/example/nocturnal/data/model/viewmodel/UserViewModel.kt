package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


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
}

