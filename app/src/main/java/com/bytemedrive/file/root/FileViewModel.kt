package com.bytemedrive.file.root

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class FileViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val folderManager: FolderManager,
    private val fileManager: FileManager,
    context: Context,
) : ViewModel() {

    var dataFiles = MutableStateFlow(AppState.customer.value!!.dataFiles)
    var dataFileLinks = MutableStateFlow(AppState.customer.value!!.dataFilesLinks)
    var thumbnails = MutableStateFlow(mapOf<UUID, Bitmap?>())
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var items = MutableStateFlow(listOf<Item>())

    val itemsSelected = MutableStateFlow(emptyList<Item>())

    val fileSelectionDialogOpened = MutableStateFlow(false)

    val action = MutableStateFlow<Action?>(null)

    init {
        viewModelScope.launch {
            getThumbnails(context)
        }
    }

    fun clickFileAndFolder(item: Item) {
        val anyFileSelected = itemsSelected.value.isNotEmpty()

        if (anyFileSelected) {
            longClickFileAndFolder(item)
        } else {
            when (item.type) {
                ItemType.Folder -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))
                ItemType.File -> null // TODO: Add some action
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

    suspend fun updateItems(folderId: String?, context: Context) {
        AppState.customer.collectLatest { customer ->
            val tempFolders = customer?.folders
                ?.filter { folder -> folder.parent == folderId?.let { UUID.fromString(it) } }
                ?.map { Item(it.id, it.name, ItemType.Folder, it.starred) }.orEmpty()

            val tempFiles = customer?.dataFilesLinks
                ?.filter { file -> file.folderId == folderId?.let { UUID.fromString(it) } }
                ?.map { Item(it.id, it.name, ItemType.File, it.starred) }.orEmpty()

            dataFileLinks.value = customer?.dataFilesLinks.orEmpty().toMutableList()
            folders.value = customer?.folders.orEmpty().toMutableList()
            items.value = tempFolders + tempFiles
            getThumbnails(context)
        }
    }

    fun singleFile(id: String) = dataFileLinks.value.find { it.id == UUID.fromString(id) }

    fun singleFolder(id: String) = folders.value.find { it.id == UUID.fromString(id) }

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
                    val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

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

    fun removeFile(id: UUID, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            val file = dataFileLinks.value.find { it.id == id }
            val physicalFileRemovable = dataFileLinks.value.none { it.id == file?.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

            eventPublisher.publishEvent(EventFileDeleted(id))

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
                    val physicalFileRemovable = dataFileLinks.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                    eventPublisher.publishEvent(EventFileDeleted(file.id))

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }
                }
                (folderManager.findAllFoldersRecursively(id, folders.value) + folder).forEach {
                    eventPublisher.publishEvent(EventFolderDeleted(it.id))
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

    fun downloadFile(id: UUID, context: Context) =
        dataFileLinks.value.find { it.id == id }?.let { dataFileLink ->
            dataFiles.value.find { it.id == dataFileLink.dataFileId }?.let { dataFile ->
                viewModelScope.launch {
                    val contentResolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, dataFileLink.name)
                        put(MediaStore.Downloads.MIME_TYPE, dataFile.contentType)
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    val encryptedFile = fileManager.rebuildFile(dataFile.chunksViewIds, "${dataFileLink.id}-encrypted", context.cacheDir)

                    AesService.decryptWithKey(encryptedFile.inputStream(), contentResolver.openOutputStream(uri!!)!!, dataFile.secretKey)
                }
            }
        }

    private suspend fun getThumbnails(context: Context) {
        /*thumbnails.value = dataFileLinks.value.associate { dataFileLink ->
            dataFileLink.id to dataFileLink.thumbnails
                .find { thumbnail -> thumbnail.resolution == Resolution.P360 }
                ?.let { thumbnail ->
                    val encryptedFile = fileManager.rebuildFile(thumbnail.chunksViewIds, "${thumbnail.id}-encrypted", context.cacheDir)
                    val fileDecrypted = AesService.decryptWithKey(encryptedFile.readBytes(), thumbnail.secretKey)

                    BitmapFactory.decodeByteArray(fileDecrypted, 0, fileDecrypted.size)
                }
        }*/
    }
}

data class Action(val ids: List<UUID>, val type: Type) {
    enum class Type(type: String) {
        CopyItems("copyItems"),
        MoveItems("moveItems")
    }
}