package com.bytemedrive.file.starred

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.FilePagingSource
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.root.Item
import com.bytemedrive.file.root.ItemType
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.preview.FilePreview
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.navigation.AppNavigator
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
    private val folderManager: FolderManager,
    private val dataFileRepository: DataFileRepository,
    private val folderRepository: FolderRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    var list = MutableStateFlow(listOf<Item>())

    var starred = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    val dataFilePreview = MutableStateFlow<FilePreview?>(null)

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
                ItemType.FOLDER -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))

                ItemType.FILE -> {
                    viewModelScope.launch {
                        dataFileRepository.getDataFileLinkById(item.id)?.let { dataFileLink ->
                            val dataFile = dataFileRepository.getDataFileById(dataFileLink.dataFileId)
                            val dataFileIdsStarred = dataFileRepository.getDataFileLinksStarred(starred = true).map { it.dataFileId }

                            if (dataFile?.contentType == MimeTypes.IMAGE_JPEG) {
                                dataFilePreview.update { FilePreview(dataFile, dataFileIdsStarred) }
                            }
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
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        // TODO: Check the file is removed
        customerRepository.getCustomer()?.let { customer ->
            dataFileLinks.filter { ids.contains(it.id) }.map { file ->
                val physicalFileRemovable = dataFileLinks.none { it.id == file.id }

                if (physicalFileRemovable && customer.walletId != null) {
                    fileRepository.remove(customer.walletId, file.id)
                }

                file.id
            }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            folders.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders, dataFileLinks).map { file ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == file.id }

                    if (physicalFileRemovable && customer.walletId != null) {
                        fileRepository.remove(customer.walletId, file.id)
                    }

                    file.id
                }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders) + folder).map { it.id }
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
            combine(folderRepository.getAllFoldersFlow(starred = true), dataFileRepository.getDataFileLinksStarredFlow(starred = true)) { folders, dataFileLinks ->
                val tempFolders = folders.map { Item(it.id, it.name, ItemType.FOLDER, it.starred, false) }
                val tempFileLinks = dataFileLinks.map { Item(it.id, it.name, ItemType.FILE, it.starred, false) }

                tempFolders + tempFileLinks
            }.collectLatest { items ->
                starred.update { items }
            }
        }
    }
}