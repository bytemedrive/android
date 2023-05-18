package com.bytemedrive.file.root

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.folder.Folder
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class FileViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
) : ViewModel() {

    var files = MutableStateFlow(AppState.customer.value!!.files)
    var thumbnails = MutableStateFlow(mapOf<UUID, Bitmap?>())
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var fileAndFolderList = MutableStateFlow(listOf<Item>())

    val fileAndFolderSelected = MutableStateFlow(emptyList<Item>())

    init {
        viewModelScope.launch {
            thumbnails.value = files.value.associate {
                it.id to it.thumbnails
                    .find { thumbnail -> thumbnail.resolution == Resolution.P360 }
                    ?.let { thumbnail ->
                        val bytes = fileRepository.download(thumbnail.chunkId.toString())
                        val fileDecrypted = AesService.decryptWithKey(bytes, thumbnail.secretKey)

                        BitmapFactory.decodeByteArray(fileDecrypted, 0, fileDecrypted.size)
                    }
            }
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

    fun clearSelectedFileAndFolder() {
        fileAndFolderSelected.value = emptyList()
    }

    fun updateFileAndFolderList(folderId: String?) = viewModelScope.launch {
        combine(files, folders) { files, folders ->
            folders
                .filter { folder -> folder.parent == folderId?.let { UUID.fromString(it) } }
                .map { Item(it.id, it.name, ItemType.Folder, it.starred) } +
                files
                    .filter { file -> file.folderId == folderId?.let { UUID.fromString(it) } }
                    .map { Item(it.id, it.name, ItemType.File, it.starred) }
        }.collect { value ->
            fileAndFolderList.value = value
        }
    }

    fun singleFile(id: String) = files.value.find { it.id == UUID.fromString(id) }

    fun singleFolder(id: String) = folders.value.find { it.id == UUID.fromString(id) }

    fun removeFile(id: UUID, onSuccess: () -> Unit) = viewModelScope.launch {
        eventPublisher.publishEvent(EventFileDeleted(id))
        fileRepository.remove(id.toString())
        onSuccess()
    }

    fun removeFolder(id: UUID, onSuccess: () -> Unit) = viewModelScope.launch {
        folders.value.find { it.id == id }?.let { folder ->
            findFilesToRemove(id, folders.value, files.value).forEach { file ->
                eventPublisher.publishEvent(EventFileDeleted(file.id))
                fileRepository.remove(file.id.toString())
            }
            (findFoldersToRemove(id, folders.value) + folder).forEach {
                eventPublisher.publishEvent(EventFolderDeleted(it.id))
            }
        }

        onSuccess()
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

    private fun findFilesToRemove(folderId: UUID, allFolders: List<Folder>, allFiles: List<File>): List<File> {
        val filesToRemove = allFiles.filter { it.folderId == folderId }
        val subFolders = allFolders.filter { it.parent == folderId }

        val filesInSubFolders = mutableListOf<File>()
        for (subfolder in subFolders) {
            filesInSubFolders.addAll(findFilesToRemove(subfolder.id, allFolders, allFiles))
        }

        return filesToRemove + filesInSubFolders
    }

    private fun findFoldersToRemove(folderId: UUID, allFolders: List<Folder>): List<Folder> {
        val objectsInFolder = allFolders.filter { it.parent == folderId }

        val objectsInSubFolders = mutableListOf<Folder>()
        for (subfolder in objectsInFolder) {
            objectsInSubFolders.addAll(findFoldersToRemove(subfolder.id, allFolders))
        }

        return objectsInFolder + objectsInSubFolders
    }

    fun getFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(fileAndFolderList.value) }
        ).flow.cachedIn(viewModelScope)

    fun downloadFile(id: UUID, context: Context) =
        files.value.find { it.id == id }?.let { file ->
            viewModelScope.launch {
                val bytes = fileRepository.download(file.chunkId.toString())
                val fileDecrypted = AesService.decryptWithKey(bytes, file.secretKey)

                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                    put(MediaStore.Downloads.MIME_TYPE, file.contentType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                contentResolver.openOutputStream(uri!!).use { it?.write(fileDecrypted) }
            }
        }
}