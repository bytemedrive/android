package com.bytemedrive.file.shared.floatingactionbutton

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.AppNavigator
import org.koin.androidx.compose.get
import java.util.UUID

@Composable
fun FloatingActionButtonCreate(
    selectedFolderId: UUID? = null,
    appNavigator: AppNavigator = get()
) {
    FloatingActionButton(
        onClick = {
            selectedFolderId?.let { folderId ->
                appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE, mapOf("folderId" to folderId.toString()))
            } ?: appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE)
        },
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Create",
            tint = Color.White,
        )
    }
}