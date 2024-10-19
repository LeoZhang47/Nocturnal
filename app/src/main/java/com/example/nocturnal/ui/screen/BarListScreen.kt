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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController

@Composable
@Preview
fun BarListView(navController: NavHostController, viewModel: BarListViewModel = viewModel()) {
    val bars by viewModel.bars.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(bars) { bar ->
            BarItem(bar = bar, onBarClick = {
                navController.navigate("barDetail/${bar.name}/${bar.location?.latitude}/${bar.location?.longitude}")
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
fun BarDetailScreen(barName: String?, latitude: Double?, longitude: Double?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Bar Name: $barName", style = MaterialTheme.typography.headlineMedium)
        latitude?.let { lat ->
            longitude?.let { lng ->
                Text(text = "Location: Lat: $lat, Lng: $lng")
            }
        }
    }
}