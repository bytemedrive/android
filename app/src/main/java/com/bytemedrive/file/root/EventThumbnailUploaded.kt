package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventThumbnailUploaded(
    val sourceDataFileId: UUID,
    val chunksIds: List<UUID>,
    val chunksViewIds: List<UUID>,
    val sizeBytes: Long,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val resolution: Resolution,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")

        customer.dataFiles.find { it.id == sourceDataFileId }?.thumbnails?.add(DataFile.Thumbnail(resolution, chunksIds, chunksViewIds, sizeBytes, contentType, secretKey))
    }
}
