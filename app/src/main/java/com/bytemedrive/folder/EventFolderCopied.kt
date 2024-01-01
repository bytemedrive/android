package com.bytemedrive.folder

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderCopied(val sourceFolderId: UUID, val targetFolderId: UUID, val parentId: UUID? = null) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.folderDao()
        val sourceFolder = dao.getById(sourceFolderId)
        dao.add(FolderEntity(targetFolderId, sourceFolder.name, false, parentId))
    }
}
