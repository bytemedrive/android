package com.bytemedrive.file.root

import com.bytemedrive.kotlin.updateIf
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

data class EventThumbnailCompleted(
    val sourceDataFileId: UUID,
    val resolution: Resolution,
    val completedAt: ZonedDateTime,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFiles.update { dataFiles ->
            dataFiles.updateIf(
                { it.id == sourceDataFileId },
                { sourceDataFile ->
                    val thumbnails = sourceDataFile.thumbnails.updateIf({ it.resolution == resolution }, { it.copy(uploadStatus = DataFile.UploadStatus.COMPLETED) })

                    sourceDataFile.copy(thumbnails = thumbnails)
                }
            )
        }
    }
}
