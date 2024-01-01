package com.bytemedrive.datafile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bytemedrive.file.root.UploadChunk
import java.util.UUID

@Entity(tableName = "data_file")
data class DataFileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "chunks") val chunks: List<UploadChunk>,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "content_type") val contentType: String?,
    @ColumnInfo(name = "secret_key_base64") val secretKeyBase64: String?,
    @ColumnInfo(name = "thumbnails") val thumbnails: List<Thumbnail>,
    @ColumnInfo(name = "checksum") val checksum: String?,
    @ColumnInfo(name = "upload_status") val uploadStatus: UploadStatus,
    @ColumnInfo(name = "exif_orientation") val exifOrientation: Int?
) {

    constructor(id: UUID, name: String, sizeBytes: Long, uploadStatus: UploadStatus) :
        this(id, emptyList(), name, sizeBytes, null, null, emptyList(), null, uploadStatus, null)

    fun ofUploadStatus(uploadStatus: UploadStatus): DataFileEntity {
        return DataFileEntity(id, chunks, name, sizeBytes, contentType, secretKeyBase64, thumbnails, checksum, uploadStatus, exifOrientation)
    }
}
