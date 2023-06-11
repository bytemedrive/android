package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileMoved(val fileId: UUID, val folderId: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.files.replaceAll {
            if (it.id == fileId) {
                it.copy(folderId = folderId)
            } else it
        }
    }
}
