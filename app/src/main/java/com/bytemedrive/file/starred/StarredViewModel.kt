package com.bytemedrive.file.starred

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bytemedrive.file.root.FilePagingSource
import com.bytemedrive.file.root.Item
import com.bytemedrive.file.root.ItemType
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StarredViewModel: ViewModel() {
    var files = MutableStateFlow(AppState.customer.value!!.files)
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var list = MutableStateFlow(listOf<Item>())
    var starred = MutableStateFlow(listOf<Item>())

    init {
        viewModelScope.launch {
            combine(files, folders) { files, folders ->
                folders
                    .filter { it.starred }
                    .map { Item(it.id, it.name, ItemType.Folder, it.starred) } +
                    files
                        .filter { it.starred  }
                        .map { Item(it.id, it.name, ItemType.File, it.starred) }
            }.collect { value ->
                starred.value = value
            }
        }
    }

    fun getStarredFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(starred.value) }
        ).flow.cachedIn(viewModelScope)
}