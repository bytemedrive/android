package com.bytemedrive.file

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
data class EventFileUploaded(
    val id: UUID,
    val chunksIds: List<UUID>,
    val name: String,
    val sizeBytes: Long,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")

        // TODO: Temporary, fix when we work with chunks
        customer.files.add(File(id, chunksIds[0], name, sizeBytes, contentType, secretKey))
    }
}
