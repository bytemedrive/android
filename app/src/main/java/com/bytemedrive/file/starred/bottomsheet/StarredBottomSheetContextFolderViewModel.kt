package com.bytemedrive.file.starred.bottomsheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderDao
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class StarredBottomSheetContextFolderViewModel(
    private val externalScope: CoroutineScope,
    private val folderManager: FolderManager,
    private val folderRepository: FolderRepository,
    private val eventPublisher: EventPublisher,
): ViewModel() {
    var folder by mutableStateOf<Folder?>(null)

    fun initialize(folderId: UUID) = viewModelScope.launch{
        folder = folderRepository.getFolderById(folderId)
    }

    fun removeFolder(id: UUID) = externalScope.launch {
        folderManager.removeFolder(id)
    }

    fun toggleStarredFolder(id: UUID, value: Boolean) = externalScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFolderStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFolderStarAdded(id))
        }
    }
}