package com.bytemedrive.file

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileDeleted(val id: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.files.removeIf { it.id == id }
    }
}
