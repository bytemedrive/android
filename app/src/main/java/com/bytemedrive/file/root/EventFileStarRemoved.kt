package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFileStarRemoved(val dataFileLinkId: UUID) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()
        val dataFileLink = dao.geDataFileLinkById(dataFileLinkId)
        dao.update(dataFileLink.copy(starred = false))
    }
}
