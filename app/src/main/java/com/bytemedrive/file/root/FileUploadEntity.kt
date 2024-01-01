package com.bytemedrive.file.root

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "file_upload")
data class FileUploadEntity(

    @PrimaryKey
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "folder_id") val folderId: UUID?,
    @ColumnInfo(name = "queued_at") val queuedAt: ZonedDateTime
)
