package com.example.nocturnal.data.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import androidx.lifecycle.liveData
import com.google.firebase.auth.FirebaseAuth

class UserViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)  // Registration successful
                } else {
                    callback(false, task.exception?.message)  // Registration failed
                }
            }
    }
}

