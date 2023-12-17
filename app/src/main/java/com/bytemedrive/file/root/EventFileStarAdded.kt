package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFileStarAdded(val dataFileLinkId: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.update { dataFileLink ->
            dataFileLink.map {
                if (it.id == dataFileLinkId) {
                    it.copy(starred = true)
                } else it
            }
        }
    }
}
