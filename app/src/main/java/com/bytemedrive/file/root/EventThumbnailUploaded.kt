package com.bytemedrive.file

import com.bytemedrive.file.root.File
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventThumbnailUploaded(
    val id: UUID,
    val chunksIds: List<UUID>,
    val resolution: Resolution,
    val fileId: UUID,
    val contentType: String,
    val secretKeyBase64: String
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.files = customer.files.map {
            val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
            val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")

            it.thumbnails.add(File.Thumbnail(id, chunksIds[0], resolution, secretKey))

            it
        }.toMutableList()
    }
}
