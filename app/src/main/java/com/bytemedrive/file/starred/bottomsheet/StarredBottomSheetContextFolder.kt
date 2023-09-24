package com.bytemedrive.file.starred.bottomsheet

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
import androidx.compose.material.icons.rounded.Delete
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
fun StarredBottomSheetContextFolder(
    id: String,
    fileViewModel: FileViewModel = get(),
    appNavigator: AppNavigator = get()
) =
    fileViewModel.singleFolder(id)?.let { folder ->

        val navigateBack = { appNavigator.navigateTo(AppNavigator.NavTarget.STARRED) }

        val remove = { fileViewModel.removeFolder(folder.id) { navigateBack() } }

        val toggleStarred = { fileViewModel.toggleStarredFolder(folder.id, folder.starred) { navigateBack() } }

        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ListItem(
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
