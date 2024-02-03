package com.bytemedrive.file.root.bottomsheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class FileBottomSheetContextFolderViewModel(
    private val externalScope: CoroutineScope,
    private val folderRepository: FolderRepository,
    private val folderManager: FolderManager,
    private val eventPublisher: EventPublisher,
): ViewModel() {
    var folder by mutableStateOf<Folder?>(null)

    fun initialize(folderId: UUID) = viewModelScope.launch{
        folder = folderRepository.getFolderById(folderId)
    }

    fun toggleStarredFolder(id: UUID, value: Boolean) = externalScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFolderStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFolderStarAdded(id))
        }
    }

    fun removeFolder(id: UUID) = externalScope.launch {
        folderManager.removeFolder(id)
    }
}