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

    fun openFolder(id: UUID?) {
        selectedFolder.value = AppState.customer.value!!.folders.find { it.id == id }
        updateFileAndFolderList(id)
    }

    fun addToHistory(id: UUID) {
        history.value = history.value + id
    }

    fun goBack(closeDialog: () -> Unit) {
        if (history.value.lastOrNull() == null) {
            closeDialog()
        } else {
            history.value = history.value.subList(0, max(0, history.value.size - 1))
            openFolder(history.value.lastOrNull())
        }
    }

    fun updateFileAndFolderList(folderId: UUID?) = viewModelScope.launch {
        AppState.customer.collectLatest { customer ->
            val tempFolders = customer?.folders
                ?.filter { folder -> folder.parent == folderId }
                ?.map { Item(it.id, it.name, ItemType.Folder, it.starred, false) }.orEmpty()

            val tempFiles = customer?.dataFilesLinks
                ?.filter { dataFileLink -> dataFileLink.folderId == folderId }
                ?.map { Item(it.id, it.name, ItemType.File, it.starred, false) }.orEmpty()

            fileAndFolderList.value = tempFolders + tempFiles
        }
    }

    fun getFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(fileAndFolderList.value) }
        ).flow.cachedIn(viewModelScope)

    // TODO: Rework with use of recursive function to have single iteration
    fun copyItem(action: Action, folderId: UUID?, closeDialog: () -> Unit) = viewModelScope.launch {
        val folders = AppState.customer.value!!.folders

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

        AppState.customer.value!!.dataFilesLinks.filter { file -> action.ids.contains(file.id) }.forEach { file ->
            eventPublisher.publishEvent(EventFileCopied(file.id, UUID.randomUUID(), folderId = folderId, name = "Copy of ${file.name}"))
        }

        clearFileSelection()
        closeDialog()
    }

    fun moveItems(action: Action, folderId: UUID, closeDialog: () -> Unit) = viewModelScope.launch {
        val customer = AppState.customer.value!!
        val selectedFolders = customer.folders.filter { folder -> action.ids.contains(folder.id) }
        val selectedFiles = customer.dataFilesLinks.filter { file -> action.ids.contains(file.id) }

        selectedFolders.forEach { eventPublisher.publishEvent(EventFolderMoved(it.id, folderId)) }
        selectedFiles.forEach { eventPublisher.publishEvent(EventFileMoved(it.id, folderId)) }

        clearFileSelection()
        closeDialog()
    }

    private suspend fun copyFolders(currentFolderId: UUID, newFolderId: UUID) {
        AppState.customer.value!!.folders.filter { it.parent == currentFolderId }.forEach {
            eventPublisher.publishEvent(EventFolderCopied(it.id, parentId = newFolderId))
        }
    }

    private suspend fun copyFiles(currentFolderId: UUID, newFolderId: UUID?) {
        AppState.customer.value!!.dataFilesLinks.filter { it.folderId == currentFolderId }.forEach { dataFileLink ->
            AppState.customer.value!!.dataFiles.find { it.id == dataFileLink.dataFileId }?.let { dataFile ->
                eventPublisher.publishEvent(EventFileCopied(dataFile.id, UUID.randomUUID(), newFolderId, dataFile.name))
            }
        }
    }

    private fun clearFileSelection() {
        selectedFolder.value = null
    }
}