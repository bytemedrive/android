package com.bytemedrive.file.root

import com.bytemedrive.kotlin.updateIf
import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import kotlinx.coroutines.flow.update
import java.util.UUID

data class EventFileStarRemoved(val dataFileLinkId: UUID) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFilesLinks.update { dataFileLink -> dataFileLink.updateIf({ it.id == dataFileLinkId }, { it.copy(starred = false) } ) }
    }
}
