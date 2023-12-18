package com.bytemedrive.file.root

import java.util.UUID
import javax.crypto.SecretKey


data class DataFile(
    val id: UUID,
    val chunks: List<UploadChunk> = emptyList(),
    val name: String,
    val sizeBytes: Long,
    val contentType: String?,
    val secretKey: SecretKey?,
    val thumbnails: List<Thumbnail> = emptyList(),
    val checksum: String?,
    val uploadStatus: UploadStatus,
    val exifOrientation: Int?,
) {

    constructor(id: UUID, name: String, sizeBytes: Long, uploadStatus: UploadStatus) : this(id, emptyList(), name, sizeBytes, null, null, emptyList(), null, uploadStatus, null)

    data class Thumbnail(
        val resolution: Resolution,
        val chunks: List<UploadChunk>,
        val sizeBytes: Long,
        val contentType: String,
        val secretKey: SecretKey,
        val uploadStatus: UploadStatus,
    )

    enum class UploadStatus{
        QUEUED,
        STARTED,
        COMPLETED
    }
}
