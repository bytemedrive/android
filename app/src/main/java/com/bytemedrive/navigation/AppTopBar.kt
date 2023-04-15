import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar() {
    TopAppBar(
        title = { Text(text = "Top App Bar", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.ArrowBack, "backIcon", tint = Color.White)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = 10.dp
    )
}