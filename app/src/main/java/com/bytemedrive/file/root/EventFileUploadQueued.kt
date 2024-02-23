package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.database.FileUpload
import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadQueued(
    val dataFileId: UUID,
    val name: String,
    val sizeBytes: Long,
    val dataFileLinkId: UUID,
    val queuedAt: ZonedDateTime,
    val filePath: String,
    val folderId: UUID?,
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        val dataFileEntity = DataFileEntity(dataFileId, name, sizeBytes, UploadStatus.QUEUED)
        dao.add(dataFileEntity)

        val dataFileLinkEntity = DataFileLinkEntity(dataFileLinkId, dataFileId, name, folderId, UploadStatus.QUEUED, false)
        dao.add(dataFileLinkEntity)

        database.fileUploadDao().add(FileUploadEntity(dataFileId, name, filePath, folderId, queuedAt))
    }
}
