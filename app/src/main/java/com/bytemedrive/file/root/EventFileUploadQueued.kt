package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.UUID

data class EventFileUploadQueued(
    val dataFileId: UUID,
    val name: String,
    val sizeBytes: Long,
    val dataFileLinkId: UUID,
    val queuedAt: ZonedDateTime,
    val folderId: UUID?,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        val dataFile = DataFile(dataFileId, name, sizeBytes, DataFile.UploadStatus.QUEUED)
        val dataFileLink = DataFileLink(dataFileLinkId, dataFileId, name, folderId)

        customer.dataFiles.update { it + dataFile }
        customer.dataFilesLinks.update { it + dataFileLink }
    }
}
