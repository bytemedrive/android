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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.navigation.AppNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBottomSheetContextFile(
    id: String,
    fileViewModel: FileViewModel = get(),
    appNavigator: AppNavigator = get()
) =
    fileViewModel.singleFile(id)?.let { file ->
        val context = LocalContext.current

        val navigateBack = {
            file.folderId?.let {
                appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to file.folderId.toString()))
            } ?: appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
        }

        val remove = { fileViewModel.removeFile(file.id) { navigateBack() } }

        val toggleStarred = { fileViewModel.toggleStarredFile(file.id, file.starred) { navigateBack() } }

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
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "File",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(file.name) },
            )

            Divider()

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { toggleStarred() }),
                leadingContent = {
                    Icon(
                        imageVector = if (file.starred) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = if (file.starred) "Remove from starred" else "Add to starred",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = if (file.starred) "Remove from starred" else "Add to starred") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { fileViewModel.downloadFile(file.id, context) }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Download") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { fileViewModel.useSelectionScreenToCopyItems(file.id) }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.FileCopy,
                        contentDescription = "Copy file",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Copy file") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { fileViewModel.useSelectionScreenToMoveItems(file.id) }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.DriveFileMove,
                        contentDescription = "Move file",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Move file") },
            )

            ListItem(
                modifier = Modifier
                    .height(32.dp)
                    .clickable(onClick = { remove() }),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Remove",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Remove") },
            )

        }

    }
