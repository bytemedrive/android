package com.bytemedrive.file.root

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import com.bytemedrive.R
import com.bytemedrive.navigation.TopBarAppContent
import com.bytemedrive.navigation.TopBarAppContentBack
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarFile(
    folderId: UUID? = null,
    toggleNav: suspend () -> Unit,
    fileViewModel: FileViewModel = koinInject(),
) {
    val context = LocalContext.current
    val itemsSelected by fileViewModel.itemsSelected.collectAsState()

    val selectedItemsAreOnlyFiles = itemsSelected.all { it.type == ItemType.FILE }

    val downloadFile = {
        fileViewModel.downloadFiles(itemsSelected.map { it.id })
        fileViewModel.clearSelectedItems()

        Toast.makeText(context, "${itemsSelected.size} items will be downloaded. See notification for details", Toast.LENGTH_SHORT).show()
    }

    when {
        itemsSelected.isNotEmpty() -> {
            TopAppBar(
                title = {
                    Text(
                        pluralStringResource(id = R.plurals.top_bar_file_items, itemsSelected.size, itemsSelected.size),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { fileViewModel.clearSelectedItems() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        fileViewModel.useSelectionScreenToCopyItems(itemsSelected.map { it.id }, folderId)
                        fileViewModel.clearSelectedItems()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.FileCopy,
                            contentDescription = "Item copy"
                        )
                    }
                    IconButton(onClick = {
                        fileViewModel.useSelectionScreenToMoveItems(itemsSelected.map { it.id }, folderId)
                        fileViewModel.clearSelectedItems()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.DriveFileMove,
                            contentDescription = "Item move"
                        )
                    }
                    IconButton(onClick = {
                        fileViewModel.removeItems(itemsSelected.map { it.id })
                        fileViewModel.clearSelectedItems()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Item delete"
                        )
                    }
                    if (selectedItemsAreOnlyFiles) {
                        IconButton(onClick = downloadFile) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = "Item download"
                            )
                        }
                    }
                    IconButton(onClick = { fileViewModel.toggleAllItems(context) }) {
                        Icon(
                            imageVector = Icons.Filled.SelectAll,
                            contentDescription = "Select all items"
                        )
                    }
                }
            )
        }
        folderId != null -> TopBarAppContentBack()
        else -> TopBarAppContent(toggleNav)
    }

}