import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextOverflow
import com.bytemedrive.file.root.TopBarFile
import com.bytemedrive.file.starred.TopBarStarred
import com.bytemedrive.navigation.BarType
import com.bytemedrive.navigation.TopBarViewModel
import com.bytemedrive.store.AppState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

@Composable
fun TopBarApp(
    toggleNav: suspend () -> Unit,
    topBarViewModel: TopBarViewModel = koinInject(),
) {
    val barType by topBarViewModel.barType.collectAsState()

    AnimatedVisibility(visible = barType == BarType.SELECTION_FILE, enter = slideInVertically()) {
        TopBarFile()
    }

    AnimatedVisibility(visible = barType == BarType.SELECTION_STARRED, enter = slideInVertically()) {
        TopBarStarred()
    }

    AnimatedVisibility(visible = barType == BarType.SCREEN, enter = slideInVertically()) {
        TopBarAppContent(toggleNav)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarAppContent(toggleNav: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    val title by AppState.title.collectAsState()

    TopAppBar(
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch { toggleNav() }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        },
    )
}