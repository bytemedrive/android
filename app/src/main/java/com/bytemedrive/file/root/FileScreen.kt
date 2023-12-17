package com.bytemedrive.file.root

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.R
import com.bytemedrive.file.shared.floatingactionbutton.FloatingActionButtonCreate
import com.bytemedrive.file.shared.preview.FilePreviewDialog
import com.bytemedrive.file.shared.selection.FileSelectionDialog
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject
import java.util.UUID

private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileScreen(
    folderId: UUID? = null,
    fileViewModel: FileViewModel = koinInject(),
    appNavigator: AppNavigator = koinInject(),
) {
    val context = LocalContext.current
    val items by fileViewModel.items.collectAsState()
    val itemsUploading by fileViewModel.itemsUploading.collectAsState(emptyList())
    val itemsUploadingByFolderId = itemsUploading.filter { it.folderId?.equals(folderId) ?: true }
    val itemsPaged = fileViewModel.getItemsPages(itemsUploadingByFolderId + items).collectAsLazyPagingItems()
    val itemsSelected by fileViewModel.itemsSelected.collectAsState()
    val thumbnails by fileViewModel.thumbnails.collectAsState()
    val dataFilePreview by fileViewModel.dataFilePreview.collectAsState()
    val fileSelectionDialogOpened by fileViewModel.fileSelectionDialogOpened.collectAsState()

    LaunchedEffect(Unit) {
        requestPermissions(context)
        fileViewModel.selectedFolder.update { AppState.customer!!.folders.value.find { it.id == folderId } }

        AppState.topBarComposable.update { { toggleNav -> TopBarFile(folderId, toggleNav) } }

        if (folderId == null) {
            AppState.title.update { "My files" }
        } else {
            fileViewModel.singleFolder(folderId)?.let { folder ->
                AppState.title.update { folder.name }
            }
        }

        fileViewModel.init(context)
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

    dataFilePreview?.let { dataFilePreview_ ->
        val dataFileIds = AppState.customer!!.dataFilesLinks.value
            .filter { dataFileLink -> dataFileLink.folderId == folderId }
            .map { it.dataFileId }

        FilePreviewDialog(dataFilePreview_, dataFileIds, { fileViewModel.dataFilePreview.update { null } })
    }

    if (fileSelectionDialogOpened) {
        FileSelectionDialog()
    }

    Scaffold(
        floatingActionButton = { FloatingActionButtonCreate(folderId) },
    ) { paddingValues ->

        if (items.isEmpty()) {
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
                    items(items = itemsPaged) { fileAndFolder ->
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
                                FileImage(itemSelected, item, thumbnails[item.id])
                                Column(
                                    modifier = Modifier
                                        .padding(start = 18.dp)
                                        .weight(1f)
                                ) {
                                    Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight(500))
                                    Row() {
                                        if (item.uploading) {
                                            Text(text = "Uploading")
                                        } else if (item.starred) {
                                            Icon(
                                                modifier = Modifier.size(16.dp),
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = "Starred",
                                                tint = Color.Black,
                                            )
                                        }
                                    }
                                }
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
                }
            }
        }
    }
}


@Composable
private fun FileImage(itemSelected: Boolean, item: Item, image: Bitmap?) {
    Box(
        modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center
    ) {
        when {
            itemSelected -> {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Checked",
                    tint = Color.Black,
                )
            }
            item.uploading -> {
                CircularProgressIndicator()
            }
            item.type == ItemType.FILE -> {
                image?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "Thumbnail ${item.name}", contentScale = ContentScale.Crop) } ?: Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "File",
                    tint = Color.Black,
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = Color.Black,
                )
            }
        }
    }
}

private fun requestPermissions(context: Context) {
    val activity = context as Activity

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
    }
}