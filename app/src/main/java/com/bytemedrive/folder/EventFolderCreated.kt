package com.bytemedrive.folder

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderCreated(
    val id: UUID,
    val name: String,
    val starred: Boolean = false,
    val parent: UUID? = null
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        database.folderDao().add(FolderEntity(id, name, starred, parent))
    }
}
