package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFolderDeleted(val ids: List<UUID>) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.update { folders ->
            folders.filterNot { ids.contains(it.id) }
        }
    }
}
