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
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import java.io.File as FileJava

class FileViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val folderManager: FolderManager,
    context: Context,
) : ViewModel() {

    var files = MutableStateFlow(AppState.customer.value!!.files)
    var thumbnails = MutableStateFlow(mapOf<UUID, Bitmap?>())
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var fileAndFolderList = MutableStateFlow(listOf<Item>())

    val fileAndFolderSelected = MutableStateFlow(emptyList<Item>())

    val fileSelectionDialogOpened = MutableStateFlow(false)

    val action = MutableStateFlow<Action?>(null)

    init {
        viewModelScope.launch {
            getThumbnails(context)
        }
    }

    fun clickFileAndFolder(item: Item) {
        val anyFileSelected = fileAndFolderSelected.value.isNotEmpty()

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
        fileAndFolderSelected.value = if (fileAndFolderSelected.value.contains(item)) {
            fileAndFolderSelected.value - item
        } else {
            fileAndFolderSelected.value + item
        }
    }

    fun toggleAllItems(context: Context) {
        if (fileAndFolderSelected.value.size == fileAndFolderList.value.size) {
            fileAndFolderSelected.value = emptyList()
        } else {
            fileAndFolderSelected.value = fileAndFolderList.value
            Toast.makeText(context, "${fileAndFolderList.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearSelectedFileAndFolder() {
        fileAndFolderSelected.value = emptyList()
    }

    fun updateFileAndFolderList(folderId: String?, context: Context) = viewModelScope.launch {
        AppState.customer.collectLatest { customer ->
            val tempFolders = customer?.folders
                ?.filter { folder -> folder.parent == folderId?.let { UUID.fromString(it) } }
                ?.map { Item(it.id, it.name, ItemType.Folder, it.starred) }.orEmpty()

            val tempFiles = customer?.files
                ?.filter { file -> file.folderId == folderId?.let { UUID.fromString(it) } }
                ?.map { Item(it.id, it.name, ItemType.File, it.starred) }.orEmpty()

            files.value = customer?.files.orEmpty().toMutableList()
            folders.value = customer?.folders.orEmpty().toMutableList()
            fileAndFolderList.value = tempFolders + tempFiles
            getThumbnails(context)
        }
    }

    fun singleFile(id: String) = files.value.find { it.id == UUID.fromString(id) }

    fun singleFolder(id: String) = folders.value.find { it.id == UUID.fromString(id) }

    fun removeFile(id: UUID, onSuccess: () -> Unit) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            val file = files.value.find { it.id == id }
            val physicalFileRemovable = files.value.none { it.id == file?.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

            eventPublisher.publishEvent(EventFileDeleted(id))

            if (physicalFileRemovable) {
                fileRepository.remove(walletId, id)
            }

            onSuccess()
        }
    }

    fun removeFolder(id: UUID, onSuccess: () -> Unit) = viewModelScope.launch {
        AppState.customer.value?.wallet?.let { walletId ->
            folders.value.find { it.id == id }?.let { folder ->
                findAllFilesRecursively(id, folders.value, files.value).forEach { file ->
                    val physicalFileRemovable = files.value.none { it.id == file.id } // TODO: Fix - add DataFile class for physical file representation, File will be soft file

                    eventPublisher.publishEvent(EventFileDeleted(file.id))

                    if (physicalFileRemovable) {
                        fileRepository.remove(walletId, file.id)
                    }
                }
                (folderManager.findAllFoldersRecursively(id, folders.value) + folder).forEach {
                    eventPublisher.publishEvent(EventFolderDeleted(it.id))
                }
            }

            onSuccess()
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

    fun getFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(fileAndFolderList.value) }
        ).flow

    fun useSelectionScreenToMoveItems(id: UUID) = useSelectionScreenToMoveItems(listOf(id))

    fun useSelectionScreenToMoveItems(ids: List<UUID>) {
        action.value = Action(ids, Action.Type.MoveItems)
        fileSelectionDialogOpened.value = true
    }

    fun useSelectionScreenToCopyItem(id: UUID) {
        action.value = Action(listOf(id), Action.Type.CopyItem)
        fileSelectionDialogOpened.value = true
    }

    fun downloadFile(id: UUID, context: Context) =
        files.value.find { it.id == id }?.let { file ->
            viewModelScope.launch {
                val encryptedFile = FileJava.createTempFile("${file.name}-encrypted", null, context.cacheDir)


                encryptedFile.outputStream().use { outputStream ->
                    file.chunksViewIds
                        .forEach { chunkViewId ->
                            val response = fileRepository.download(chunkViewId)

                            response.bodyAsChannel().toInputStream().use { inputStream ->
                                inputStream.copyTo(outputStream, UploadViewModel.BUFFER_SIZE)
                            }
                        }
                }

                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                    put(MediaStore.Downloads.MIME_TYPE, file.contentType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                AesService.decryptWithKey(encryptedFile.inputStream(), contentResolver.openOutputStream(uri!!)!!, file.secretKey)
            }
        }

    private fun findAllFilesRecursively(folderId: UUID, allFolders: List<Folder>, allFiles: List<File>): List<File> {
        val filesToRemove = allFiles.filter { it.folderId == folderId }
        val subFolders = allFolders.filter { it.parent == folderId }

        val filesInSubFolders = mutableListOf<File>()
        for (subfolder in subFolders) {
            filesInSubFolders.addAll(findAllFilesRecursively(subfolder.id, allFolders, allFiles))
        }

        return filesToRemove + filesInSubFolders
    }

    private suspend fun getThumbnails(context: Context) {
        thumbnails.value = files.value.associate {
            it.id to it.thumbnails
                .find { thumbnail -> thumbnail.resolution == Resolution.P360 }
                ?.let { thumbnail ->
                    val encryptedFile = FileJava.createTempFile("${thumbnail.id}-encrypted", null, context.cacheDir)

                    encryptedFile.outputStream().use { outputStream ->
                        thumbnail.chunksViewIds.forEach { chunkViewId ->
                            val response = fileRepository.download(chunkViewId)

                            response.bodyAsChannel().toInputStream().use { inputStream ->
                                inputStream.copyTo(outputStream, UploadViewModel.BUFFER_SIZE)
                            }
                        }
                    }

                    val fileDecrypted = AesService.decryptWithKey(encryptedFile.readBytes(), thumbnail.secretKey)

                    BitmapFactory.decodeByteArray(fileDecrypted, 0, fileDecrypted.size)
                }
        }
    }
}

data class Action(val ids: List<UUID>, val type: Type) {
    enum class Type(type: String) {
        CopyItem("copyItem"),
        MoveItems("moveItems")
    }
}