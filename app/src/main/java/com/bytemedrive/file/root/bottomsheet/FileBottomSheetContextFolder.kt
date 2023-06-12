package com.bytemedrive.file.root.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.navigation.AppNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBottomSheetContextFolder(
    id: String,
    fileViewModel: FileViewModel = get(),
    appNavigator: AppNavigator = get()
) =
    fileViewModel.singleFolder(id)?.let { folder ->

        val navigateBack = {
            folder.parent?.let {
                appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to folder.parent.toString()))
            } ?: appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
        }

        val remove = { fileViewModel.removeFolder(folder.id) { navigateBack() } }

        val toggleStarred = { fileViewModel.toggleStarredFolder(folder.id, folder.starred) { navigateBack() } }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListItem(
                modifier = Modifier.height(32.dp),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = "Folder",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(folder.name) },
            )

            Divider()

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { toggleStarred() }),
                leadingContent = {
                    Icon(
                        imageVector = if (folder.starred) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = if (folder.starred) "Remove from starred" else "Add to starred",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = if (folder.starred) "Remove from starred" else "Add to starred") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { fileViewModel.useSelectionScreenToCopyItem(folder.id) }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.FolderCopy,
                        contentDescription = "Copy folder",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Copy folder") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { fileViewModel.useSelectionScreenToMoveItems(folder.id) }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.DriveFileMove,
                        contentDescription = "Move folder",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Move folder") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { remove() }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Remove",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Remove") },
            )
        }

    }