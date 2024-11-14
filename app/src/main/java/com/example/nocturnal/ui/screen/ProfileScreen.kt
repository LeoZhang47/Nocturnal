import android.widget.Toast
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
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    fragmentManager: FragmentManager,
    profileActivity: ProfileActivity,
    //imageUrls: List<String>,
    userViewModel: UserViewModel = viewModel(),
    onChangeProfilePicture: () -> Unit
) {
    val currentUser = userViewModel.getCurrentUser()
    val usernameFlow = currentUser?.uid?.let { userViewModel.getUsername(it) } ?: MutableStateFlow("Guest")
    val username by usernameFlow.collectAsState()

    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            userViewModel.getUserProfilePicture(uid,
                onSuccess = { url -> profilePictureUrl = url },
                onFailure = { profilePictureUrl = null }
            )
        }
    }

    val imageUrls by userViewModel.imageUrls.observeAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var userScore by remember { mutableStateOf(0) }
    var scoreErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userViewModel.getUserPosts()
        userViewModel.getUserScore(
            onSuccess = { score -> userScore = score },
            onFailure = { error -> scoreErrorMessage = error }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(profilePictureUrl ?: R.drawable.nocturnal_default_pfp),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                            .clip(CircleShape), // Makes the image circular
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Score: ${userScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Change Username")
                    }

                    Button(
                        onClick = { onChangeProfilePicture() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Change PFP")
                    }

                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Change Password")
                    }

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
            }

            item {
                if (imageUrls.isNotEmpty()) {
                    Text(
                        text = "Your Images",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                } else {
                    Text(
                        text = "No images available",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(imageUrls) { imageUrl ->
                ExpandableImage(imageUrl)
            }
        }

        // Existing dialog code for changing username and password
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
                                onFailure = { error -> errorMessage = error }
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
                            visualTransformation = PasswordVisualTransformation()
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
                            onFailure = { error -> passwordErrorMessage = error }
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

@Composable
fun ExpandableImage(imageUrl: String) {
    val isPopupOpen = remember { mutableStateOf(false) }

    // Thumbnail image with click to open popup
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = "Expandable image",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable { isPopupOpen.value = true },
        contentScale = ContentScale.Crop
    )

    // Popup dialog for full-screen image
    if (isPopupOpen.value) {
        Dialog(onDismissRequest = { isPopupOpen.value = false }) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Full-screen image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPopupOpen.value = false }, // Dismiss on click
                contentScale = ContentScale.Fit
            )
        }
    }
}