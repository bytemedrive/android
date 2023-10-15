package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFolderDeleted(val ids: List<UUID>) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.removeIf { ids.contains(it.id) }
    }
}
