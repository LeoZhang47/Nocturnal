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
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.nocturnal.R
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nocturnal.ui.activity.CameraActivity.Companion.MIN_DISTANCE
import kotlin.math.abs


class BarListActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private val barListViewModel: BarListViewModel by viewModels { BarListViewModel.Factory }

    private lateinit var gestureDetector: GestureDetector
    private var x1 = 0.0f
    private var x2 = 0.0f
    private var y1 = 0.0f
    private var y2 = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_list) // Ensures layout is set with BottomNavigationView

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        gestureDetector = GestureDetector(this, this)

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
            NavHost(navController = navController, startDestination = "list") {
                composable("list") { BarListView(navController) }
                composable("barDetail/{barID}") { backStackEntry ->
                    val barID = backStackEntry.arguments?.getString("barID")
                    val bar: Bar? = barID?.let { barListViewModel.getBarByID(it) }
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        when (event?.action) {
            0 -> {
                x1=event.x
                y1=event.y
            }
            1 -> {
                x2=event.x
                y2 = event.y
                val valueX = x2-x1
                if (abs(valueX) > MIN_DISTANCE) {
                    if (x2 < x1) {
                        startActivity(Intent(this, CameraActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) { }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}
}