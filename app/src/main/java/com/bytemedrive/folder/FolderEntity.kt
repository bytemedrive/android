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
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "starred") val starred: Boolean = false,
    @ColumnInfo(name = "parent") val parent: UUID? = null,
)
