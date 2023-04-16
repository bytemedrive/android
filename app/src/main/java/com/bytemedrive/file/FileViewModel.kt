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
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.crypto.SecretKey
import kotlin.math.min

class FileViewModel(private val fileRepository: FileRepository) : ViewModel() {

    private var _files = MutableStateFlow(AppState.customer.value!!.files)
    val files: StateFlow<List<File>> = _files

    private fun takePartOfList(pageIndex: Int = 0, pageSize: Int = 20): List<File> {
        val offset = pageIndex * pageSize
        val lastItemIndex = min(_files.value.size - 1, offset + pageSize - 1)

        return _files.value.slice(offset..lastItemIndex)
    }

    fun getFilesPages(): Flow<PagingData<File>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(this::takePartOfList) }
        ).flow.cachedIn(viewModelScope)

    fun downloadFile(file: File, context: Context) {
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