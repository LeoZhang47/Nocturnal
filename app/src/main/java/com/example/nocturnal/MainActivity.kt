package com.example.nocturnal.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nocturnal.R
import com.example.nocturnal.ui.login.LoginFragment
import com.example.nocturnal.ui.theme.NocturnalTheme
import com.example.nocturnal.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge mode
        enableEdgeToEdge()

        // Optionally, load the fragment view instead of Compose-based view
        // Uncomment if you need to use fragments:
        // setContentView(R.layout.activity_main)
        // setupFragment()

        // Compose-based content
        setContent {
            NocturnalTheme {
                MyAppScaffold()
            }
        }
    }

    // Fragment setup, only used if you need to integrate both fragments and Compose
    private fun setupFragment() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = LoginFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppScaffold() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Nocturnal") },
                actions = {
                    // Conditionally show the settings button based on current route
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    if (currentRoute != "profile") {
                        IconButton(onClick = {
                            navController.navigate("profile")
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.settings_24px),
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AppNavigation(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            Greeting("Android")
        }
        composable("profile") {
            ProfileScreen(navController)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Hello $name!",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    NocturnalTheme {
        ProfileScreen(rememberNavController())
    }
}
