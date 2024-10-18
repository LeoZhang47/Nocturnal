package com.example.nocturnal.ui.activity

import BarDetailScreen
import BarListView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class BarListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "list") {
                composable("list") { BarListView(navController) }
                composable("barDetail/{barName}/{latitude}/{longitude}") { backStackEntry ->
                    val barName = backStackEntry.arguments?.getString("barName")
                    val latitude = backStackEntry.arguments?.getDouble("latitude")
                    val longitude = backStackEntry.arguments?.getDouble("longitude")
                    BarDetailScreen(barName, latitude, longitude)
                }
            }
        }
    }
}