package com.bytemedrive.file.shared.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bytemedrive.file.root.Action
import com.bytemedrive.file.root.EventFileCopied
import com.bytemedrive.file.root.EventFileMoved
import com.bytemedrive.file.root.FilePagingSource
import com.bytemedrive.file.root.Item
import com.bytemedrive.file.root.ItemType
import com.bytemedrive.folder.EventFolderCopied
import com.bytemedrive.folder.EventFolderMoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

class FileSelectionViewModel(
    private val eventPublisher: EventPublisher,
    private val folderManager: FolderManager
) : ViewModel() {

    val selectedFolder = MutableStateFlow<Folder?>(null)

    private var fileAndFolderList = MutableStateFlow(listOf<Item>())

    private val history = MutableStateFlow(emptyList<UUID>())

    init {
        watchFileAndFolderList()
    }

    fun openFolder(id: UUID?) =
        selectedFolder.update { AppState.customer!!.folders.value.find { it.id == id } }

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

    fun getFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(fileAndFolderList.value) }
        ).flow.cachedIn(viewModelScope)

    // TODO: Rework with use of recursive function to have single iteration
    fun copyItem(action: Action, folderId: UUID?, closeDialog: () -> Unit) = viewModelScope.launch {
        val folders = AppState.customer!!.folders.value

        folders.filter { folder -> action.ids.contains(folder.id) }.forEach { folder ->
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

        AppState.customer!!.dataFilesLinks.value.filter { file -> action.ids.contains(file.id) }.forEach { file ->
            eventPublisher.publishEvent(EventFileCopied(file.dataFileId, UUID.randomUUID(), folderId = folderId, name = "Copy of ${file.name}"))
        }

        clearFileSelection()
        closeDialog()
    }

    fun moveItems(action: Action, folderId: UUID, closeDialog: () -> Unit) = viewModelScope.launch {
        val customer = AppState.customer!!
        val selectedFolders = customer.folders.value.filter { folder -> action.ids.contains(folder.id) }
        val selectedFiles = customer.dataFilesLinks.value.filter { file -> action.ids.contains(file.id) }

        selectedFolders.forEach { eventPublisher.publishEvent(EventFolderMoved(it.id, folderId)) }
        selectedFiles.forEach { eventPublisher.publishEvent(EventFileMoved(it.id, folderId)) }

        clearFileSelection()
        closeDialog()
    }

    private fun watchFileAndFolderList() = viewModelScope.launch {
        combine(selectedFolder, AppState.customer!!.folders, AppState.customer!!.dataFilesLinks) { selectedFolder, folders, dataFileLinks ->
            val tempFolders = folders
                .filter { folder -> folder.parent == selectedFolder?.id }
                .map { Item(it.id, it.name, ItemType.Folder, it.starred, false) }

            val tempFileLinks = dataFileLinks
                .filter { dataFileLink -> dataFileLink.folderId == selectedFolder?.id }
                .map { Item(it.id, it.name, ItemType.File, it.starred, false) }

            tempFolders + tempFileLinks
        }.collectLatest { items ->
            fileAndFolderList.update { items }
        }
    }

    private suspend fun copyFolders(currentFolderId: UUID, newFolderId: UUID) {
        AppState.customer!!.folders.value.filter { it.parent == currentFolderId }.forEach {
            eventPublisher.publishEvent(EventFolderCopied(it.id, parentId = newFolderId))
        }
    }

    private suspend fun copyFiles(currentFolderId: UUID, newFolderId: UUID?) {
        AppState.customer!!.dataFilesLinks.value.filter { it.folderId == currentFolderId }.forEach { dataFileLink ->
                AppState.customer!!.dataFiles.value.find { it.id == dataFileLink.dataFileId }?.let { dataFile ->
                eventPublisher.publishEvent(EventFileCopied(dataFile.id, UUID.randomUUID(), newFolderId, dataFile.name))
            }
        }
    }

    private fun clearFileSelection() =
        selectedFolder.update { null }

}