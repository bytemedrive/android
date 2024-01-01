package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFileDeleted(val dataFileLinkIds: List<UUID>) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()
        dataFileLinkIds
            .map { dao.geDataFileLinkById(it) }
            .forEach {
                dao.delete(it)
                if (dao.geDataFileLinksByDataFile(it.dataFileId).isEmpty()) {
                    dao.delete(dao.geDataFileById(it.dataFileId))
                }
            }
    }
}
