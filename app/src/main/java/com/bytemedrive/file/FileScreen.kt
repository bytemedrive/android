package com.bytemedrive.file

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    folderId: String?,
    fileViewModel: FileViewModel = koinViewModel(),
    appNavigator: AppNavigator = get()
) {
    val context = LocalContext.current
    val items = fileViewModel.getFilesPages().collectAsLazyPagingItems()
    val selectedFolderId by fileViewModel.selectedFolderId.collectAsState()

    LaunchedEffect("initialize") {
        requestPermissions(context)
        fileViewModel.updateList(folderId)

        if (folderId == null) {
            AppState.title.value = "My files"
            fileViewModel.selectedFolderId.value = null
        } else {
            fileViewModel.singleFolder(folderId)?.let { folder ->
                AppState.title.value = folder.name
                fileViewModel.selectedFolderId.value = folder.id
            }
        }
    }

    val clickOnItem: (item: Item) -> Unit = { item ->
        when (item.type) {
            ItemType.Folder -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))
            ItemType.File -> null // TODO: Add some action
        }
    }

    BackHandler(true) {
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    Scaffold(
        floatingActionButton = { FloatingActionButtonComponent(selectedFolderId) },
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
                items(items = items) {
                    it?.let { item ->
                        println(item)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 12.dp)
                                .clickable { clickOnItem(item) },
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
                            IconButton(onClick = {
                                when (item.type) {
                                    ItemType.File -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FILE, mapOf("id" to item.id.toString()))
                                    ItemType.Folder -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FOLDER, mapOf("id" to item.id.toString()))
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

@Composable
fun FloatingActionButtonComponent(
    selectedFolderId: UUID?,
    appNavigator: AppNavigator = get()
) {
    FloatingActionButton(
        onClick = {
            selectedFolderId?.let { folderId ->
                appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE, mapOf("folderId" to folderId.toString()))
            } ?: appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE)
        },
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Create",
            tint = Color.White,
        )
    }
}

private fun requestPermissions(context: Context) {
    val activity = context as Activity

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
    }
}
