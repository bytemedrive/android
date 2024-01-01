package com.bytemedrive.datafile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "data_file_link",
    indices = [Index(value = ["data_file_id"]), Index(value = ["folder_id"])]
)
data class DataFileLinkEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "data_file_id") val dataFileId: UUID,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "folder_id") val folderId: UUID?,
    @ColumnInfo(name = "uploading") val uploading: Boolean,
    @ColumnInfo(name = "starred") val starred: Boolean,
) {

    fun ofUploading(uploading: Boolean): DataFileLinkEntity {
        return DataFileLinkEntity(id, dataFileId, name, folderId, uploading, starred)
    }
}
