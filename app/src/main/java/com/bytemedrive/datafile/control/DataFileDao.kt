package com.bytemedrive.datafile.control

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DataFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg dataFile: DataFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg dataFile: DataFileLinkEntity)


    @Query("select * from data_file where id = :id")
    suspend fun getDataFileById(id: UUID): DataFileEntity?

    @Query("select * from data_file_link")
    suspend fun getAllDataFileLinks(): List<DataFileLinkEntity>

    @Query("select * from data_file_link where id = :id")
    suspend fun getDataFileLinkById(id: UUID): DataFileLinkEntity?

    @Query("select * from data_file_link where dataFileId= :dataFileId")
    suspend fun getDataFileLinksByDataFile(dataFileId: UUID): List<DataFileLinkEntity>

    @Query("select * from data_file_link where folderId = :folderId")
    fun getDataFileLinksByFolderIdFlow(folderId: UUID?): Flow<List<DataFileLinkEntity>>

    @Update
    suspend fun update(vararg dataFiles: DataFileEntity)

    @Update
    suspend fun update(vararg dataFileLinks: DataFileLinkEntity)

    @Delete
    suspend fun delete(dataFile: DataFileEntity)

    @Delete
    suspend fun delete(dataFileLink: DataFileLinkEntity)

    @Query("DELETE FROM data_file_link WHERE id IN(:ids)")
    suspend fun deleteByIds(ids: List<UUID>)

    @Query("delete from data_file_link where folderId IN(:folderIds)")
    suspend fun deleteByFolderIds(folderIds: List<UUID>)
}