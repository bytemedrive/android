package com.bytemedrive.datafile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

data class DataFileLink(
    val id: UUID,
    val dataFileId: UUID,
    val name: String,
    val folderId: UUID?,
    val uploadStatus: UploadStatus,
    val starred: Boolean,
) {
    constructor(dataFileLinkEntity: DataFileLinkEntity): this(
        dataFileLinkEntity.id,
        dataFileLinkEntity.dataFileId,
        dataFileLinkEntity.name,
        dataFileLinkEntity.folderId,
        dataFileLinkEntity.uploadStatus,
        dataFileLinkEntity.starred,
    )
}
