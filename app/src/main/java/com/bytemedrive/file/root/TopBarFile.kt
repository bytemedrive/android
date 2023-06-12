package com.bytemedrive.file.root

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import com.bytemedrive.R
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopBarFile(
    fileViewModel: FileViewModel = get(),
) {
    val context = LocalContext.current
    val itemsSelected by fileViewModel.itemsSelected.collectAsState()

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
                fileViewModel.useSelectionScreenToCopyItems(itemsSelected.map { it.id })
                fileViewModel.clearSelectedItems()
            }) {
                Icon(
                    imageVector = Icons.Filled.FileCopy,
                    contentDescription = "Item copy"
                )
            }
            IconButton(onClick = {
                fileViewModel.useSelectionScreenToMoveItems(itemsSelected.map { it.id })
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
            IconButton(onClick = { fileViewModel.toggleAllItems(context) }) {
                Icon(
                    imageVector = Icons.Filled.SelectAll,
                    contentDescription = "Select all items"
                )
            }
        }
    )
}