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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class StarredViewModel(
    private val appNavigator: AppNavigator,
    private val eventPublisher: EventPublisher,
    private val fileRepository: FileRepository,
    private val fileManager: FileManager,
    private val folderManager: FolderManager
) : ViewModel() {

    var dataFileLinks = MutableStateFlow(AppState.customer.value!!.dataFilesLinks)
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var list = MutableStateFlow(listOf<Item>())
    var starred = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    val dataFilePreview = MutableStateFlow<DataFile?>(null)

    init {
        viewModelScope.launch {
            AppState.customer.collectLatest { customer ->
                val folders = customer?.folders?.filter { it.starred }?.map { Item(it.id, it.name, ItemType.Folder, it.starred) }.orEmpty().toMutableList()
                val dataFileLinks = customer?.dataFilesLinks?.filter { it.starred }?.map { Item(it.id, it.name, ItemType.File, it.starred) }.orEmpty().toMutableList()

                starred.value = folders + dataFileLinks
            }
        }
    }

    fun clickFileAndFolder(item: Item) {
        val anyFileSelected = itemsSelected.value.isNotEmpty()

        if (anyFileSelected) {
            longClickFileAndFolder(item)
        } else {
            when (item.type) {
                ItemType.Folder -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))
                ItemType.File -> {
                    AppState.customer.value?.dataFilesLinks?.find { it.id == item.id }?.let { dataFileLink ->
                        val dataFile = AppState.customer.value?.dataFiles?.find { dataFile -> dataFile.id == dataFileLink.dataFileId }

                        if (dataFile?.contentType == MimeTypes.IMAGE_JPEG) {
                            dataFilePreview.value = dataFile
                        }
                    }
                }
            }
        }
    }

    fun longClickFileAndFolder(item: Item) {
        itemsSelected.value = if (itemsSelected.value.contains(item)) {
            itemsSelected.value - item
        } else {
            itemsSelected.value + item
        }
    }

    fun clearSelectedItems() {
        itemsSelected.value = emptyList()
    }

    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            dataFileLinks.value.filter { ids.contains(it.id) }.forEach { file ->
                val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                eventPublisher.publishEvent(EventFileDeleted(file.id))

                if (physicalFileRemovable) {
                    fileRepository.remove(walletId, file.id)
                }
            }
            folders.value.filter { ids.contains(it.id) }.forEach { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders.value, dataFileLinks.value).forEach { file ->
                    val physicalFileRemovable =
                        dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                    eventPublisher.publishEvent(EventFileDeleted(file.id))

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }
                }
                (folderManager.findAllFoldersRecursively(folder.id, folders.value) + folder).forEach {
                    eventPublisher.publishEvent(EventFolderDeleted(it.id))
                }
            }
        }
    }

    fun getStarredFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(starred.value) }
        ).flow

    fun toggleAllItems(context: Context) {
        if (itemsSelected.value.size == starred.value.size) {
            itemsSelected.value = emptyList()
        } else {
            itemsSelected.value = starred.value
            Toast.makeText(context, "${starred.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }
}