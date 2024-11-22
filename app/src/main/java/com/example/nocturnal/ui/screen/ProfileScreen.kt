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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    fragmentManager: FragmentManager,
    profileActivity: ProfileActivity,
    userViewModel: UserViewModel = viewModel(),
    onChangeProfilePicture: () -> Unit
) {
    val currentUser = userViewModel.getCurrentUser()
    val guestName = stringResource(R.string.guest)
    val usernameFlow = remember { MutableStateFlow(guestName) }
    val username by usernameFlow.collectAsState()

    val profilePicture by userViewModel.profilePictureUrl.collectAsState()
    val imageUrls by userViewModel.imageUrls.collectAsState(emptyList())


    // Fetch user profile picture URL when the user changes
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            userViewModel.getUserProfilePicture(uid)
        }
    }

    // Fetch username when the user changes
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            userViewModel.getUsername(uid) { fetchedUsername ->
                usernameFlow.value = fetchedUsername ?: "Guest"
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var userScore by remember { mutableIntStateOf(0) }
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
                title = { Text(stringResource(R.string.back)) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = stringResource(R.string.back)
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
                        painter = rememberAsyncImagePainter(profilePicture ?: R.drawable.nocturnal_default_pfp),
                        contentDescription = stringResource(R.string.profile_picture),
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
                        text = stringResource(R.string.score_template, userScore),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = stringResource(R.string.change_username))
                    }

                    Button(
                        onClick = { onChangeProfilePicture() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = stringResource(R.string.change_pfp))
                    }

                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = stringResource(R.string.change_password))
                    }

                    Button(
                        onClick = {
                            userViewModel.signOut()
                            profileActivity.logOutAndNavigateToLogin()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = stringResource(R.string.logout))
                    }
                }
            }

            item {
                if (imageUrls.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.your_images),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.no_images),
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

        // Dialogs for username and password change
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = stringResource(R.string.change_username)) },
                text = {
                    Column {
                        TextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text(stringResource(R.string.enter_new_username)) },
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
                                uid, newUsername,
                                callback = {success, msg ->
                                    run {
                                        if (success) {
                                            userViewModel.getUsername(uid) { fetchedUsername ->
                                                usernameFlow.value = fetchedUsername ?: "Guest"
                                            }
                                            showDialog = false
                                            errorMessage = ""
                                        } else {
                                            errorMessage = msg
                                        }

                                    }
                                }
                            )
                        }
                    }) {
                        Text(stringResource(R.string.submit))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text(text = stringResource(R.string.change_password)) },
                text = {
                    Column {
                        TextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(stringResource(R.string.enter_new_password)) },
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
                        userViewModel.changePassword(newPassword)
                    }) {
                        Text(stringResource(R.string.submit))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun ExpandableImage(imageUrl: String) {
    val isPopupOpen = remember { mutableStateOf(false) }

    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = stringResource(R.string.expandable_image),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable { isPopupOpen.value = true },
        contentScale = ContentScale.Crop
    )

    if (isPopupOpen.value) {
        Dialog(onDismissRequest = { isPopupOpen.value = false }) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = stringResource(R.string.fullscreen_image),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPopupOpen.value = false },
                contentScale = ContentScale.Fit
            )
        }
    }
}