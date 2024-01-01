package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.Thumbnail
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventThumbnailStarted(
    val sourceDataFileId: UUID,
    val chunks: List<UploadChunk>,
    val sizeBytes: Long,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val resolution: Resolution,
    val startedAt: ZonedDateTime,
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()
        val dataFile = dao.geDataFileById(sourceDataFileId)
        dao.update(dataFile.copy(thumbnails = dataFile.thumbnails + Thumbnail(resolution, chunks, sizeBytes, contentType, secretKeyBase64, UploadStatus.STARTED)))
    }
}
