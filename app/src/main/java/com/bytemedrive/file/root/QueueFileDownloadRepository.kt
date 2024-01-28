package com.bytemedrive.file.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.UUID

class QueueFileDownloadRepository(private val fileDownloadDao: FileDownloadDao) {

    private val TAG = QueueFileDownloadRepository::class.qualifiedName

    suspend fun getFiles() = withContext(Dispatchers.IO) {
        fileDownloadDao.getAll().map { it.id }
    }

    suspend fun addFile(dataFileLinkId: UUID) = withContext(Dispatchers.IO) {
        fileDownloadDao.add(FileDownloadEntity(dataFileLinkId, ZonedDateTime.now()))
    }

    suspend fun deleteFile(id: UUID) = withContext(Dispatchers.IO) {
        fileDownloadDao.delete(id)
    }
}