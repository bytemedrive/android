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
fun TopBarStarred(
    starredViewModel: StarredViewModel = get(),
) {
    val context = LocalContext.current
    val itemsSelected by starredViewModel.itemsSelected.collectAsState()

    TopAppBar(
        title = {
            Text(
                pluralStringResource(id = R.plurals.top_bar_file_items, itemsSelected.size, itemsSelected.size),
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
                starredViewModel.removeItems(itemsSelected.map { it.id })
                starredViewModel.clearSelectedItems()
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "File delete"
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