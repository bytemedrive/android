package com.bytemedrive.folder

import android.util.Log
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderStarRemoved(val id: UUID) : Convertable {
    private val TAG = EventFolderStarRemoved::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.folderDao()

        val folder = dao.getById(id)

        if (folder == null) {
            Log.w(TAG, "Trying to get non existing folder id=$id")

            return
        }

        dao.update(folder.copy(starred = false))
    }
}
