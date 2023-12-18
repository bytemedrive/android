package com.bytemedrive.file.root

import com.bytemedrive.file.root.bottomsheet.FileUploadChunk
import java.util.UUID
import javax.crypto.SecretKey


data class DataFile(
    val id: UUID,
    val chunks: List<FileUploadChunk>,
    val name: String,
    val sizeBytes: Long,
    val contentType: String,
    val secretKey: SecretKey,
    val thumbnails: List<Thumbnail> = emptyList(),
    val checksum: String,
    val uploadStatus: UploadStatus,
    val exifOrientation: Int?,
) {

    data class Thumbnail(
        val resolution: Resolution,
        val chunksIds: List<UUID>,
        val chunksViewIds: List<UUID>,
        val sizeBytes: Long,
        val contentType: String,
        val secretKey: SecretKey,
    )

    enum class UploadStatus{
        STARTED,
        COMPLETED
    }
}
