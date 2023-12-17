package com.bytemedrive.file.root

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bytemedrive.folder.Folder
import com.bytemedrive.store.AppState
import kotlin.math.min

class FilePagingSource2(
    private val queueFileUploadRepository: QueueFileUploadRepository,
    private val selectedFolder: Folder?
) : PagingSource<Int, Item>() {

//    private fun getData(pageIndex: Int = 0, pageSize: Int = 20): List<Item> {
//        val offset = pageIndex * pageSize
//        val lastItemIndex = min(data.size - 1, offset + pageSize - 1)
//
//        return data.slice(offset..lastItemIndex)
//    }

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {

        val pageIndex = params.key ?: 0

        val tempFolders = AppState.customer!!.folders.value
            .filter { folder -> folder.parent == selectedFolder?.id }
            .map { Item(it.id, it.name, ItemType.Folder, it.starred, false) }

        val tempFiles = AppState.customer!!.dataFilesLinks.value
            .filter { file -> file.folderId == selectedFolder?.id }
            .map { Item(it.id, it.name, ItemType.File, it.starred, false, it.folderId) }

        val tempFilesToUpload = queueFileUploadRepository.getFiles()
            .map { Item(it.id, it.name, ItemType.File, starred = false, uploading = true, folderId = it.folderId) }
            .filter { it.folderId?.equals(selectedFolder?.id) ?: true }

        val files = tempFilesToUpload + tempFolders + tempFiles

        val offset = pageIndex * PAGE_SIZE
        val lastItemIndex = min(files.size - 1, offset + PAGE_SIZE - 1)

        val data = files.slice(offset..lastItemIndex)

        val prevKey = if (pageIndex > 0) pageIndex - 1 else null
        val nextKey = if (data.isEmpty()) { null } else { pageIndex + (params.loadSize / PAGE_SIZE) }

        return LoadResult.Page(
            data = data,
            prevKey = prevKey,
            nextKey = nextKey,
        )
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}