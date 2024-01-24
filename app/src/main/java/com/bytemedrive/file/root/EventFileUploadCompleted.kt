package com.bytemedrive.file.root

import android.util.Log
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadCompleted(
    val dataFileId: UUID,
    val completedAt: ZonedDateTime,
) : Convertable {
    private val TAG = EventFileUploadCompleted::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        val dataFileEntity = dao.getDataFileById(dataFileId)

        if (dataFileEntity == null) {
            Log.w(TAG, "Trying to get non existing data file id=$dataFileId")

            return
        }

        dao.update(dataFileEntity.ofUploadStatus(UploadStatus.COMPLETED))

        val dataFileLinkEntities = dao.getDataFileLinksByDataFile(dataFileId).map { it.ofUploading(false) }.toTypedArray()

        dao.update(*dataFileLinkEntities)
    }
}
