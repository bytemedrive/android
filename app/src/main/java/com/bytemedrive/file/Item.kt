package com.bytemedrive.file

import java.util.UUID

data class Item(val id: UUID, val name: String, val type: ItemType)

enum class ItemType(type: String) {
    Folder("folder"),
    File("file"),
}