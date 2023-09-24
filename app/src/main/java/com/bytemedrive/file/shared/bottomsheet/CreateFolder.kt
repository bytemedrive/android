package com.bytemedrive.file.shared.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytemedrive.file.root.bottomsheet.CreateFolderViewModel
import com.bytemedrive.navigation.AppNavigator

@Composable
fun CreateFolder(
    folderId: String?,
    createFolderViewModel: CreateFolderViewModel,
    appNavigator: AppNavigator
) {
    val name by createFolderViewModel.name.collectAsState()

    var dialogNewFolderOpened by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val confirmName = {
        dialogNewFolderOpened = false
        createFolderViewModel.createFolder(folderId) {
            folderId?.let {
                appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to folderId))
            } ?: appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
        }
    }

    if (dialogNewFolderOpened) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { dialogNewFolderOpened = false },
            title = { Text(text = "New folder") },
            text = { OutlinedTextField( modifier = Modifier.focusRequester(focusRequester), value = name, onValueChange = { createFolderViewModel.name.value = it }) },
            confirmButton = {
                TextButton(
                    onClick = { confirmName() }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogNewFolderOpened = false }
                ) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .clickable { dialogNewFolderOpened = true }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { dialogNewFolderOpened = true }) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = "Folder",
                tint = Color.Black,
            )
        }
        Text(text = "Folder", fontSize = 16.sp, fontWeight = FontWeight(500))
    }
}