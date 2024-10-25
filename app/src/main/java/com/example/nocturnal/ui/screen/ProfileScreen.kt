import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.nocturnal.R
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import com.example.nocturnal.ui.activity.ProfileActivity
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.text.input.PasswordVisualTransformation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    fragmentManager: FragmentManager,
    profileActivity: ProfileActivity,
    imageUrls: List<String>, // Add image URLs parameter
    userViewModel: UserViewModel = viewModel()
) {
    val currentUser = userViewModel.getCurrentUser()

    // If currentUser is null, handle it with a fallback (e.g., "Guest")
    val usernameFlow = currentUser?.uid?.let { userViewModel.getUsername(it) } ?: MutableStateFlow("Guest")

    // Collect the StateFlow safely
    val username by usernameFlow.collectAsState()

    // Track dialog visibility and the entered username and password
    var showDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }

    // User score state
    var userScore by remember { mutableStateOf(0) }
    var scoreErrorMessage by remember { mutableStateOf("") }

    // Fetch the user score when ProfileScreen is composed
    LaunchedEffect(Unit) {
        userViewModel.getUserScore(
            onSuccess = { score ->
                userScore = score
            },
            onFailure = { error ->
                scoreErrorMessage = error
            }
        )
    }

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
                .padding(16.dp)
        ) {
            // Profile header section with buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
            ) {
                // "Profile" Heading
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                // Display user score (Placeholder)
                Text(
                    text = "Score: ${userScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                // Change Username Button
                Button(
                    onClick = { showDialog = true },
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
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Change Password")
                }

                // Log Out Button
                Button(
                    onClick = {
                        userViewModel.signOut()
                        profileActivity.logOutAndNavigateToLogin()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Log Out")
                }
            }

            // Check if there are images to display
            if (imageUrls.isNotEmpty()) {
                Text(
                    text = "Your Images",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Display user images
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(imageUrls) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                // Show a placeholder or message if there are no images
                Text(
                    text = "No images available",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
                                    userViewModel.getUsername(uid)
                                    showDialog = false
                                    errorMessage = ""
                                },
                                onFailure = { error ->
                                    errorMessage = error
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

        // Dialog for entering new password
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text(text = "Change Password") },
                text = {
                    Column {
                        TextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Enter new password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation() // Mask the input
                        )
                        if (passwordErrorMessage.isNotEmpty()) {
                            Text(
                                text = passwordErrorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        userViewModel.changePassword(
                            newPassword,
                            onSuccess = {
                                showPasswordDialog = false
                                passwordErrorMessage = ""
                            },
                            onFailure = { error ->
                                passwordErrorMessage = error
                            }
                        )
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
