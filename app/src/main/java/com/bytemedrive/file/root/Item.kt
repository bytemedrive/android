package com.bytemedrive.file.root

import java.util.UUID

data class Item(val id: UUID, val name: String, val type: ItemType, val starred: Boolean, val uploading: Boolean)

enum class ItemType(type: String) {
    Folder("folder"),
    File("file"),
}