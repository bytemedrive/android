package com.bytemedrive.datafile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bytemedrive.file.root.UploadChunk
import java.util.UUID

@Entity(tableName = "data_file")
data class DataFileEntity(
    @PrimaryKey
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

    constructor(id: UUID, name: String, sizeBytes: Long, uploadStatus: UploadStatus) :
        this(id, emptyList(), name, sizeBytes, null, null, emptyList(), null, uploadStatus, null)

    fun ofUploadStatus(uploadStatus: UploadStatus): DataFileEntity {
        return DataFileEntity(id, chunks, name, sizeBytes, contentType, secretKeyBase64, thumbnails, checksum, uploadStatus, exifOrientation)
    }
}
