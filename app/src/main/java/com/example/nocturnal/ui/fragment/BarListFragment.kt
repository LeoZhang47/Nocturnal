package com.example.nocturnal.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nocturnal.R
import com.example.nocturnal.data.model.Bar
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import com.example.nocturnal.ui.screen.BarDetailScreen
import com.example.nocturnal.ui.screen.BarListView

class BarListFragment : Fragment() {

    private val viewModel: BarListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // Enables the options menu

        viewModel.startLocationUpdates()

        val view = inflater.inflate(R.layout.fragment_bar_list, container, false)
        view.findViewById<ComposeView>(R.id.composable_container).setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "list") {
                composable("list") { BarListView(navController, viewModel, requireContext()) }
                composable("barDetail/{barID}") { backStackEntry ->
                    val barID = backStackEntry.arguments?.getString("barID")
                    val bar: Bar? = barID?.let { viewModel.getBarByID(it) }
                    BarDetailScreen(bar, viewModel)
                }
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        refreshBars()
    }

    private fun refreshBars() {
        viewModel.locationService.getLastKnownLocation { location ->
            location?.let {
                viewModel.fetchBars(it)
            }
        }
    }

}
