package com.bytemedrive.folder

import androidx.room.Transaction
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderDeleted(val ids: List<UUID>) : Convertable {

    @Transaction
    override suspend fun convert(database: ByteMeDatabase) {
        database.dataFileDao().deleteByFolderIds(ids)
        database.folderDao().deleteByIds(ids)
    }
}
