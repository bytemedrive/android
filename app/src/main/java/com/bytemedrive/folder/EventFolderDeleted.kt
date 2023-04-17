package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFolderDeleted(val id: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.removeIf { it.id == id }
    }
}
