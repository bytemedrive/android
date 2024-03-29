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

        dao.findDataFileById(dataFileId)?.let { dataFileEntity ->
            dao.update(dataFileEntity.ofUploadStatus(UploadStatus.COMPLETED))
            dao.getDataFileLinksByDataFileId(dataFileId).map { it.setUploadStatus(UploadStatus.COMPLETED) }.toTypedArray().let { dao.update(*it) }
        } ?: Log.w(TAG, "Trying to get non existing data file id=$dataFileId")
    }
}
