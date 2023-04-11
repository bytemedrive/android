package com.bytemedrive.file

import com.bytemedrive.file.File
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID


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
        customer.files.add(File(id, name, sizeBytes, contentType))
    }

}
