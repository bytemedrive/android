package com.bytemedrive.file.root

import com.bytemedrive.kotlin.updateIf
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadCompleted(
    val dataFileId: UUID,
    val completedAt: ZonedDateTime,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFiles.update { dataFile -> dataFile.updateIf({ it.id == dataFileId }, { it.copy(uploadStatus = DataFile.UploadStatus.COMPLETED) }) }
        customer.dataFilesLinks.update { dataFileLinks ->
            dataFileLinks.updateIf(
                { it.dataFileId == dataFileId },
                { it.copy(uploading = false) }
            )
        }
    }
}
