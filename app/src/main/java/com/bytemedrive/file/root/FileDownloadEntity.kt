package com.bytemedrive.file.root

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "file_download")
data class FileDownloadEntity(

    @PrimaryKey
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "queued_at") val queuedAt: ZonedDateTime
)
