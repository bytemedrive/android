package com.bytemedrive.file.shared.entity

import androidx.room.PrimaryKey
import java.util.UUID

data class FileListItem(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val type: ItemType,
    val starred: Boolean,
    val uploading: Boolean,
    val folderId: UUID? = null
) {
    constructor(itemEntity: FileListItemEntity): this(
        itemEntity.id,
        itemEntity.name,
        itemEntity.type,
        itemEntity.starred,
        itemEntity.uploading,
        itemEntity.folderId
    )
}

