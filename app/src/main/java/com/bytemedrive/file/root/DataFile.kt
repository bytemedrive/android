package com.bytemedrive.file.root

import java.util.UUID
import javax.crypto.SecretKey


data class DataFile(
    val id: UUID,
    val chunksIds: List<UUID>,
    val chunksViewIds: List<UUID>,
    val name: String,
    val sizeBytes: Long,
    val contentType: String,
    val secretKey: SecretKey,
    val thumbnails: MutableList<Thumbnail> = mutableListOf(),
    val checksum: String,
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
}
