package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFolderCopied(val currentId: UUID, val newId: UUID? = null, val parentId: UUID? = null) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.find { it.id == currentId }?.let { folder ->
            val id = newId ?: folder.id
            val newFolder = folder.copy(id = id, parent = parentId)

            customer.folders = (customer.folders + newFolder).toMutableList()
        }
    }
}
