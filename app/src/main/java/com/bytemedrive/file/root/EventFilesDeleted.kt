package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFilesDeleted(val dataFileLinkIds: List<UUID>) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.removeIf { dataFileLinkIds.contains(it.id) }
    }
}
