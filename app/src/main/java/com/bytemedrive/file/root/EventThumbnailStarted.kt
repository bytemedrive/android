package com.bytemedrive.file.root

import android.util.Log
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
    private val TAG = EventThumbnailStarted::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        val dataFileEntity = dao.findDataFileById(sourceDataFileId)

        if (dataFileEntity == null) {
            Log.w(TAG, "Trying to get non existing data file id=$sourceDataFileId")

            return
        }

        val thumbnails = dataFileEntity.thumbnails + Thumbnail(resolution, chunks, sizeBytes, contentType, secretKeyBase64, UploadStatus.STARTED)

        dao.update(dataFileEntity.copy(thumbnails = thumbnails))
    }
}
