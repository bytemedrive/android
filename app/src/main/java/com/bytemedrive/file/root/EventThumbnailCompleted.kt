package com.bytemedrive.file.root

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

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()
        val dataFile = dao.geDataFileById(sourceDataFileId)
        val thumbnails = dataFile.thumbnails.updateIf({ it.resolution == resolution }, { it.copy(uploadStatus = UploadStatus.COMPLETED) })
        dao.update(dataFile.copy(thumbnails = thumbnails))
    }
}
