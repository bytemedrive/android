package com.bytemedrive.datafile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bytemedrive.file.root.UploadChunk
import java.util.UUID

data class DataFile(
    val id: UUID,
    val chunks: List<UploadChunk>,
    val name: String,
    val sizeBytes: Long,
    val contentType: String?,
    val secretKeyBase64: String?,
    val thumbnails: List<Thumbnail>,
    val checksum: String?,
    val uploadStatus: UploadStatus,
    val exifOrientation: Int?
) {
    constructor(dataFileEntity: DataFileEntity): this(
        dataFileEntity.id,
        dataFileEntity.chunks,
        dataFileEntity.name,
        dataFileEntity.sizeBytes,
        dataFileEntity.contentType,
        dataFileEntity.secretKeyBase64,
        dataFileEntity.thumbnails,
        dataFileEntity.checksum,
        dataFileEntity.uploadStatus,
        dataFileEntity.exifOrientation
    )
}