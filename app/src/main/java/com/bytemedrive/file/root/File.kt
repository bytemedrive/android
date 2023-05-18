package com.bytemedrive.file.root

import java.util.UUID
import javax.crypto.SecretKey

data class File(
    val id: UUID,
    val chunkId: UUID,
    val name: String,
    val sizeBytes: Long,
    val contentType: String,
    val secretKey: SecretKey,
    val starred: Boolean = false,
    val folderId: UUID?,
    val thumbnails: MutableList<Thumbnail> = mutableListOf()
) {

    data class Thumbnail(val id: UUID, val chunkId: UUID, val resolution: Resolution, val secretKey: SecretKey)
}
