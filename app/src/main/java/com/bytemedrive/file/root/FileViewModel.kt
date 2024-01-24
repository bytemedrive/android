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
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FileViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val folderManager: FolderManager,
    private val fileManager: FileManager,
    private val queueFileDownloadRepository: QueueFileDownloadRepository,
    private val folderRepository: FolderRepository,
    private val dataFileRepository: DataFileRepository
) : ViewModel() {

    private val TAG = FileViewModel::class.qualifiedName

    var selectedFolder by mutableStateOf<Folder?>(null)

    var thumbnails = MutableStateFlow(mapOf<UUID, Bitmap?>())

    var items = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    var itemsUploading: Flow<List<Item>> = emptyFlow()

    val fileSelectionDialogOpened = MutableStateFlow(false)

    val action = MutableStateFlow<Action?>(null)

    val dataFilePreview = MutableStateFlow<DataFileEntity?>(null)

    private var watchJob: Job? = null


    fun clickFileAndFolder(item: Item) {
        val anyFileSelected = itemsSelected.value.isNotEmpty()

        if (anyFileSelected) {
            longClickFileAndFolder(item)
        } else {
            when (item.type) {
                ItemType.FOLDER -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))

                ItemType.FILE -> {
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
                it - item
            } else {
                it + item
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

    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        AppState.customer?.wallet?.let { walletId ->
            dataFileLinks.filter { ids.contains(it.id) }.map { file ->
                val physicalFileRemovable = dataFileLinks.none { it.id == file.id }

                if (physicalFileRemovable) {
                    fileRepository.remove(walletId, file.id)
                }

                file.id
            }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            folders.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders, dataFileLinks).map { file ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == file.id }

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }

                    file.id
                }.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders) + folder).map { it.id }
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
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        AppState.customer?.wallet?.let { walletId ->
            folders.find { it.id == id }?.let { folder ->
                fileManager.findAllFilesRecursively(id, folders, dataFileLinks).forEach { file ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == file.id }

                    eventPublisher.publishEvent(EventFileDeleted(listOf(file.id)))

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }
                }
                (folderManager.findAllFoldersRecursively(id, folders) + folder).forEach {
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
        ids.forEach { queueFileDownloadRepository.addFile(it) }
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun downloadFile(id: UUID) = viewModelScope.launch {
        queueFileDownloadRepository.addFile(id)
        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
    }

    fun cancelJobs() {
        watchJob?.cancel()
    }

    fun initialize(context: Context, folderId: UUID?) {
        watchJob = viewModelScope.launch {
            combine(
                folderRepository.getFoldersByParentIdFlow(folderId),
                dataFileRepository.getDataFileLinksByFolderIdFlow(folderId)
            ) { folders, files ->
                val tempFolders = folders.map { Item(it.id, it.name, ItemType.FOLDER, it.starred, false) }

                val tempFiles = files.map { Item(it.id, it.name, ItemType.FILE, it.starred, it.uploading, it.folderId) }

                tempFolders + tempFiles
            }.collectLatest {
                items.update { it }

                // TODO: I do not think this is a good way, try to find better
                getThumbnails(context)
            }
        }

        viewModelScope.launch {
            folderId?.let {
                selectedFolder = folderRepository.getFolderById(it)
            }
        }
    }

    private fun getThumbnails(context: Context) {
        thumbnails.update {
            AppState.customer!!.dataFilesLinks.value.mapNotNull { dataFileLink ->
                findThumbnailForDataFileLink(dataFileLink, context)?.let { thumbnail ->
                    dataFileLink.id to thumbnail
                }
            }.toMap()
        }
    }

    private fun findThumbnailForDataFileLink(dataFileLink: DataFileLinkEntity, context: Context): Bitmap? {
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
        COPY_ITEMS("copyItems"),
        MOVE_ITEMS("moveItems")
    }
}