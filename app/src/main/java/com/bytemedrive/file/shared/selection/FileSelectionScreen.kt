package com.bytemedrive.file.shared.selection

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.R
import com.bytemedrive.file.root.Action
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.root.ItemType
import com.bytemedrive.navigation.AppNavigator
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileSelectionDialog(
    fileSelectionViewModel: FileSelectionViewModel = koinViewModel(),
    fileViewModel: FileViewModel = koinInject(),
    appNavigator: AppNavigator = koinInject(),
) {
    val action by fileViewModel.action.collectAsState()
    val folders by fileSelectionViewModel.folders.collectAsState()
    val selectedFolderId by fileSelectionViewModel.selectedFolderId.collectAsState()
    val fileAndFolderListPaging = fileSelectionViewModel.getFilesPages().collectAsLazyPagingItems()
    val folder = folders.find { it.id == selectedFolderId }
    val innerFolder = folder != null
    val title = folder?.name ?: "My drive"
    val closeDialog = {
        fileViewModel.fileSelectionDialogOpened.value = false
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    LaunchedEffect(selectedFolderId) {
        fileSelectionViewModel.updateFileAndFolderList(selectedFolderId)
    }

    DisposableEffect("unmount") {
        onDispose {
            fileViewModel.action.value = null
        }
    }

    BackHandler(true) { fileSelectionViewModel.goBack(closeDialog) }

    Dialog(
        onDismissRequest = { fileViewModel.fileSelectionDialogOpened.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
                    IconButton(onClick = { fileSelectionViewModel.goBack(closeDialog) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }

                    Column() {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Select destination",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = closeDialog) {
                        Text(text = "Cancel")
                    }

                    when (action?.type) {
                        Action.Type.CopyItems -> Button(onClick = {
                            action?.let { action_ ->
                                fileSelectionViewModel.copyItem(action_, selectedFolderId, closeDialog)
                            }
                        }) { Text(text = "Select") }
                        Action.Type.MoveItems -> Button(onClick = {
                            action?.let { action_ ->
                                selectedFolderId?.let { selectedFolderId_ ->
                                    fileSelectionViewModel.moveItems(action_, selectedFolderId_, closeDialog)
                                }
                            }
                        }, enabled = innerFolder) { Text(text = "Move") }
                        else -> Log.w("FileSelectionDialog", "Action type ${action?.type} should be implemented")
                    }
                }
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (fileAndFolderListPaging.itemCount == 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(text = stringResource(id = R.string.common_no_data))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = fileAndFolderListPaging) {
                                it?.let { item ->
                                    val selectedItem = action?.ids?.contains(item.id) == true
                                    val clickable = item.type == ItemType.Folder && !selectedItem

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 12.dp)
                                            .clickable(enabled = clickable) {
                                                fileSelectionViewModel.addToHistory(item.id)
                                                fileSelectionViewModel.openFolder(item.id)
                                            }
                                            .alpha(if (selectedItem) 0.6f else 1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = if (item.type == ItemType.File) Icons.Outlined.Description else Icons.Default.Folder,
                                            contentDescription = "Folder",
                                            tint = Color.Black,
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 18.dp)
                                                .weight(1f)
                                        ) {
                                            Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight(500))
                                            Row() {
                                                if (item.starred) {
                                                    Icon(
                                                        modifier = Modifier.size(16.dp),
                                                        imageVector = Icons.Rounded.Star,
                                                        contentDescription = "Starred",
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
            }
        }
    }
}
