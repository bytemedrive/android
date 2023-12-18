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

data class EventFileUploadCompleted(
    val dataFileId: UUID,
    val completedAt: ZonedDateTime,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFiles.update { dataFile -> dataFile.updateIf({ it.id == dataFileId }, { it.copy(uploadStatus = DataFile.UploadStatus.COMPLETED) }) }
    }
}
