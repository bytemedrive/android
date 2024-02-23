package com.bytemedrive.file.root

import android.util.Log
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
    private val TAG = EventFileUploadStarted::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        val dataFileEntity = dao.findDataFileById(dataFileId)

        if (dataFileEntity == null) {
            Log.w(TAG, "Trying to get non existing data file id=$dataFileId")

            return
        }

        dao.update(
            dataFileEntity.copy(
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
