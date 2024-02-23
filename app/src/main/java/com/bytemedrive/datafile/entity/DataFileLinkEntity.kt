package com.bytemedrive.datafile.entity

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
    val uploadStatus: UploadStatus,
    val starred: Boolean,
) {

    fun setUploadStatus(uploadStatus: UploadStatus): DataFileLinkEntity = DataFileLinkEntity(id, dataFileId, name, folderId, uploadStatus, starred)
}
