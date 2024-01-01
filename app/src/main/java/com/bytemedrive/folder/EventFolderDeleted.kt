package com.bytemedrive.folder

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderDeleted(val ids: List<UUID>) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        ids.forEach {
            database.dataFileDao().deleteByFolder(it)
            database.folderDao().delete(it)

        }
    }
}
