package com.example.nocturnal.ui.activity

import BarDetailScreen
import BarListView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.model.viewmodel.BarListViewModel

class BarListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: BarListViewModel by viewModels { BarListViewModel.Factory }
            NavHost(navController = navController, startDestination = "list") {
                composable("list") { BarListView(navController) }
                composable("barDetail/{barID}") { backStackEntry ->
                    val barID = backStackEntry.arguments?.getString("barID")
                    // get bar from ID
                    val bar : Bar? = viewModel.getBarByID(barID)

                    BarDetailScreen(bar)
                }
            }
        }
    }
}