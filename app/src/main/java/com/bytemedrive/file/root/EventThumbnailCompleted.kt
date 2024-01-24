package com.bytemedrive.file.root

import android.util.Log
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.kotlin.updateIf
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventThumbnailCompleted(
    val sourceDataFileId: UUID,
    val resolution: Resolution,
    val completedAt: ZonedDateTime,
) : Convertable {
    private val TAG = EventThumbnailCompleted::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()
        val dataFileEntity = dao.getDataFileById(sourceDataFileId)

        if (dataFileEntity == null) {
            Log.w(TAG, "Trying to get non existing data file id=$sourceDataFileId")

            return
        }

        val thumbnails = dataFileEntity.thumbnails.updateIf({ it.resolution == resolution }, { it.copy(uploadStatus = UploadStatus.COMPLETED) })

        dao.update(dataFileEntity.copy(thumbnails = thumbnails))
    }
}
