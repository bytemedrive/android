package com.bytemedrive.file.root

import com.bytemedrive.file.root.bottomsheet.FileUploadChunk
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventFileUploadStarted(
    val dataFileId: UUID,
    val chunks: List<FileUploadChunk>,
    val name: String,
    val sizeBytes: Long,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val dataFileLinkId: UUID,
    val startedAt: ZonedDateTime,
    val folderId: UUID?,
    val exifOrientation: Int?,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
        val dataFile = DataFile(dataFileId, chunks, name, sizeBytes, contentType, secretKey, mutableListOf(), checksum, DataFile.UploadStatus.STARTED, exifOrientation)
        val dataFileLink = DataFileLink(dataFileLinkId, dataFileId, name, folderId)

        customer.dataFiles.update { it + dataFile }
        customer.dataFilesLinks.update { it + dataFileLink }
    }
}
