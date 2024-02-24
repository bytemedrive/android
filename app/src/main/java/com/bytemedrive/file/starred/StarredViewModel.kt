package com.bytemedrive.file.starred

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.control.FileListItemRepository
import com.bytemedrive.file.shared.entity.FileListItem
import com.bytemedrive.file.shared.entity.ItemType
import com.bytemedrive.file.shared.preview.FilePreview
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class StarredViewModel(
    private val appNavigator: AppNavigator,
    private val eventPublisher: EventPublisher,
    private val fileRepository: FileRepository,
    private val fileManager: FileManager,
    private val folderManager: FolderManager,
    private val dataFileRepository: DataFileRepository,
    private val folderRepository: FolderRepository,
    private val customerRepository: CustomerRepository,
    private val fileListItemRepository: FileListItemRepository
) : ViewModel() {

    var fileListItems = fileListItemRepository.getAllStarredPaged(starred = true)

    var thumbnails by mutableStateOf(mapOf<UUID, File?>())

    var starred by mutableStateOf(listOf<FileListItem>())

    var itemsSelected by mutableStateOf(emptyList<FileListItem>())

    var dataFilePreview by mutableStateOf<FilePreview?>(null)

    private var watchJob: Job? = null

    private var watchThumbnails: Job? = null

    fun initialize(context: Context) {
        watchItems()
        watchThumbnails(context)
    }

    fun clickFileAndFolder(item: FileListItem) {
        val anyFileSelected = itemsSelected.isNotEmpty()

        if (anyFileSelected) {
            longClickFileAndFolder(item)
        } else {
            when (item.type) {
                ItemType.FOLDER -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))

                ItemType.FILE -> {
                    viewModelScope.launch {
                        dataFileRepository.getDataFileLinkById(item.id)?.let { dataFileLink ->
                            val dataFileLinks = dataFileRepository.getDataFileLinksStarred(true)
                            val dataFiles = dataFileRepository.getDataFilesByIds(dataFileLinks.map { it.dataFileId })
                            val dataFileLinkPreviews = dataFileLinks
                                .filter { dataFiles.find { dataFile -> dataFile.id == it.dataFileId }?.contentType == MimeTypes.IMAGE_JPEG }
                                .map { it.id }

                            dataFilePreview = FilePreview(dataFileLink, dataFileLinkPreviews)
                        }
                    }
                }
            }
        }
    }

    fun longClickFileAndFolder(item: FileListItem) {
        itemsSelected = if (itemsSelected.contains(item)) {
            itemsSelected - item
        } else {
            itemsSelected + item
        }
    }

    fun clearSelectedItems() {
        itemsSelected = emptyList()
    }

    // TODO: do refactor - https://github.com/bytemedrive/android/issues/212
    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        customerRepository.getCustomer()?.let { customer ->
            dataFileLinks.filter { ids.contains(it.id) }.map { dataFileLink ->
                val dataFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                if (dataFileRemovable && customer.walletId != null) {
                    val fileChunkIdsToRemove = dataFileRepository.getFileChunkIds(dataFileLink.dataFileId)

                    fileRepository.remove(customer.walletId, fileChunkIdsToRemove)
                }

                dataFileLink.id
            }.takeIf { it.isNotEmpty() }?.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            // TODO: Remove all data file links and files in deleted folder at once in one EventFolderDeleted
            folders.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders, dataFileLinks).map { dataFileLink ->
                    val dataFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                    if (dataFileRemovable && customer.walletId != null) {
                        val fileChunkIdsToRemove = dataFileRepository.getFileChunkIds(dataFileLink.dataFileId)

                        fileRepository.remove(customer.walletId, fileChunkIdsToRemove)
                    }

                    dataFileLink.id
                }.takeIf { it.isNotEmpty() }?.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders) + folder).map { it.id }
            }.flatten().takeIf { it.isNotEmpty() }?.let { foldersToRemove -> eventPublisher.publishEvent(EventFolderDeleted(foldersToRemove)) }
        }
    }

    fun toggleAllItems(context: Context) {
        if (itemsSelected.size == starred.size) {
            itemsSelected = emptyList()
        } else {
            itemsSelected = starred
            Toast.makeText(context, "${starred.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs() {
        watchJob?.cancel()
        watchThumbnails?.cancel()
    }

    private fun watchThumbnails(context: Context) {
        watchThumbnails = viewModelScope.launch {
            dataFileRepository.getAllDataFileFlow().collectLatest { dataFiles ->
                thumbnails =
                    dataFileRepository.getAllDataFileLinks()
                        .mapNotNull { dataFileLink -> fileManager.findThumbnailForDataFileLink(dataFileLink, dataFiles, context)?.let { thumbnail -> dataFileLink.id to thumbnail } }
                        .toMap()
            }
        }
    }


    private fun watchItems() {
        watchJob = viewModelScope.launch {
            fileListItemRepository.getAllStarredFlow(starred = true).collect { items ->
                starred = items
            }
        }
    }
}