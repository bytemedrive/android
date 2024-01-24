package com.bytemedrive.folder

import android.util.Log
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFolderCopied(val sourceFolderId: UUID, val targetFolderId: UUID, val parentId: UUID? = null) : Convertable {
    private val TAG = EventFolderCopied::class.qualifiedName

    override suspend fun convert(database: ByteMeDatabase) {
        val dao = database.folderDao()

        val sourceFolderEntity = dao.getById(sourceFolderId)

        if (sourceFolderEntity == null) {
            Log.w(TAG, "Trying to get non existing folder id=$sourceFolderId")

            return
        }

        dao.add(FolderEntity(targetFolderId, sourceFolderEntity.name, false, parentId))
    }
}
