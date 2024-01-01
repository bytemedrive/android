package com.bytemedrive.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datafilelink")
data class DataFileLinkEntity(
    @PrimaryKey
    val id: String,
    val name: String
)