package com.bytemedrive.file.root

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "file_download")
data class FileDownloadEntity(

    @PrimaryKey
    val id: UUID,
    val queuedAt: ZonedDateTime
)
