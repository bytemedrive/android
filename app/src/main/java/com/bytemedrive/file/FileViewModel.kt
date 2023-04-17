package com.bytemedrive.file

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.min

class FileViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher
) : ViewModel() {

    var files = MutableStateFlow(AppState.customer.value!!.files)
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var list = MutableStateFlow(listOf<Item>())

    init {
        viewModelScope.launch {
            combine(files, folders) { files, folders ->
                folders.map { Item(it.id, it.name, ItemType.Folder) } + files.map { Item(it.id, it.name, ItemType.File) }
            }.collect { value ->
                list.value = value
            }
        }
    }

    fun singleFile(id: String) = files.value.find { it.id.toString() == id }

    fun singleFolder(id: String) = folders.value.find { it.id.toString() == id }

    fun removeFile(id: UUID, onSuccess: () -> Unit) = viewModelScope.launch {
        eventPublisher.publishEvent(EventFileDeleted(id))
        fileRepository.remove(id.toString())
        onSuccess()
    }

    fun removeFolder(id: UUID, onSuccess: () -> Unit) = viewModelScope.launch {
        eventPublisher.publishEvent(EventFolderDeleted(id))
        onSuccess()
    }

    private fun takePartOfList(pageIndex: Int = 0, pageSize: Int = 20): List<Item> {
        val offset = pageIndex * pageSize
        val lastItemIndex = min(list.value.size - 1, offset + pageSize - 1)

        return list.value.slice(offset..lastItemIndex)
    }

    fun getFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(this::takePartOfList) }
        ).flow.cachedIn(viewModelScope)

    fun downloadFile(id: UUID, context: Context) =
        files.value.find { it.id == id }?.let { file ->
            viewModelScope.launch {
                val bytes = fileRepository.download(file.chunkId.toString())
                val fileDecrypted = AesService.decryptWithKey(bytes, file.secretKey)

                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, file.contentType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                contentResolver.openOutputStream(uri!!).use { it?.write(fileDecrypted) }
            }
        }
}