package com.bytemedrive.file.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bytemedrive.file.UploadViewModel
import com.bytemedrive.navigation.AppNavigator
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun BottomSheetCreate(
    folderId: String?,
    uploadViewModel: UploadViewModel = koinViewModel(),
    createFolderViewModel: CreateFolderViewModel = koinViewModel(),
    appNavigator: AppNavigator = get()
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        CreateFolder(folderId, createFolderViewModel, appNavigator)
        UploadFile(folderId, uploadViewModel, appNavigator)
    }
}
