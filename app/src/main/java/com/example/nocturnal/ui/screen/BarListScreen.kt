import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController

@Composable
fun BarListView(navController: NavHostController) {
    val viewModel: BarListViewModel = viewModel(
        factory = remember { BarListViewModel.Factory }
    )

    val bars by viewModel.bars.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(bars) { bar ->
            BarItem(bar = bar, onBarClick = {
                navController.navigate("barDetail/${bar.id}")
            })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BarItem(bar: Bar, onBarClick: () -> Unit) {
    Button(
        onClick = onBarClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(text = bar.name)
            bar.location?.let {
                Text(text = "Lat: ${it.latitude}, Lng: ${it.longitude}")
            }
        }
    }
}

@Composable
fun BarDetailScreen(bar: Bar?) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (bar != null) {
            Text(text = "Bar Name: ${bar.name}", style = MaterialTheme.typography.headlineMedium)

            val lat = bar.location?.latitude
            val lng = bar.location?.longitude
            if (lat != null && lng != null) {
                Text(text = "Location: Lat: $lat, Lng: $lng")
            } else {
                Text(text = "Location: Unknown")
            }
        } else {
            Text(text = "Bar not found", style = MaterialTheme.typography.bodyMedium)
        }
    }
}