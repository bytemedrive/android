package com.bytemedrive.file.starred

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bytemedrive.file.root.FilePagingSource
import com.bytemedrive.file.root.Item
import com.bytemedrive.file.root.ItemType
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StarredViewModel(
    private val appNavigator: AppNavigator,
) : ViewModel() {

    var files = MutableStateFlow(AppState.customer.value!!.files)
    var folders = MutableStateFlow(AppState.customer.value!!.folders)

    var list = MutableStateFlow(listOf<Item>())
    var starred = MutableStateFlow(listOf<Item>())

    val fileAndFolderSelected = MutableStateFlow(emptyList<Item>())

    init {
        viewModelScope.launch {
            AppState.customer.collectLatest { customer ->
                val folders = customer?.folders?.filter { it.starred }?.map { Item(it.id, it.name, ItemType.Folder, it.starred) }.orEmpty().toMutableList()
                val files = customer?.files?.filter { it.starred }?.map { Item(it.id, it.name, ItemType.File, it.starred) }.orEmpty().toMutableList()

                starred.value = folders + files
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
        ).flow

    fun toggleAllItems(context: Context) {
        if (fileAndFolderSelected.value.size == starred.value.size) {
            fileAndFolderSelected.value = emptyList()
        } else {
            fileAndFolderSelected.value = starred.value
            Toast.makeText(context, "${starred.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }
}