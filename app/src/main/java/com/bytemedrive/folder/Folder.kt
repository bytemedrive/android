package com.bytemedrive.folder

import java.util.UUID

data class Folder(
    val id: UUID,
    val name: String,
    val starred: Boolean = false,
    val parent: UUID? = null,
) {
    constructor(folderEntity: FolderEntity): this(
        folderEntity.id,
        folderEntity.name,
        folderEntity.starred,
        folderEntity.parent
    )
}
