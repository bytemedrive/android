package com.bytemedrive.file.starred.bottomsheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderDao
import com.bytemedrive.folder.FolderRepository
import kotlinx.coroutines.launch
import java.util.UUID

class StarredBottomSheetContextFolderViewModel(
    private val folderRepository: FolderRepository
): ViewModel() {
    var folder by mutableStateOf<Folder?>(null)

    fun initialize(folderId: UUID) = viewModelScope.launch{
        folder = folderRepository.getFolderById(folderId)
    }
}