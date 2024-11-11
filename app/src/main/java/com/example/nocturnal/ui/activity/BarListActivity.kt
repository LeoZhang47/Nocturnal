package com.example.nocturnal.ui.activity

import com.example.nocturnal.ui.screen.BarDetailScreen
import com.example.nocturnal.ui.screen.BarListView
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import com.example.nocturnal.R
import androidx.compose.ui.platform.ComposeView



class BarListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_list) // Ensures layout is set with BottomNavigationView

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set Bar List as the selected item
        bottomNavigationView.selectedItemId = R.id.navigation_bar

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_bar -> true  // Already on Bar List screen
                R.id.navigation_camera -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        // Set content for ComposeView
        findViewById<ComposeView>(R.id.composable_container).setContent {
            val navController = rememberNavController()
            val viewModel: BarListViewModel by viewModels { BarListViewModel.Factory }
            NavHost(navController = navController, startDestination = "list") {
                composable("list") { BarListView(navController) }
                composable("barDetail/{barID}") { backStackEntry ->
                    val barID = backStackEntry.arguments?.getString("barID")
                    val bar: Bar? = barID?.let { viewModel.getBarByID(it) }
                    BarDetailScreen(bar)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set Camera as the selected item when returning to CameraActivity
        bottomNavigationView.selectedItemId = R.id.navigation_bar
    }
}