package com.bytemedrive.file.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.R
import com.bytemedrive.file.shared.entity.ItemType
import com.bytemedrive.file.shared.floatingactionbutton.FloatingActionButtonCreate
import com.bytemedrive.file.shared.preview.FilePreviewDialog
import com.bytemedrive.file.shared.selection.FileSelectionDialog
import com.bytemedrive.file.shared.ui.ItemImage
import com.bytemedrive.file.shared.ui.ItemStatus
import com.bytemedrive.file.shared.ui.loadState
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.Loader
import com.bytemedrive.ui.component.LoadingNextPageItem
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileScreen(
    folderId: UUID? = null,
    fileViewModel: FileViewModel = koinInject(),
    appNavigator: AppNavigator = koinInject(),
) {
    val context = LocalContext.current
    val fileListItems = fileViewModel.fileListItems.collectAsLazyPagingItems()
    val itemsSelected by fileViewModel.itemsSelected.collectAsState()
    val fileSelectionDialogOpened by fileViewModel.fileSelectionDialogOpened.collectAsState()

    LaunchedEffect(Unit) {
        AppState.topBarComposable.update { { toggleNav -> TopBarFile(folderId, toggleNav) } }

        fileViewModel.initialize(context, folderId)
    }

    LaunchedEffect(fileViewModel.selectedFolder) {
        fileViewModel.selectedFolder?.let { folder ->
            AppState.title.update { folder.name }
        } ?: AppState.title.update { "My files" }
    }

    DisposableEffect(Unit) {
        onDispose {
            fileViewModel.clearSelectedItems()
            fileViewModel.cancelJobs()
        }
    }

    BackHandler(true) {
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fileViewModel.dataFilePreview?.let { dataFilePreview ->
        FilePreviewDialog(dataFilePreview, { fileViewModel.dataFilePreview = null })
    }

    if (fileSelectionDialogOpened) {
        FileSelectionDialog()
    }

    Scaffold(
        floatingActionButton = { FloatingActionButtonCreate(folderId) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            ) {
                items(items = fileListItems) { fileAndFolder ->
                    fileAndFolder?.let { item ->
                        val itemSelected = itemsSelected.contains(item)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 12.dp)
                                .combinedClickable(
                                    onClick = { fileViewModel.clickFileAndFolder(item) },
                                    onLongClick = { fileViewModel.longClickFileAndFolder(item) }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ItemImage(itemSelected, item, fileViewModel.thumbnails[item.id])
                            ItemStatus(item)
                            IconButton(onClick = {
                                when (item.type) {
                                    ItemType.FILE -> appNavigator.navigateTo(
                                        AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FILE, mapOf("id" to item.id.toString())
                                    )

                                    ItemType.FOLDER -> appNavigator.navigateTo(
                                        AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FOLDER, mapOf("id" to item.id.toString())
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Context menu",
                                    tint = Color.Black,
                                )
                            }
                        }
                    }
                }

                loadState(fileListItems = fileListItems)
            }
        }
    }
}
