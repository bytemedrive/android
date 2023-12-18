package com.bytemedrive.file.root

import com.bytemedrive.kotlin.updateIf
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventThumbnailStarted(
    val sourceDataFileId: UUID,
    val chunks: List<UploadChunk>,
    val sizeBytes: Long,
    val checksum: String,
    val contentType: String,
    val secretKeyBase64: String,
    val resolution: Resolution,
    val startedAt: ZonedDateTime,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val keyBytes = Base64.getDecoder().decode(secretKeyBase64)
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")

        customer.dataFiles.update { dataFiles ->
            dataFiles.updateIf(
                { it.id == sourceDataFileId },
                {
                    val thumbnail = DataFile.Thumbnail(resolution, chunks, sizeBytes, contentType, secretKey, DataFile.UploadStatus.STARTED)
                    it.copy(thumbnails = it.thumbnails + thumbnail)
                }
            )
        }
    }
}
