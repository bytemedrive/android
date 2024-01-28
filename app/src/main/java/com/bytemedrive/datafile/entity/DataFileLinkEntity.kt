package com.bytemedrive.datafile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "data_file_link",
    indices = [Index(value = ["dataFileId"]), Index(value = ["folderId"])]
)
data class DataFileLinkEntity(
    @PrimaryKey
    val id: UUID,
    val dataFileId: UUID,
    val name: String,
    val folderId: UUID?,
    val uploading: Boolean,
    val starred: Boolean,
) {

    fun ofUploading(uploading: Boolean): DataFileLinkEntity = DataFileLinkEntity(id, dataFileId, name, folderId, uploading, starred)
}
