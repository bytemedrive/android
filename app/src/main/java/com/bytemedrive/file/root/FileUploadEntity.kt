package com.bytemedrive.file.root

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "file_upload")
data class FileUploadEntity(

    @PrimaryKey
    val id: UUID,
    val name: String,
    val path: String,
    val folderId: UUID?,
    val uploadedAt: ZonedDateTime
)
