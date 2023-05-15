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
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.root.TopBarFile
import com.bytemedrive.file.starred.StarredViewModel
import com.bytemedrive.file.starred.TopBarStarred
import com.bytemedrive.store.AppState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun TopBarApp(
    toggleNav: suspend () -> Unit,
    fileViewModel: FileViewModel = koinViewModel(),
    starredViewModel: StarredViewModel = koinViewModel()
) {
    val fileAndFolderRootSelected by fileViewModel.fileAndFolderSelected.collectAsState()
    val fileAndFolderStarredSelected by starredViewModel.fileAndFolderSelected.collectAsState()
    val topBarContentVisible = fileAndFolderRootSelected.isEmpty() && fileAndFolderStarredSelected.isEmpty()

    AnimatedVisibility(visible = fileAndFolderRootSelected.isNotEmpty(), enter = slideInVertically()) {
        TopBarFile()
    }

    AnimatedVisibility(visible = fileAndFolderStarredSelected.isNotEmpty(), enter = slideInVertically()) {
        TopBarStarred()
    }

    AnimatedVisibility(visible = topBarContentVisible, enter = slideInVertically()) {
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