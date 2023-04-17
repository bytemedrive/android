package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFolderCreated(
    val id: UUID,
    val name: String,
    val starred: Boolean = false,
    val parent: UUID? = null
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.add(Folder(id, name, starred, parent))
    }
}
