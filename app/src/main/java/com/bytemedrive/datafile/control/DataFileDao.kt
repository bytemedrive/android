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

    @Query("SELECT SUM(sizeBytes) / 1073741824.0 as usedStorage FROM data_file")
    suspend fun getUsedStorage(): String

    @Query("select * from data_file where id = :id")
    suspend fun getDataFileById(id: UUID): DataFileEntity?

    @Query("SELECT * FROM data_file WHERE checksum = :checksum")
    suspend fun getDataFileByChecksum(checksum: String): DataFileEntity?

    @Query("select * from data_file where id IN(:ids)")
    suspend fun getDataFilesByIds(ids: List<UUID>): List<DataFileEntity>

    @Query("SELECT * FROM data_file_link WHERE starred = :starred")
    suspend fun getAllDataFileLinks(starred: Boolean): List<DataFileLinkEntity>

    @Query("select * from data_file_link where id = :id")
    suspend fun getDataFileLinkById(id: UUID): DataFileLinkEntity?

    @Query("select * from data_file_link where dataFileId IN (:dataFileLinkIds)")
    suspend fun getDataFileLinksByIds(dataFileLinkIds: List<UUID>): List<DataFileLinkEntity>

    @Query("select * from data_file_link where dataFileId= :dataFileId")
    suspend fun getDataFileLinksByDataFileId(dataFileId: UUID): List<DataFileLinkEntity>

    @Query("select * from data_file_link where folderId = :folderId")
    fun getDataFileLinksByFolderIdFlow(folderId: UUID?): Flow<List<DataFileLinkEntity>>

    @Query("select * from data_file_link where folderId = :folderId")
    suspend fun getDataFileLinksByFolderId(folderId: UUID?): List<DataFileLinkEntity>

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