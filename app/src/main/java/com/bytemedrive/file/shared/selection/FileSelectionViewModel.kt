package com.bytemedrive.file.shared.selection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.EventFileCopied
import com.bytemedrive.file.root.EventFileMoved
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.shared.control.FileListItemRepository
import com.bytemedrive.folder.EventFolderCopied
import com.bytemedrive.folder.EventFolderMoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

class FileSelectionViewModel(
    private val eventPublisher: EventPublisher,
    private val folderManager: FolderManager,
    private val folderRepository: FolderRepository,
    private val dataFileRepository: DataFileRepository,
    private val fileListItemRepository: FileListItemRepository
) : ViewModel() {

    var fileListItems = fileListItemRepository.getAllPaged()

    var selectedFolder by mutableStateOf<Folder?>(null)

    private val history = MutableStateFlow(emptyList<UUID>())

    init {
        refreshFileListItems(null)
    }

    fun openFolder(id: UUID?) = viewModelScope.launch {
        selectedFolder = id?.let { folderRepository.getFolderById(id) }

        refreshFileListItems(selectedFolder)
    }

    fun addToHistory(id: UUID) =
        history.update { it + id }

    fun goBack(closeDialog: () -> Unit) {
        if (history.value.lastOrNull() == null) {
            closeDialog()
        } else {
            history.update { it.subList(0, max(0, history.value.size - 1)) }
            openFolder(history.value.lastOrNull())
        }
    }

    // TODO: Rework with use of recursive function to have single iteration
    fun copyItem(action: FileViewModel.Action, folderId: UUID?, closeDialog: () -> Unit) = viewModelScope.launch {
        val folders = folderRepository.getFoldersByIds(action.ids)

        folders.forEach { folder ->
            val currentFolderToCopy = folder.copy(id = UUID.randomUUID(), name = "Copy of ${folder.name}", parent = folderId)
            val innerFoldersToCopy = folderManager.findAllFoldersRecursively(folder.id, folders).toMutableList()

            innerFoldersToCopy.forEach { innerFolder ->
                val parent = if (innerFolder.parent == folder.id) currentFolderToCopy.id else innerFolder.parent
                val newFolder = innerFolder.copy(id = UUID.randomUUID(), parent = parent)

                copyFolders(innerFolder.id, newFolder.id)
                copyFiles(innerFolder.id, newFolder.id)

                eventPublisher.publishEvent(EventFolderCopied(innerFolder.id, newFolder.id, newFolder.parent))
            }

            copyFiles(folder.id, currentFolderToCopy.id)

            eventPublisher.publishEvent(EventFolderCopied(folder.id, currentFolderToCopy.id, currentFolderToCopy.parent))
        }

        dataFileRepository.getDataFileLinksByIds(action.ids).forEach { file ->
            eventPublisher.publishEvent(EventFileCopied(file.dataFileId, UUID.randomUUID(), folderId = folderId, name = "Copy of ${file.name}"))
        }

        clearFileSelection()
        closeDialog()
    }

    fun moveItems(action: FileViewModel.Action, folderId: UUID?, closeDialog: () -> Unit) = viewModelScope.launch {
        val selectedFolders = folderRepository.getFoldersByIds(action.ids)
        val selectedFileLinks = dataFileRepository.getDataFileLinksByIds(action.ids)

        selectedFolders.forEach { eventPublisher.publishEvent(EventFolderMoved(it.id, folderId)) }
        selectedFileLinks.forEach { eventPublisher.publishEvent(EventFileMoved(it.id, folderId)) }

        clearFileSelection()
        closeDialog()
    }

    private fun refreshFileListItems(selectedFolder: Folder?) = viewModelScope.launch {
        fileListItems = fileListItemRepository.getAllPaged(selectedFolder?.id)
    }

    private suspend fun copyFolders(currentFolderId: UUID, newFolderId: UUID) {
        folderRepository.getFoldersByParentId(currentFolderId).forEach {
            eventPublisher.publishEvent(EventFolderCopied(it.id, parentId = newFolderId))
        }
    }

    private suspend fun copyFiles(currentFolderId: UUID, newFolderId: UUID?) = viewModelScope.launch {
        dataFileRepository.getDataFileLinksByFolderId(currentFolderId).forEach { dataFileLink ->
            dataFileRepository.getDataFileById(dataFileLink.dataFileId)?.let { dataFile ->
                eventPublisher.publishEvent(EventFileCopied(dataFile.id, UUID.randomUUID(), newFolderId, dataFile.name))
            }
        }
    }

    private fun clearFileSelection() {
        selectedFolder = null
    }
}