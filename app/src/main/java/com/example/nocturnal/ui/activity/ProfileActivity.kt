package com.example.nocturnal.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.example.nocturnal.ProfileScreen

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Pass the finish() call to close the activity when back is pressed
                ProfileScreen(onBackClick = { finish() })
            }
        }
    }
}
