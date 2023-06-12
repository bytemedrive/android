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

    var files = MutableStateFlow(AppState.customer.value!!.files)
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    val selectedFolderId = MutableStateFlow<UUID?>(null)

    private var fileAndFolderList = MutableStateFlow(listOf<Item>())

    private val history = MutableStateFlow(emptyList<UUID>())

    fun openFolder(id: UUID?) {
        selectedFolderId.value = id
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
                ?.map { Item(it.id, it.name, ItemType.Folder, it.starred) }.orEmpty()

            val tempFiles = customer?.files
                ?.filter { file -> file.folderId == folderId }
                ?.map { Item(it.id, it.name, ItemType.File, it.starred) }.orEmpty()

            files.value = customer?.files.orEmpty().toMutableList()
            folders.value = customer?.folders.orEmpty().toMutableList()
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
        folders.value.filter { folder -> action.ids.contains(folder.id) }.forEach { folder ->
            val currentFolderToCopy = folder.copy(id = UUID.randomUUID(), name = "Copy of ${folder.name}", parent = folderId)
            val innerFoldersToCopy = folderManager.findAllFoldersRecursively(folder.id, folders.value).toMutableList()

            innerFoldersToCopy.forEach { innerFolder ->
                val parent = if (innerFolder.parent == folder.id) currentFolderToCopy.id else innerFolder.parent
                val newFolder = innerFolder.copy(id = UUID.randomUUID(), parent = parent)

                copyFolders(innerFolder.id, newFolder.id)
                copyFiles(innerFolder.id, UUID.randomUUID(), newFolder.id)

                eventPublisher.publishEvent(EventFolderCopied(innerFolder.id, newFolder.id, newFolder.parent))
            }

            copyFiles(folder.id, UUID.randomUUID(), currentFolderToCopy.id)

            eventPublisher.publishEvent(EventFolderCopied(folder.id, currentFolderToCopy.id, currentFolderToCopy.parent))
        }

        files.value.filter { file -> action.ids.contains(file.id) }.forEach { file ->
            eventPublisher.publishEvent(EventFileCopied(file.id, UUID.randomUUID(), folderId = folderId, name = "Copy of ${file.name}"))
        }

        closeDialog()
    }

    private suspend fun copyFolders(currentFolderId: UUID, newFolderId: UUID) {
        folders.value.filter { it.parent == currentFolderId }.forEach {
            eventPublisher.publishEvent(EventFolderCopied(it.id, parentId = newFolderId))
        }
    }

    private suspend fun copyFiles(currentFolderId: UUID, newId: UUID, newFolderId: UUID, name: String? = null) {
        files.value.filter { it.folderId == currentFolderId }.forEach { file ->
            eventPublisher.publishEvent(EventFileCopied(file.id, newId, newFolderId, name))
        }
    }

    fun moveItems(action: Action, folderId: UUID, closeDialog: () -> Unit) = viewModelScope.launch {
        val selectedFolders = folders.value.filter { folder -> action.ids.contains(folder.id) }
        val selectedFiles = files.value.filter { file -> action.ids.contains(file.id) }

        selectedFolders.forEach { eventPublisher.publishEvent(EventFolderMoved(it.id, folderId)) }
        selectedFiles.forEach { eventPublisher.publishEvent(EventFileMoved(it.id, folderId)) }

        closeDialog()
    }
}