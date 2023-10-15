package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileDeleted(val dataFileLinkIds: List<UUID>) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        dataFileLinkIds.forEach { dataFileLinkIdToRemove ->
            customer.dataFilesLinks
                .find { dataFileLinkIdToRemove == it.id }
                ?.let { dataFileLink ->
                    customer.dataFilesLinks.remove(dataFileLink)

                    val remainingLinksCount = customer.dataFilesLinks.filter { it.dataFileId == dataFileLink.dataFileId }.size

                    if (remainingLinksCount == 0) {
                        customer.dataFiles.removeIf { it.id == dataFileLink.dataFileId }
                    }
                }
        }
    }
}
