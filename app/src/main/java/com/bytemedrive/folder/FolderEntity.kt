package com.bytemedrive.folder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "folder",
    indices = [Index(value = ["parent"])]
)
data class FolderEntity(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val starred: Boolean = false,
    val parent: UUID? = null,
)
