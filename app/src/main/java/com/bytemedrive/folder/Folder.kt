package com.bytemedrive.folder

import java.util.UUID

class Folder(
    val id: UUID,
    val name: String,
    val parent: UUID?
)
