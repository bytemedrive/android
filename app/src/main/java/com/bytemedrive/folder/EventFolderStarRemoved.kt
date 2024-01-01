package com.bytemedrive.folder

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderStarRemoved(val id: UUID) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.folderDao()
        val folder = dao.getById(id)
        dao.update(folder.copy(starred = false))
    }
}
