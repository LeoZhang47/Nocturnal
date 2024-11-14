package com.example.nocturnal.ui.screen

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.nocturnal.R
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import com.mapbox.geojson.Point
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BarListView(navController: NavHostController) {
    val viewModel: BarListViewModel = viewModel(
        factory = remember { BarListViewModel.Factory }
    )

    val bars by viewModel.bars.collectAsState()
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    val formattedDate = currentDate.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Posts for $formattedDate",
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
fun BarDetailScreen(bar: Bar?) {
    val viewModel: BarListViewModel = viewModel(
        factory = remember { BarListViewModel.Factory }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

            val postIDs: ArrayList<String> = ArrayList()
            for (postID in bar.postIDs) {
                val post = viewModel.getPostById(postID)
                if (post != null) {
                    val timestamp = post.timestamp
                    val instant = timestamp.toDate().toInstant()
                    if (instant.isAfter(startTime)) {
                        postIDs.add(0, postID)
                    }
                }
            }
            if (postIDs.isEmpty()) {
                Text(text = "No one has posted yet. Be the first!")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(postIDs) { postID ->
                        val post = viewModel.getPostById(postID)
                        val username = remember { mutableStateOf<String?>(null) }
                        var profilePicturePath by remember { mutableStateOf<String?>(null) }
                        if (post != null) {
                            LaunchedEffect(post.userID) {
                                username.value = viewModel.getUsername(post.userID) ?: "Unknown User"

                                post.userID.let { uid ->
                                    viewModel.getUserProfilePicture(uid,
                                        onSuccess = { url -> profilePicturePath = url },
                                        onFailure = { profilePicturePath = "src/main/res/drawable/nocturnal-default-pfp.png" }
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                val timestamp = post.timestamp.toDate()
                                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(profilePicturePath ?: R.drawable.nocturnal_default_pfp),
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 0.dp)
                                            .height(40.dp)
                                    )
                                    Text(
                                        text = "@${username.value ?: "Loading username..."}",
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
                                contentDescription = "Default image",
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
            Text(text = "Bar not found", style = MaterialTheme.typography.bodyMedium)
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
