package com.bytemedrive.file.shared.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bytemedrive.datafile.entity.UploadStatus
import java.util.UUID

@Entity(tableName = "item")
data class FileListItemEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val type: ItemType,
    val starred: Boolean,
    val uploadStatus: UploadStatus,
    val folderId: UUID? = null
)

