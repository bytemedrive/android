package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFileDeleted(val dataFileLinkIds: List<UUID>) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()

        dao.deleteByIds(dataFileLinkIds)
    }
}
