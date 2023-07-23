package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileStarRemoved(val dataFileLinkId: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.replaceAll {
            if (it.id == dataFileLinkId) {
                it.copy(starred = false)
            } else it
        }
    }
}
