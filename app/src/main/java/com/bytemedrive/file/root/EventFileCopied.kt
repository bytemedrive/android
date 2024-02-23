package com.bytemedrive.file.root

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventFileCopied(val dataFileId: UUID, val dataFileLinkId: UUID, val folderId: UUID? = null, val name: String) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) =
        database.dataFileDao().add(DataFileLinkEntity(dataFileLinkId, dataFileId, name, folderId, UploadStatus.COMPLETED, false))
}
