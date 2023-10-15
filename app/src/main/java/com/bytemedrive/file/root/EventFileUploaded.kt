package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventFileUploaded(
    val dataFileId: UUID,
    val chunksIds: List<UUID>,
    val chunksViewIds: List<UUID>,
    val name: String,
    val sizeBytes: Long,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val dataFileLinkId: UUID,
    val folderId: UUID?,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
        val dataFile = DataFile(dataFileId, chunksIds, chunksViewIds, name, sizeBytes, contentType, secretKey, mutableListOf(), checksum)

        customer.dataFiles.add(dataFile)
        dataFileLinkId.let { customer.dataFilesLinks.add(DataFileLink(it, dataFileId, name, folderId)) }
    }
}
