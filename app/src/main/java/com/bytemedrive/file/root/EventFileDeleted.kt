package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFileDeleted(val dataFileLinkIds: List<UUID>) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        dataFileLinkIds.forEach { dataFileLinkIdToRemove ->
            customer.dataFilesLinks.value
                .find { dataFileLinkIdToRemove == it.id }
                ?.let { dataFileLink ->
                    customer.dataFilesLinks.update { it - dataFileLink }

                    val remainingLinksCount = customer.dataFilesLinks.value.filter { it.dataFileId == dataFileLink.dataFileId }.size

                    if (remainingLinksCount == 0) {
                        customer.dataFiles.update { dataFiles -> dataFiles.filterNot { it.id == dataFileLink.dataFileId } }
                    }
                }
        }
    }
}
