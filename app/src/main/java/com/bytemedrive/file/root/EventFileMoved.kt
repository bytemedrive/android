package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventFileMoved(val dataFileLinkId: UUID, val folderId: UUID?) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.replaceAll {
            if (it.id == dataFileLinkId) {
                it.copy(folderId = folderId)
            } else it
        }
    }
}
