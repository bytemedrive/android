package com.bytemedrive.file.root

import com.bytemedrive.database.FileUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.UUID

class QueueFileUploadRepository(private val fileUploadDao: FileUploadDao) {

    private val TAG = QueueFileUploadRepository::class.qualifiedName

    suspend fun getFiles() =
        fileUploadDao.getAll().map { FileUpload(it.id, it.name, it.path, it.folderId) }

    suspend fun addFile(fileUpload: FileUpload) =
        fileUploadDao.add(FileUploadEntity(fileUpload.id, fileUpload.name, fileUpload.path, fileUpload.folderId, ZonedDateTime.now()))

    suspend fun deleteFile(id: UUID) =
        fileUploadDao.delete(id)
}