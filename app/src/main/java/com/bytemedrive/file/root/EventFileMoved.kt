package com.bytemedrive.file.root

import android.util.Log
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFileMoved(val dataFileLinkId: UUID, val folderId: UUID?) : Convertable {
    private val TAG = EventFileMoved::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        val dataFileLinkEntity = dao.getDataFileLinkById(dataFileLinkId)

        if (dataFileLinkEntity == null) {
            Log.w(TAG, "Trying to get non existing data file link id=$dataFileLinkId")

            return
        }

        dao.update(dataFileLinkEntity.copy(folderId = folderId))
    }
}
