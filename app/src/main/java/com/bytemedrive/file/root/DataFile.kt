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
) {

    data class Thumbnail(val thumbnailDataFileId: UUID, val resolution: Resolution)
}
