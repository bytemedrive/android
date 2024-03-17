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

    suspend fun getAll(folderId: UUID? = null) = itemDao.getAll(folderId = folderId).map(::FileListItem)

    fun getAllStarredPaged( starred: Boolean = false) = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { itemDao.getAllStarredPaged(starred) }
    ).flow.map { it.map { item -> FileListItem(item) } }

    fun getAllStarredFlow(starred: Boolean = false) = itemDao.getAllStarredFlow(starred).map { it.map(::FileListItem) }
}