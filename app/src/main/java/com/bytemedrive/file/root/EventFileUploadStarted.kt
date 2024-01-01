package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadStarted(
    val dataFileId: UUID,
    val chunks: List<UploadChunk>,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val startedAt: ZonedDateTime,
    val exifOrientation: Int?,
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dataFile = database.dataFileDao().geDataFileById(dataFileId)
        database.dataFileDao().update(
            dataFile.copy(
                chunks = chunks,
                contentType = contentType,
                secretKeyBase64 = secretKeyBase64,
                checksum = checksum,
                uploadStatus = UploadStatus.STARTED,
                exifOrientation = exifOrientation
            )
        )
    }
}
