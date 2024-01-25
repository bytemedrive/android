package com.bytemedrive.datafile.control

import com.bytemedrive.datafile.entity.DataFile
import com.bytemedrive.datafile.entity.DataFileLink
import kotlinx.coroutines.flow.map
import java.util.UUID

class DataFileRepository(
    private val dataFileDao: DataFileDao
) {
    suspend fun getUsedStorage() = dataFileDao.getUsedStorage()

    fun getDataFileLinksByFolderIdFlow(folderId: UUID?) = dataFileDao.getDataFileLinksByFolderIdFlow(folderId).map { it.map(::DataFileLink) }

    suspend fun getDataFileById(dataFileId: UUID) = dataFileDao.getDataFileById(dataFileId)?.let(::DataFile)

    suspend fun getDataFileByChecksum(checksum: String) = dataFileDao.getDataFileByChecksum(checksum)?.let(::DataFile)

    suspend fun getDataFilesByIds(dataFileIds: List<UUID>) = dataFileDao.getDataFilesByIds(dataFileIds).map(::DataFile)

    suspend fun getDataFileLinkById(dataFileLinkId: UUID) = dataFileDao.getDataFileLinkById(dataFileLinkId)?.let(::DataFileLink)

    suspend fun getDataFileLinksByIds(dataFileLinkIds: List<UUID>) = dataFileDao.getDataFileLinksByIds(dataFileLinkIds).map(::DataFileLink)

    suspend fun getDataFileLinksByFolderId(folderId: UUID?) = dataFileDao.getDataFileLinksByFolderId(folderId).map(::DataFileLink)

    suspend fun getDataFileLinksByDataFileId(dataFileId: UUID) = dataFileDao.getDataFileLinksByDataFileId(dataFileId).map(::DataFileLink)

    suspend fun getAllDataFileLinks(starred: Boolean = false)  = dataFileDao.getAllDataFileLinks(starred).map(::DataFileLink)
}