package com.bytemedrive.folder

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFolderCopied(val currentId: UUID, val newId: UUID? = null, val parentId: UUID? = null) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.folders.update { folders ->
            folders.find { it.id == currentId }?.let { folder ->
                val id = newId ?: folder.id
                val newFolder = folder.copy(id = id, parent = parentId)

                folders + newFolder
            } ?: folders
        }
    }
}
