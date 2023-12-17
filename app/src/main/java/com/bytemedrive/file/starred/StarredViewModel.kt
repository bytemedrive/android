package com.bytemedrive.file.starred

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bytemedrive.file.root.DataFile
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.FilePagingSource
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.root.Item
import com.bytemedrive.file.root.ItemType
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class StarredViewModel(
    private val appNavigator: AppNavigator,
    private val eventPublisher: EventPublisher,
    private val fileRepository: FileRepository,
    private val fileManager: FileManager,
    private val folderManager: FolderManager
) : ViewModel() {

    var list = MutableStateFlow(listOf<Item>())

    var starred = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    val dataFilePreview = MutableStateFlow<DataFile?>(null)

    private var watchJob: Job? = null

    fun init() {
        watchItems()
    }

    fun clickFileAndFolder(item: Item) {
        val anyFileSelected = itemsSelected.value.isNotEmpty()

        if (anyFileSelected) {
            longClickFileAndFolder(item)
        } else {
            when (item.type) {
                ItemType.Folder -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))

                ItemType.File -> {
                    AppState.customer!!.dataFilesLinks.value.find { it.id == item.id }?.let { dataFileLink ->
                        val dataFile = AppState.customer!!.dataFiles.value.find { dataFile -> dataFile.id == dataFileLink.dataFileId }

                        if (dataFile?.contentType == MimeTypes.IMAGE_JPEG) {
                            dataFilePreview.update { dataFile }
                        }
                    }
                }
            }
        }
    }

    fun longClickFileAndFolder(item: Item) =
        itemsSelected.update {
            if (it.contains(item)) {
                itemsSelected.value - item
            } else {
                itemsSelected.value + item
            }
        }

    fun clearSelectedItems() = itemsSelected.update { emptyList() }

    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        val dataFileLinks = AppState.customer!!.dataFilesLinks
        val folders = AppState.customer!!.folders

        AppState.customer?.wallet?.let { walletId ->
            dataFileLinks.value.filter { ids.contains(it.id) }.map { file ->
                val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id }

                if (physicalFileRemovable) {
                    fileRepository.remove(walletId, file.id)
                }

                file.id
            }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            folders.value.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders.value, dataFileLinks.value).map { file ->
                    val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id }

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }

                    file.id
                }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders.value) + folder).map { it.id }
            }.flatten().let { foldersToRemove -> eventPublisher.publishEvent(EventFolderDeleted(foldersToRemove)) }
        }
    }

    fun getStarredFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(starred.value) }
        ).flow

    fun toggleAllItems(context: Context) {
        if (itemsSelected.value.size == starred.value.size) {
            itemsSelected.update { emptyList() }
        } else {
            itemsSelected.update { starred.value }
            Toast.makeText(context, "${starred.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs() {
        watchJob?.cancel()
    }

    private fun watchItems() {
        watchJob = viewModelScope.launch {
            combine(AppState.customer!!.folders, AppState.customer!!.dataFilesLinks) { folders, dataFileLinks ->
                val tempFolders = folders
                    .filter { it.starred }
                    .map { Item(it.id, it.name, ItemType.Folder, it.starred, false) }

                val tempFileLinks = dataFileLinks
                    .filter { it.starred }
                    .map { Item(it.id, it.name, ItemType.File, it.starred, false) }

                tempFolders + tempFileLinks
            }.collectLatest { items ->
                starred.update { items }
            }
        }
    }
}