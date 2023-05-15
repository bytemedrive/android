package com.bytemedrive.file

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileStarRemoved(val id: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.files.replaceAll {
            if (it.id == id) {
                it.copy(starred = false)
            } else it
        }
    }
}
