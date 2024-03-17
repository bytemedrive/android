package com.bytemedrive.file.starred

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.bytemedrive.R
import com.bytemedrive.navigation.TopBarAppContent
import com.bytemedrive.ui.component.AlertDialogRemove
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarStarred(
    toggleNav: suspend () -> Unit,
    starredViewModel: StarredViewModel = koinInject(),
) {
    val context = LocalContext.current
    var alertDialogRemoveOpened by remember { mutableStateOf(false) }

    if (alertDialogRemoveOpened) {
        AlertDialogRemove(
            "Remove items?",
            stringResource(
                id = R.string.top_bar_remove_items,
                pluralStringResource(id = R.plurals.top_bar_file_items, starredViewModel.itemsSelected.size, starredViewModel.itemsSelected.size)
            ),
            {
                starredViewModel.removeItems(starredViewModel.itemsSelected.map { it.id })
                starredViewModel.clearSelectedItems()
                alertDialogRemoveOpened = false
            }) { alertDialogRemoveOpened = false }
    }

    if (starredViewModel.itemsSelected.isEmpty()) {
        TopBarAppContent(toggleNav)
    } else {
        TopAppBar(
            title = {
                Text(
                    pluralStringResource(id = R.plurals.top_bar_file_items, starredViewModel.itemsSelected.size, starredViewModel.itemsSelected.size),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = { starredViewModel.clearSelectedItems() }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    alertDialogRemoveOpened = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "File remove"
                    )
                }
                IconButton(onClick = { starredViewModel.toggleAllItems(context) }) {
                    Icon(
                        imageVector = Icons.Filled.SelectAll,
                        contentDescription = "Select all"
                    )
                }
            }
        )
    }
}