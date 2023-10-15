package com.bytemedrive.file.root.bottomsheet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.ui.component.AlertDialogRemove
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBottomSheetContextFile(
    id: String,
    fileViewModel: FileViewModel = koinInject(),
    appNavigator: AppNavigator = koinInject()
) =
    fileViewModel.singleDataFileLink(id)?.let { dataFileLink ->
        val context = LocalContext.current
        var alertDialogDeleteOpened by remember { mutableStateOf(false) }

        val toggleStarred = { fileViewModel.toggleStarredFile(dataFileLink.id, dataFileLink.starred) { appNavigator.navigateTo(AppNavigator.NavTarget.BACK) } }

        if (alertDialogDeleteOpened) {
            AlertDialogRemove(
                "Delete file?",
                "Are you sure you want to permanently delete file \"${dataFileLink.name}\"?",
                { fileViewModel.removeFile(dataFileLink.id) { appNavigator.navigateTo(AppNavigator.NavTarget.BACK) } }) { alertDialogDeleteOpened = false }
        }

        val downloadFile = {
            fileViewModel.downloadFile(dataFileLink.id)

            Toast.makeText(context, "1 item will be downloaded. See notification for details", Toast.LENGTH_SHORT).show()
        }

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "File",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(dataFileLink.name) },
            )

            Divider()

            ListItem(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(onClick = { toggleStarred() }),
                leadingContent = {
                    Icon(
                        imageVector = if (dataFileLink.starred) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = if (dataFileLink.starred) "Remove from starred" else "Add to starred",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = if (dataFileLink.starred) "Remove from starred" else "Add to starred") },
            )

            ListItem(
                modifier = Modifier.clickable(onClick = downloadFile),
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
                    .clickable(onClick = { fileViewModel.useSelectionScreenToCopyItems(dataFileLink.id) }),
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
                    .clickable(onClick = { fileViewModel.useSelectionScreenToMoveItems(dataFileLink.id) }),
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
                    .clickable(onClick = { alertDialogDeleteOpened = true }),
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
