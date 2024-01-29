package com.bytemedrive.file.shared.control

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.bytemedrive.file.shared.entity.FileListItem
import kotlinx.coroutines.flow.map
import java.util.UUID

class FileListItemRepository(
    private val itemDao: FileListItemDao
) {

    fun getAllPaged(folderId: UUID? = null) = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { itemDao.getAllPaged(folderId) }
    ).flow.map { it.map { item -> FileListItem(item) } }

    fun getAllStaredPaged(folderId: UUID? = null) = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { itemDao.getAllStarredPaged(folderId = folderId) }
    ).flow.map { it.map { item -> FileListItem(item) } }
}