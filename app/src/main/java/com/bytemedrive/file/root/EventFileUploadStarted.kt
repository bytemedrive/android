package com.bytemedrive.file.root

import com.bytemedrive.kotlin.updateIf
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventFileUploadStarted(
    val dataFileId: UUID,
    val chunks: List<UploadChunk>,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val startedAt: ZonedDateTime,
    val exifOrientation: Int?,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")

        customer.dataFiles.update { dataFile ->
            dataFile.updateIf(
                { it.id == dataFileId },
                { it.copy(
                    chunks = chunks,
                    checksum = checksum,
                    contentType = contentType,
                    secretKey = secretKey,
                    exifOrientation = exifOrientation,
                    uploadStatus = DataFile.UploadStatus.STARTED
                ) }
            )
        }
    }
}
