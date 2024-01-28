package com.bytemedrive.file.root.bottomsheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderRepository
import kotlinx.coroutines.launch
import java.util.UUID

class FileBottomSheetContextFolderViewModel(
    private val folderRepository: FolderRepository
): ViewModel() {
    var folder by mutableStateOf<Folder?>(null)

    fun initialize(folderId: UUID) = viewModelScope.launch{
        folder = folderRepository.getFolderById(folderId)
    }
}