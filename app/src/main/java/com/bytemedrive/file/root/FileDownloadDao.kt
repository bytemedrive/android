package com.bytemedrive.file.root

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID

@Dao
interface FileDownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg files: FileDownloadEntity)

    @Query("select * from file_download order by queued_at asc")
    suspend fun getAll(): List<FileDownloadEntity>

    @Query("delete from file_download where id = :id")
    suspend fun delete(id: UUID)
}