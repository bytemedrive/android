package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
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
    val folderId: UUID?,
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        val dataFileEntity = DataFileEntity(dataFileId, name, sizeBytes, UploadStatus.QUEUED)
        dao.add(dataFileEntity)

        val dataFileLinkEntity = DataFileLinkEntity(dataFileLinkId, dataFileId, name, folderId, true, false)
        dao.add(dataFileLinkEntity)
    }
}
