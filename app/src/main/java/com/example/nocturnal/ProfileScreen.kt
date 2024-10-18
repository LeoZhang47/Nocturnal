package com.example.nocturnal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nocturnal.ui.fragment.LoginFragment
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.collectAsState
import com.example.nocturnal.ui.activity.ProfileActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    fragmentManager: FragmentManager,
    userViewModel: UserViewModel = viewModel(),
    profileActivity: ProfileActivity // Pass ProfileActivity to trigger logout
) {
    val currentUser = userViewModel.getCurrentUser()

    // If currentUser is null, handle it with a fallback (e.g., "Guest")
    val usernameFlow = currentUser?.uid?.let { userViewModel.getUsername(it) } ?: MutableStateFlow("Guest")

    // Collect the StateFlow safely
    val username by usernameFlow.collectAsState()

    // Track dialog visibility and the entered username
    var showDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
        ) {
            // "Profile" Heading
            Text(
                text = username,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            // Display user score
            Text(
                text = "Score: XX",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            // Change Username Button
            Button(
                onClick = { showDialog = true }, // Show the dialog
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Change Username")
            }

            // Change Profile Picture Button
            Button(
                onClick = { /* Handle Change Profile Picture */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Change PFP")
            }

            // Change Password Button
            Button(
                onClick = { /* Handle Change Password */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Change Password")
            }

            // Log Out Button
            Button(
                onClick = {
                    userViewModel.signOut() // Call the ViewModel's log out function
                    profileActivity.logOutAndNavigateToLogin() // Log out and navigate back to LoginFragment
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Log Out")
            }
        }

        // Dialog for entering new username
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Change Username") },
                text = {
                    Column {
                        TextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("Enter new username") },
                            singleLine = true
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        currentUser?.uid?.let { uid ->
                            userViewModel.changeUsername(
                                newUsername,
                                onSuccess = {
                                    // Refetch username after successfully changing it
                                    userViewModel.getUsername(uid)
                                    showDialog = false // Close dialog on success
                                    errorMessage = ""  // Clear any previous error message
                                },
                                onFailure = { error ->
                                    errorMessage = error // Show error message if any
                                }
                            )
                        }
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
