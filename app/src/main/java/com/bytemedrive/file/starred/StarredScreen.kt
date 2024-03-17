package com.bytemedrive.file.starred

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.R
import com.bytemedrive.file.shared.entity.ItemType
import com.bytemedrive.file.shared.floatingactionbutton.FloatingActionButtonCreate
import com.bytemedrive.file.shared.preview.FilePreviewDialog
import com.bytemedrive.file.shared.ui.ItemImage
import com.bytemedrive.file.shared.ui.ItemStatus
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StarredScreen(
    starredViewModel: StarredViewModel = koinInject(),
    appNavigator: AppNavigator = koinInject()
) {
    val context = LocalContext.current
    val fileListItems = starredViewModel.fileListItems.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        AppState.title.update { "Starred files" }
        AppState.topBarComposable.update { { toggleNav -> TopBarStarred(toggleNav) } }

        starredViewModel.initialize(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            starredViewModel.clearSelectedItems()
            starredViewModel.cancelJobs()
        }
    }

    BackHandler(true) {
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    starredViewModel.dataFilePreview?.let { dataFilePreview_ ->
        FilePreviewDialog(dataFilePreview_, { starredViewModel.dataFilePreview = null })
    }

    Scaffold(
        floatingActionButton = { FloatingActionButtonCreate() },
    ) { paddingValues ->

        if (fileListItems.itemCount == 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = stringResource(id = R.string.common_no_data))
            }
        } else {
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
                    items(items = fileListItems) {
                        it?.let { item ->
                            val itemSelected = starredViewModel.itemsSelected.contains(item)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 12.dp)
                                    .combinedClickable(
                                        onClick = { starredViewModel.clickFileAndFolder(item) },
                                        onLongClick = { starredViewModel.longClickFileAndFolder(item) }
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ItemImage(itemSelected, item, starredViewModel.thumbnails[item.id])
                                ItemStatus(item)
                                IconButton(onClick = {
                                    when (item.type) {
                                        ItemType.FILE -> appNavigator.navigateTo(
                                            AppNavigator.NavTarget.STARRED_BOTTOM_SHEET_CONTEXT_FILE, mapOf("id" to item.id.toString())
                                        )

                                        ItemType.FOLDER -> appNavigator.navigateTo(
                                            AppNavigator.NavTarget.STARRED_BOTTOM_SHEET_CONTEXT_FOLDER, mapOf("id" to item.id.toString())
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
                }
            }
        }
    }
}