package com.bytemedrive.folder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FolderDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg folders: FolderEntity)

    @Update
    suspend fun update(vararg folders: FolderEntity)

    @Query("SELECT * FROM folder")
    suspend fun getAll(): List<FolderEntity>

    @Query("select * from folder where id = :id")
    suspend fun getById(id: UUID): FolderEntity?

    @Query("select * from folder where id = :id")
    fun getByIdAsFlow(id: UUID): Flow<FolderEntity>

    @Query("select * from folder where parent = :id")
    fun getByParentIdFlow(id: UUID?): Flow<List<FolderEntity>>

    @Query("select * from folder where parent = :id")
    suspend fun getByParent(id: UUID): List<FolderEntity>

    @Query("delete from folder where id = :id")
    suspend fun delete(id: UUID)

    @Query("DELETE FROM folder WHERE id IN(:ids)")
    suspend fun deleteByIds(ids: List<UUID>)
}