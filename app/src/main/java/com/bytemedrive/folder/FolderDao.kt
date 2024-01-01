package com.bytemedrive.folder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg folders: FolderEntity)

    @Update
    suspend fun update(vararg folders: FolderEntity)

    @Query("select * from folder where id = :id")
    suspend fun getById(id: UUID): FolderEntity

    @Query("select * from folder where parent = :id")
    suspend fun getByParent(id: UUID): List<FolderEntity>

    @Query("delete from folder where id = :id")
    suspend fun delete(id: UUID)
}