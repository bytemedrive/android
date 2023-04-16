package com.bytemedrive.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.min

class FileViewModel() : ViewModel() {

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
}