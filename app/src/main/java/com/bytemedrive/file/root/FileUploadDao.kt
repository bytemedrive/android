package com.bytemedrive.file.root

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID

@Dao
interface FileUploadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg files: FileUploadEntity)

    @Query("select * from file_upload order by queuedAt asc")
    suspend fun getAll(): List<FileUploadEntity>

    @Query("delete from file_upload where id = :id")
    suspend fun delete(id: UUID)
}