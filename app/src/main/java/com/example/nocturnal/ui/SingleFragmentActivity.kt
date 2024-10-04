package com.example.nocturnal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nocturnal.R

abstract class SingleFragmentActivity : AppCompatActivity() {

    // Abstract method for subclasses to provide the fragment instance
    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment) // A generic layout file with a FrameLayout

        // Check if fragment is already added to avoid duplication on configuration changes
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, createFragment()) // `fragment_container` is a placeholder in the layout
                .commit()
        }
    }
}