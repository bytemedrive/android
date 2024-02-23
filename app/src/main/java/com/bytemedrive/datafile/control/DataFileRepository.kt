package com.bytemedrive.datafile.control

import com.bytemedrive.datafile.entity.DataFile
import com.bytemedrive.datafile.entity.DataFileLink
import com.bytemedrive.datafile.entity.UploadStatus
import kotlinx.coroutines.flow.map
import java.util.UUID

class DataFileRepository(
    private val dataFileDao: DataFileDao
) {
    suspend fun getFileChunksToRemove(id: UUID) = dataFileDao.findDataFileById(id)?.let(::DataFile)?.let { dataFile ->
        dataFile.chunks.map { it.id } + dataFile.thumbnails.flatMap { it.chunks.map { uploadChunk -> uploadChunk.id } }
    } ?: emptyList()

    suspend fun getUsedStorage() = dataFileDao.getUsedStorage()

    fun getDataFileLinksByFolderIdFlow(folderId: UUID?) = dataFileDao.getDataFileLinksByFolderIdFlow(folderId).map { it.map(::DataFileLink) }

    suspend fun getDataFileById(dataFileId: UUID) = dataFileDao.findDataFileById(dataFileId)?.let(::DataFile)

    suspend fun getDataFileByChecksum(checksum: String?) = dataFileDao.getDataFileByChecksum(checksum)?.let(::DataFile)

    suspend fun getDataFilesByIds(dataFileIds: List<UUID>) = dataFileDao.getDataFilesByIds(dataFileIds).map(::DataFile)

    suspend fun getDataFilesByUploadStatus(uploadStatus: UploadStatus) = dataFileDao.getDataFilesByUploadStatus(uploadStatus).map(::DataFile)

    suspend fun getDataFileLinkById(dataFileLinkId: UUID) = dataFileDao.getDataFileLinkById(dataFileLinkId)?.let(::DataFileLink)

    suspend fun getDataFileLinksByIds(dataFileLinkIds: List<UUID>) = dataFileDao.getDataFileLinksByIds(dataFileLinkIds).map(::DataFileLink)

    suspend fun getDataFileLinksStarred(starred: Boolean = false) = dataFileDao.getDataFileLinksStarred(starred).map(::DataFileLink)

    suspend fun getDataFileLinksByFolderId(folderId: UUID?) = dataFileDao.getDataFileLinksByFolderId(folderId).map(::DataFileLink)

    suspend fun getDataFileLinksByDataFileId(dataFileId: UUID) = dataFileDao.getDataFileLinksByDataFileId(dataFileId).map(::DataFileLink)

    fun getDataFileLinksStarredFlow(starred: Boolean = false) = dataFileDao.getDataFileLinksStarredFlow(starred = starred).map { it.map(::DataFileLink) }

    suspend fun getAllDataFiles() = dataFileDao.getAllDataFiles().map(::DataFile)

    suspend fun getAllDataFileLinks() = dataFileDao.getAllDataFileLinks().map(::DataFileLink)

    fun getAllDataFileFlow()  = dataFileDao.getAllDataFileFlow().map { it.map(::DataFile) }
}