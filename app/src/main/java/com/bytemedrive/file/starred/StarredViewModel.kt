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
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StarredViewModel(
    private val appNavigator: AppNavigator
) : ViewModel() {

    var files = MutableStateFlow(AppState.customer.value!!.files)
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var list = MutableStateFlow(listOf<Item>())
    var starred = MutableStateFlow(listOf<Item>())

    val fileAndFolderSelected = MutableStateFlow(emptyList<Item>())

    init {
        viewModelScope.launch {
            combine(files, folders) { files, folders ->
                folders
                    .filter { it.starred }
                    .map { Item(it.id, it.name, ItemType.Folder, it.starred) } +
                    files
                        .filter { it.starred }
                        .map { Item(it.id, it.name, ItemType.File, it.starred) }
            }.collect { value ->
                starred.value = value
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

    fun getStarredFilesPages(): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FilePagingSource(starred.value) }
        ).flow.cachedIn(viewModelScope)
}