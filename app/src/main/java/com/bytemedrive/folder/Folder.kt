package com.bytemedrive.folder

import java.util.UUID

data class Folder(
    val id: UUID,
    val name: String,
    val starred: Boolean = false,
    val parent: UUID?,
)
