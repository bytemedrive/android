package com.bytemedrive.folder

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import java.util.UUID

class FolderRepository(
    private val folderDao: FolderDao
) {

    suspend fun getAllFolders() = folderDao.getAll().map(::Folder)

    fun getAllFoldersFlow(starred: Boolean = false) = folderDao.getAllFlow(starred = starred).map { it.map(::Folder) }

    suspend fun getFolderById(id: UUID) = folderDao.getById(id)?.let(::Folder)

    suspend fun getFoldersByIds(id: List<UUID>) = folderDao.getByIds(id).map(::Folder)

    suspend fun getFoldersByParentId(id: UUID?) = folderDao.getByParentId(id).map(::Folder)

    fun getFoldersByParentIdFlow(id: UUID?) = folderDao.getByParentIdFlow(id).map { it.map(::Folder) }
}