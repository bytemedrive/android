package com.bytemedrive.file.root

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.service.ServiceManager
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FileViewModel(
    context: Context,
    serviceManager: ServiceManager,
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val folderManager: FolderManager,
    private val fileManager: FileManager,
    private val queueFileUploadRepository: QueueFileUploadRepository,
    private val queueFileDownloadRepository: QueueFileDownloadRepository,
) : ViewModel() {

    private val TAG = FileViewModel::class.qualifiedName

    var dataFileLinks = MutableStateFlow(AppState.customer.value!!.dataFilesLinks)
    var thumbnails = MutableStateFlow(mapOf<UUID, Bitmap?>())
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var items = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    var itemsUploading: Flow<List<Item>> = emptyFlow()

    val fileSelectionDialogOpened = MutableStateFlow(false)

    val action = MutableStateFlow<Action?>(null)

    val dataFilePreview = MutableStateFlow<DataFile?>(null)

    init {
        getThumbnails(context)
        watchFilesToUpload()
        serviceManager.startServices(context)
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

    fun toggleAllItems(context: Context) {
        if (itemsSelected.value.size == items.value.size) {
            itemsSelected.value = emptyList()
        } else {
            itemsSelected.value = items.value
            Toast.makeText(context, "${items.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearSelectedItems() {
        itemsSelected.value = emptyList()
    }

    fun updateItems(folderId: UUID?, context: Context) = viewModelScope.launch {
        AppState.customer.collectLatest { customer ->
            val tempFolders = customer?.folders
                ?.filter { folder -> folder.parent == folderId }
                ?.map { Item(it.id, it.name, ItemType.Folder, it.starred, false) }.orEmpty()

            val tempFiles = customer?.dataFilesLinks
                ?.filter { file -> file.folderId == folderId }
                ?.map { Item(it.id, it.name, ItemType.File, it.starred, false, it.folderId) }.orEmpty()

            dataFileLinks.value = customer?.dataFilesLinks.orEmpty().toMutableList()
            folders.value = customer?.folders.orEmpty().toMutableList()
            items.value = tempFolders + tempFiles
            getThumbnails(context)
        }
    }

    fun singleDataFileLink(id: UUID) = dataFileLinks.value.find { it.id == id }

    fun singleFolder(id: UUID) = folders.value.find { it.id == id }

    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            dataFileLinks.value.filter { ids.contains(it.id) }.map { file ->
                val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                if (physicalFileRemovable) {
                    fileRepository.remove(walletId, file.id)
                }

                file.id
            }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            folders.value.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders.value, dataFileLinks.value).map { file ->
                    val physicalFileRemovable =
                        dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }

                    file.id
                }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders.value) + folder).map { it.id }
            }.flatten().let { foldersToRemove -> eventPublisher.publishEvent(EventFolderDeleted(foldersToRemove)) }
        }
    }

    fun removeFile(id: UUID, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            val file = dataFileLinks.value.find { it.id == id }
            val physicalFileRemovable = dataFileLinks.value.none { it.id == file?.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

            eventPublisher.publishEvent(EventFileDeleted(listOf(id)))

            if (physicalFileRemovable) {
                fileRepository.remove(walletId, id)
            }

            onSuccess?.invoke()
        }
    }

    fun removeFolder(id: UUID, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            folders.value.find { it.id == id }?.let { folder ->
                fileManager.findAllFilesRecursively(id, folders.value, dataFileLinks.value).forEach { file ->
                    val physicalFileRemovable =
                        dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                    eventPublisher.publishEvent(EventFileDeleted(listOf(file.id)))

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }
                }
                (folderManager.findAllFoldersRecursively(id, folders.value) + folder).forEach {
                    eventPublisher.publishEvent(EventFolderDeleted(listOf(it.id)))
                }
            }

            onSuccess?.invoke()
        }
    }

    fun toggleStarredFile(id: UUID, value: Boolean, onSuccess: () -> Unit) = viewModelScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFileStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFileStarAdded(id))
        }

        onSuccess()
    }

    fun toggleStarredFolder(id: UUID, value: Boolean, onSuccess: () -> Unit) = viewModelScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFolderStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFolderStarAdded(id))
        }

        onSuccess()
    }

    fun getItemsPages(items: List<Item>): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(items) }
        ).flow

    fun useSelectionScreenToMoveItems(id: UUID) = useSelectionScreenToMoveItems(listOf(id))

    fun useSelectionScreenToMoveItems(ids: List<UUID>) {
        action.value = Action(ids, Action.Type.MoveItems)
        fileSelectionDialogOpened.value = true
    }

    fun useSelectionScreenToCopyItems(id: UUID) = useSelectionScreenToCopyItems(listOf(id))

    fun useSelectionScreenToCopyItems(ids: List<UUID>) {
        action.value = Action(ids, Action.Type.CopyItems)
        fileSelectionDialogOpened.value = true
    }

    fun downloadFiles(ids: List<UUID>) = viewModelScope.launch {
        ids.forEach { queueFileDownloadRepository.addFile(it) }
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun downloadFile(id: UUID) = viewModelScope.launch {
        queueFileDownloadRepository.addFile(id)
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    private fun watchFilesToUpload() = viewModelScope.launch {
        itemsUploading = queueFileUploadRepository.watchFiles().map { files ->
            files.map { Item(it.id, it.name, ItemType.File, starred = false, uploading = true, folderId = it.folderId) }
        }

    }

    private fun getThumbnails(context: Context) = viewModelScope.launch {
        thumbnails.value = dataFileLinks.value.associate { dataFileLink ->
            dataFileLink.id to findThumbnailForDataFileLink(dataFileLink, context)
        }
    }

    private fun findThumbnailForDataFileLink(dataFileLink: DataFileLink, context: Context): Bitmap? {
        val dataFiles = AppState.customer.value?.dataFiles
        if (dataFiles != null) {
            val dataFile = dataFiles.find { it.id == dataFileLink.dataFileId }
            val thumbnail = dataFile?.thumbnails?.find { thumbnail -> thumbnail.resolution == Resolution.P360 }

            return thumbnail?.let {
                val thumbnailName = FileManager.getThumbnailName(dataFile.id, thumbnail.resolution)
                val filePath = "${context.filesDir}/$thumbnailName"
                val file = File(filePath)

                if (file.exists()) BitmapFactory.decodeFile("${context.filesDir}/$thumbnailName") else null
            }
        }

        return null
    }
}

data class Action(val ids: List<UUID>, val type: Type) {
    enum class Type(type: String) {
        CopyItems("copyItems"),
        MoveItems("moveItems")
    }
}