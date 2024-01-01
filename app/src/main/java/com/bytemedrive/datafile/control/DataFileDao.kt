package com.bytemedrive.datafile.control

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import java.util.UUID

@Dao
interface DataFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg dataFile: DataFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg dataFile: DataFileLinkEntity)

    @Query("select * from data_file where id = :id")
    suspend fun geDataFileById(id: UUID): DataFileEntity

    @Query("select * from data_file_link where id = :id")
    suspend fun geDataFileLinkById(id: UUID): DataFileLinkEntity

    @Query("select * from data_file_link where data_file_id= :dataFileId")
    suspend fun geDataFileLinksByDataFile(dataFileId: UUID): List<DataFileLinkEntity>

    @Update
    suspend fun update(vararg dataFiles: DataFileEntity)

    @Update
    suspend fun update(vararg dataFileLinks: DataFileLinkEntity)

    @Delete
    suspend fun delete(dataFile: DataFileEntity)

    @Delete
    suspend fun delete(dataFileLink: DataFileLinkEntity)

    @Query("delete from data_file_link where folder_id = :folderId")
    suspend fun deleteByFolder(folderId: UUID)
}