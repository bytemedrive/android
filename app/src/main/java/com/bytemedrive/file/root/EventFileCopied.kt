package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFileCopied(val dataFileId: UUID, val dataFileLinkId: UUID, val folderId: UUID? = null, val name: String) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.update { it + DataFileLink(dataFileLinkId, dataFileId, name, folderId) }
    }
}
