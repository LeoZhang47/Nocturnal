package com.example.nocturnal.ui.screen

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.nocturnal.R
import com.example.nocturnal.data.model.Bar
import com.example.nocturnal.data.model.Post
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import com.mapbox.geojson.Point
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BarListView(navController: NavHostController, viewModel: BarListViewModel) {

    val isLoading by viewModel.isLoading.collectAsState()
    val bars by viewModel.bars.collectAsState()
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_formatter))
    val formattedDate = currentDate.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center // Centers the content inside the Box
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(144.dp)
                )
            }
        } else {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(bars) { bar ->
                    BarItem(bar = bar, viewModel = viewModel, onBarClick = {
                        navController.navigate("barDetail/${bar.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun BarItem(bar: Bar, viewModel: BarListViewModel, onBarClick: () -> Unit) {
    val barLocation = bar.location?.let {
        Point.fromLngLat(it.longitude, bar.location.latitude)
    }
    val distanceToBar = barLocation?.let { viewModel.getDistanceToBar(it) }

    Button(
        onClick = onBarClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(text = bar.name, fontSize = 20.sp)
            distanceToBar?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun BarDetailScreen(bar: Bar?, viewModel: BarListViewModel) {

    val isLoading by viewModel.isLoading.collectAsState()
    val posts by viewModel.posts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPosts()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center // Centers the content inside the Box
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(144.dp)
                )
            }
        } else {
            if (bar != null) {
                Text(text = bar.name, style = MaterialTheme.typography.headlineMedium)
                val currentDate = LocalDate.now(ZoneId.of("America/New_York"))
                val today6AM = currentDate.atTime(LocalTime.of(6, 0))
                    .atZone(ZoneId.of("America/New_York")).toInstant()
                val yesterday6AM = currentDate.minusDays(1)
                    .atTime(LocalTime.of(6, 0)).atZone(ZoneId.of("America/New_York")).toInstant()
                val currentInstant = Instant.now().atZone(ZoneId.of("America/New_York")).toInstant()
                val currentTime = currentInstant.atZone(ZoneId.of("America/New_York")).toLocalTime()
                val startTime: Instant = if (currentTime.isBefore(LocalTime.of(6, 0))) {
                    yesterday6AM
                } else {
                    today6AM
                }

                val postsForBar = ArrayList<Post>()
                for (post in posts) {
                    if (post.id in bar.postIDs && post.timestamp.toDate().toInstant().isAfter(startTime)) {
                        postsForBar.add(0, post)
                    }
                }
                if (postsForBar.isEmpty()) {
                    Text(text = "No one has posted yet. Be the first!")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(postsForBar) { post ->
                            val username = remember { mutableStateOf<String?>(null) }
                            var profilePicturePath by remember { mutableStateOf<String?>(null) }

                            LaunchedEffect(post.userID) {
                                username.value = viewModel.getUsername(post.userID)

                                post.userID.let { uid ->
                                    viewModel.getUserProfilePicture(uid,
                                        onSuccess = { url -> profilePicturePath = url },
                                        onFailure = { profilePicturePath = "src/main/res/drawable/nocturnal-default-pfp.png" }
                                    )
                                }
                            }
                            if (username.value != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    val timestamp = post.timestamp.toDate()
                                    val formatter = SimpleDateFormat(stringResource(R.string.time_formatter), Locale.getDefault())
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(profilePicturePath ?: R.drawable.nocturnal_default_pfp),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                        val usernameText = username.value
                                        Text(
                                            text = if (usernameText != null) {
                                                stringResource(R.string.username, usernameText)
                                            } else stringResource(R.string.loading_username),
                                            modifier = Modifier.padding(8.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = formatter.format(timestamp),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 18.sp
                                        )
                                    }


                                    ExpandableImage(imageUrl = post.media)
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.defaultimage),
                                    contentDescription = stringResource(R.string.default_image),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

            } else {
                Text(text = stringResource(R.string.bar_not_found), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ExpandableImage(imageUrl: String) {
    val isPopupOpen = remember { mutableStateOf(false) }

    // Thumbnail image with click to open popup
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = stringResource(R.string.expandable_image),
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
                contentDescription = stringResource(R.string.fullscreen_image),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPopupOpen.value = false }, // Dismiss on click
                contentScale = ContentScale.Fit
            )
        }
    }
}
