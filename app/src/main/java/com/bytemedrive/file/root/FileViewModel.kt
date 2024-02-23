package com.bytemedrive.file.root

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.datafile.entity.DataFile
import com.bytemedrive.datafile.entity.DataFileLink
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.control.FileListItemRepository
import com.bytemedrive.file.shared.entity.FileListItem
import com.bytemedrive.file.shared.entity.ItemType
import com.bytemedrive.file.shared.preview.FilePreview
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FileViewModel(
    private val externalScope: CoroutineScope,
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val folderManager: FolderManager,
    private val fileManager: FileManager,
    private val queueFileDownloadRepository: QueueFileDownloadRepository,
    private val folderRepository: FolderRepository,
    private val dataFileRepository: DataFileRepository,
    private val customerRepository: CustomerRepository,
    private val itemRepository: FileListItemRepository
) : ViewModel() {

    private val TAG = FileViewModel::class.qualifiedName

    var selectedFolder by mutableStateOf<Folder?>(null)

    var thumbnails = MutableStateFlow(mapOf<UUID, File?>())

    var items by mutableStateOf(emptyList<FileListItem>())

    var fileListItems = itemRepository.getAllPaged()

    val itemsSelected = MutableStateFlow(emptyList<FileListItem>())

    val fileSelectionDialogOpened = MutableStateFlow(false)

    val action = MutableStateFlow<Action?>(null)

    val dataFilePreview = MutableStateFlow<FilePreview?>(null)

    private var watchItems: Job? = null

    private var watchThumbnails: Job? = null

    fun initialize(context: Context, folderId: UUID?) {
        fileListItems = itemRepository.getAllPaged(folderId)

        watchItems = viewModelScope.launch {
            combine(
                folderRepository.getFoldersByParentIdFlow(folderId),
                dataFileRepository.getDataFileLinksByFolderIdFlow(folderId)
            ) { folders, files ->
                val tempFolders = folders.map { FileListItem(it.id, it.name, ItemType.FOLDER, it.starred, UploadStatus.COMPLETED) }
                val tempFiles = files.map { FileListItem(it.id, it.name, ItemType.FILE, it.starred, it.uploadStatus, it.folderId) }

                tempFolders + tempFiles
            }.collectLatest { items_ ->
                items = items_
            }
        }

        viewModelScope.launch {
            selectedFolder = folderId?.let { folderRepository.getFolderById(it) }
        }

        watchThumbnails(context)
    }

    fun clickFileAndFolder(item: FileListItem) {
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
                            val dataFileIds = dataFileRepository.getDataFileLinksByFolderId(dataFileLink.folderId).map { it.dataFileId }

                            if (dataFile?.contentType == MimeTypes.IMAGE_JPEG) {
                                dataFilePreview.update { FilePreview(dataFile, dataFileIds) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun longClickFileAndFolder(item: FileListItem) =
        itemsSelected.update {
            if (it.contains(item)) {
                it - item
            } else {
                it + item
            }
        }

    fun toggleAllItems(context: Context) {
        if (itemsSelected.value.size == items.size) {
            itemsSelected.update { emptyList() }
        } else {
            itemsSelected.update { items }
            Toast.makeText(context, "${items.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearSelectedItems() = itemsSelected.update { emptyList() }

    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        customerRepository.getCustomer()?.let { customer ->
            dataFileLinks.filter { ids.contains(it.id) }.map { dataFileLink ->
                val physicalFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                if (physicalFileRemovable && customer.walletId != null) {
                    fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                }

                dataFileLink.id
            }.takeIf { it.isNotEmpty() }?.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            // TODO: Remove all data file links and files in deleted folder at once in one EventFolderDeleted
            folders.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders, dataFileLinks).map { dataFileLink ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                    if (physicalFileRemovable && customer.walletId != null) {
                        fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                    }

                    dataFileLink.id
                }.takeIf { it.isNotEmpty() }?.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders) + folder).map { it.id }
            }.flatten().takeIf { it.isNotEmpty() }?.let { foldersToRemove -> eventPublisher.publishEvent(EventFolderDeleted(foldersToRemove)) }
        }
    }

    fun removeFile(dataFileLinkId: UUID) = externalScope.launch {
        customerRepository.getCustomer()?.let { customer ->
            dataFileRepository.getDataFileLinkById(dataFileLinkId)?.let { dataFileLink ->
                eventPublisher.publishEvent(EventFileDeleted(listOf(dataFileLinkId)))

                val physicalFileRemovable = dataFileRepository.getDataFileLinksByDataFileId(dataFileLink.dataFileId).isEmpty()

                if (physicalFileRemovable && customer.walletId != null) {
                    fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                }
            }
        }
    }

    fun removeFolder(id: UUID) = externalScope.launch {
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        customerRepository.getCustomer()?.let { customer ->
            folderRepository.getFolderById(id)?.let { folder ->
                fileManager.findAllFilesRecursively(id, folders, dataFileLinks).forEach { dataFileLink ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                    eventPublisher.publishEvent(EventFileDeleted(listOf(dataFileLink.id)))

                    if (physicalFileRemovable && customer.walletId != null) {
                        fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                    }
                }
                (folderManager.findAllFoldersRecursively(id, folders) + folder).forEach {
                    eventPublisher.publishEvent(EventFolderDeleted(listOf(it.id)))
                }
            }
        }
    }

    fun toggleStarredFile(id: UUID, value: Boolean) = externalScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFileStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFileStarAdded(id))
        }
    }

    fun toggleStarredFolder(id: UUID, value: Boolean) = externalScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFolderStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFolderStarAdded(id))
        }
    }

    fun useSelectionScreenToMoveItems(id: UUID, folderId: UUID?) = useSelectionScreenToMoveItems(listOf(id), folderId)

    fun useSelectionScreenToMoveItems(ids: List<UUID>, folderId: UUID?) {
        action.update { Action(ids, Action.Type.MOVE_ITEMS, folderId) }
        fileSelectionDialogOpened.update { true }
    }

    fun useSelectionScreenToCopyItems(id: UUID, folderId: UUID?) = useSelectionScreenToCopyItems(listOf(id), folderId)

    fun useSelectionScreenToCopyItems(ids: List<UUID>, folderId: UUID?) {
        action.update { Action(ids, Action.Type.COPY_ITEMS, folderId) }
        fileSelectionDialogOpened.update { true }
    }

    fun downloadFiles(ids: List<UUID>) = viewModelScope.launch {
        queueFileDownloadRepository.addFiles(ids)
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun downloadFile(id: UUID) = viewModelScope.launch {
        queueFileDownloadRepository.addFile(id)
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun cancelJobs() {
        watchItems?.cancel()
        watchThumbnails?.cancel()
    }

    private fun watchThumbnails(context: Context) {
        watchThumbnails = viewModelScope.launch {
            dataFileRepository.getAllDataFileFlow().collectLatest { dataFiles ->
                thumbnails.update {
                    dataFileRepository.getAllDataFileLinks()
                        .mapNotNull { dataFileLink -> findThumbnailForDataFileLink(dataFileLink, dataFiles, context)?.let { thumbnail -> dataFileLink.id to thumbnail } }
                        .toMap()
                }
            }
        }
    }

    private fun findThumbnailForDataFileLink(dataFileLink: DataFileLink, dataFiles: List<DataFile>, context: Context): File? =
        dataFiles
            .find { it.id == dataFileLink.dataFileId }
            ?.thumbnails
            ?.find { it.resolution == Resolution.P360 }
            ?.let {
                val thumbnailName = FileManager.getThumbnailName(dataFileLink.dataFileId, it.resolution)
                val filePath = "${context.filesDir}/$thumbnailName"
                val file = File(filePath)

                if (file.exists()) file else null
            }

    data class Action(val ids: List<UUID>, val type: Type, val folderId: UUID?) {
        enum class Type(type: String) {
            COPY_ITEMS("copyItems"),
            MOVE_ITEMS("moveItems")
        }
    }
}
