package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFileDeleted(val dataFileLinkIds: List<UUID>) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.dataFileDao()
        val dataFileLinks = dao.getDataFileLinksByIds(dataFileLinkIds)

        dao.deleteDataFileLinksByIds(dataFileLinkIds)

        val removableFileIds = dataFileLinks.mapNotNull { dataFileLink ->
            val dataFileRemovable = dao.getDataFileLinksByDataFileId(dataFileLink.dataFileId).isEmpty()

            if (dataFileRemovable) dataFileLink.dataFileId else null
        }

        if (removableFileIds.isNotEmpty()) {
            dao.deleteDataFilesByIds(removableFileIds)
        }
    }
}
