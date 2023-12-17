package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFolderStarAdded(val id: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.update { folders ->
            folders.map {
                if (it.id == id) {
                    it.copy(starred = true)
                } else it
            }
        }
    }
}
