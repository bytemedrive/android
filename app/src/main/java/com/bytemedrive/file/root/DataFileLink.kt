package com.bytemedrive.file.root

import java.util.UUID

data class DataFileLink(
    val id: UUID,
    val dataFileId: UUID,
    val name: String,
    val folderId: UUID?,
    val starred: Boolean = false,
)
