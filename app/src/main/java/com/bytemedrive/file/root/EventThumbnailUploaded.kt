package com.bytemedrive.file.root

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventThumbnailUploaded(
    val sourceDataFileId: UUID,
    val thumbnailDataFileId: UUID,
    val resolution: Resolution,
) : Convertable {

    override fun convert(customer: CustomerAggregate) {
        customer.dataFiles.find { it.id == sourceDataFileId }?.thumbnails?.add(DataFile.Thumbnail(thumbnailDataFileId, resolution))
    }
}
