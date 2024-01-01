package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadCompleted(
    val dataFileId: UUID,
    val completedAt: ZonedDateTime,
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        var dataFile = database.dataFileDao().geDataFileById(dataFileId)
        database.dataFileDao().update(dataFile.ofUploadStatus(UploadStatus.COMPLETED))

        database.dataFileDao()
            .geDataFileLinksByDataFile(dataFileId)
            .forEach { database.dataFileDao().update(it.ofUploading(false)) }
    }
}
