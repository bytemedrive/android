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
import androidx.paging.cachedIn
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FileViewModel(
    context: Context,
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val folderManager: FolderManager,
    private val fileManager: FileManager,
    private val queueFileUploadRepository: QueueFileUploadRepository,
    private val queueFileDownloadRepository: QueueFileDownloadRepository,
) : ViewModel() {

    private val TAG = FileViewModel::class.qualifiedName

    var thumbnails = MutableStateFlow(mapOf<UUID, Bitmap?>())

    val selectedFolder = MutableStateFlow<Folder?>(null)

    var items = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    val fileSelectionDialogOpened = MutableStateFlow(false)

    val action = MutableStateFlow<Action?>(null)

    val dataFilePreview = MutableStateFlow<DataFile?>(null)

    init {
        getThumbnails(context)
        watchItems(context)
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
        itemsSelected.update { items ->
            if (items.contains(item)) {
                items - item
            } else {
                items + item
            }
        }

    fun toggleAllItems(context: Context) {
        if (itemsSelected.value.size == items.value.size) {
            itemsSelected.update { emptyList() }
        } else {
            itemsSelected.update { items.value }
            Toast.makeText(context, "${items.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearSelectedItems() = itemsSelected.update { emptyList() }

    fun singleDataFileLink(id: UUID) = AppState.customer!!.dataFilesLinks.value.find { it.id == id }

    fun singleFolder(id: UUID) = AppState.customer!!.folders.value.find { it.id == id }

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

    fun removeFile(id: UUID, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        val dataFileLinks = AppState.customer!!.dataFilesLinks

        AppState.customer?.wallet?.let { walletId ->
            val file = dataFileLinks.value.find { it.id == id }
            val physicalFileRemovable = dataFileLinks.value.none { it.id == file?.id }

            eventPublisher.publishEvent(EventFileDeleted(listOf(id)))

            if (physicalFileRemovable) {
                fileRepository.remove(walletId, id)
            }

            onSuccess?.invoke()
        }
    }

    fun removeFolder(id: UUID, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        val dataFileLinks = AppState.customer!!.dataFilesLinks
        val folders = AppState.customer!!.folders

        AppState.customer?.wallet?.let { walletId ->
            folders.value.find { it.id == id }?.let { folder ->
                fileManager.findAllFilesRecursively(id, folders.value, dataFileLinks.value).forEach { file ->
                    val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id }

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

    fun useSelectionScreenToMoveItems(id: UUID, folderId: UUID?) = useSelectionScreenToMoveItems(listOf(id), folderId)

    fun useSelectionScreenToMoveItems(ids: List<UUID>, folderId: UUID?) {
        action.update { Action(ids, Action.Type.MoveItems, folderId) }
        fileSelectionDialogOpened.update { true }
    }

    fun useSelectionScreenToCopyItems(id: UUID, folderId: UUID?) = useSelectionScreenToCopyItems(listOf(id), folderId)

    fun useSelectionScreenToCopyItems(ids: List<UUID>, folderId: UUID?) {
        action.update { Action(ids, Action.Type.CopyItems, folderId) }
        fileSelectionDialogOpened.update { true }
    }

    fun downloadFiles(ids: List<UUID>) = viewModelScope.launch {
        ids.forEach { queueFileDownloadRepository.addFile(it) }
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun downloadFile(id: UUID) = viewModelScope.launch {
        queueFileDownloadRepository.addFile(id)
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun getItemsPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource2(queueFileUploadRepository, selectedFolder.value) }
        ).flow

    private fun watchItems(context: Context) = viewModelScope.launch {
        combine(
            selectedFolder,
            AppState.customer!!.folders,
            AppState.customer!!.dataFilesLinks,
            queueFileUploadRepository.watchFiles()
        ) { selectedFolder, folders, dataFilesLinks, filesToUpload ->
            val tempFolders = folders
                .filter { folder -> folder.parent == selectedFolder?.id }
                .map { Item(it.id, it.name, ItemType.Folder, it.starred, false) }

            val tempFiles = dataFilesLinks
                .filter { file -> file.folderId == selectedFolder?.id }
                .map { Item(it.id, it.name, ItemType.File, it.starred, false, it.folderId) }

            val tempFilesToUpload = filesToUpload
                .map { Item(it.id, it.name, ItemType.File, starred = false, uploading = true, folderId = it.folderId) }
                .filter { it.folderId?.equals(selectedFolder?.id) ?: true }

            tempFilesToUpload + tempFolders + tempFiles
        }.collectLatest { collectedItems ->
            items.update { collectedItems }
            getThumbnails(context)
        }
    }

    private fun getThumbnails(context: Context) {
        thumbnails.update {
            AppState.customer!!.dataFilesLinks.value.mapNotNull { dataFileLink ->
                findThumbnailForDataFileLink(dataFileLink, context)?.let { thumbnails ->
                    dataFileLink.id to thumbnails
                }
            }.toMap()
        }
    }

    private fun findThumbnailForDataFileLink(dataFileLink: DataFileLink, context: Context): Bitmap? {
        val dataFiles = AppState.customer?.dataFiles

        if (dataFiles != null) {
            val dataFile = AppState.customer!!.dataFiles.value.find { it.id == dataFileLink.dataFileId }
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

data class Action(val ids: List<UUID>, val type: Type, val folderId: UUID?) {
    enum class Type(type: String) {
        CopyItems("copyItems"),
        MoveItems("moveItems")
    }
}