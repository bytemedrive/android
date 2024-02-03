package com.bytemedrive.file.starred.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.ui.component.AlertDialogRemove
import com.bytemedrive.ui.component.Loader
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarredBottomSheetContextFolder(
    folderId: UUID,
    starredBottomSheetContextFolderViewModel: StarredBottomSheetContextFolderViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject()
) {

    LaunchedEffect(Unit) {
        starredBottomSheetContextFolderViewModel.initialize(folderId)
    }

    starredBottomSheetContextFolderViewModel.folder?.let { folder ->

        var alertDialogDeleteOpened by remember { mutableStateOf(false) }

        val navigateBack = { appNavigator.navigateTo(AppNavigator.NavTarget.STARRED) }

        val toggleStarred = {
            starredBottomSheetContextFolderViewModel.toggleStarredFolder(folder.id, folder.starred)
            navigateBack()
        }

        if (alertDialogDeleteOpened) {
            AlertDialogRemove(
                "Delete folder?",
                "Are you sure you want to permanently delete folder \"${folder.name}\"?",
                {
                    starredBottomSheetContextFolderViewModel.removeFolder(folder.id)
                    alertDialogDeleteOpened = false
                    navigateBack()
                }) { alertDialogDeleteOpened = false }
        }

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
