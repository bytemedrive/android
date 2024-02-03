package com.bytemedrive.file.starred.bottomsheet

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.ui.component.AlertDialogRemove
import com.bytemedrive.ui.component.Loader
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarredBottomSheetContextFile(
    dataFileLinkId: UUID,
    starredBottomSheetContextFileViewModel: StarredBottomSheetContextFileViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject()
) {

    LaunchedEffect(Unit) {
        starredBottomSheetContextFileViewModel.initialize(dataFileLinkId)
    }

    starredBottomSheetContextFileViewModel.dataFileLink?.let { dataFileLink ->
        val context = LocalContext.current

        var alertDialogDeleteOpened by remember { mutableStateOf(false) }

        val navigateBack = { appNavigator.navigateTo(AppNavigator.NavTarget.STARRED) }

        val toggleStarred = {
            starredBottomSheetContextFileViewModel.toggleStarredFile(dataFileLink.id, dataFileLink.starred)
            navigateBack()
        }

        if (alertDialogDeleteOpened) {
            AlertDialogRemove(
                "Delete file?",
                "Are you sure you want to permanently delete file \"${dataFileLink.name}\"?",
                {
                    starredBottomSheetContextFileViewModel.removeFile(dataFileLink.id)
                    alertDialogDeleteOpened = false
                    navigateBack()
                }) { alertDialogDeleteOpened = false }
        }

        val downloadFile = {
            starredBottomSheetContextFileViewModel.downloadFile(dataFileLink.id)
            Toast.makeText(context, "One item will be downloaded. See notification for details", Toast.LENGTH_SHORT).show()
            appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = "File",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(dataFileLink.name) },
            )

            Divider()

            ListItem(
                modifier = Modifier
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
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Download",
                        tint = Color.Black,
                    )
                },
                headlineText = { Text(text = "Download") },
            )

            ListItem(
                modifier = Modifier
                    .clickable(onClick = { alertDialogDeleteOpened = true }),
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

    } ?: Loader()
}
