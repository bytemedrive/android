package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileCopied(val currentId: UUID, val newId: UUID, val folderId: UUID? = null, val name: String? = null) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.files.find { it.id == currentId }?.let { file ->
            val filename = if (name.isNullOrEmpty()) file.name else name
            val newFile = file.copy(id = newId, name = filename, folderId = folderId)

            customer.files = (customer.files + newFile).toMutableList()
        }
    }
}
