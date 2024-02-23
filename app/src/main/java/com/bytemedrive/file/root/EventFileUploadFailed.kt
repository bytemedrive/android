package com.bytemedrive.file.root

import android.util.Log
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadFailed(
    val dataFileId: UUID,
    val failedAt: ZonedDateTime,
) : Convertable {
    private val TAG = EventFileUploadFailed::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        dao.findDataFileById(dataFileId)?.let { dataFileEntity ->
            dao.update(dataFileEntity.ofUploadStatus(UploadStatus.FAILED))
            dao.getDataFileLinksByDataFileId(dataFileId).map { it.setUploadStatus(UploadStatus.FAILED) }.toTypedArray().let { dao.update(*it) }
        } ?: Log.w(TAG, "Trying to get non existing data file id=$dataFileId")
    }
}
