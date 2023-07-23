package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileDeleted(val dataFileLinkId: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.removeIf { it.id == dataFileLinkId }
    }
}
