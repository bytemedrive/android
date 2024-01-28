package com.bytemedrive.datafile.control

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import com.bytemedrive.datafile.entity.UploadStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DataFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg dataFile: DataFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg dataFile: DataFileLinkEntity)

    @Query("SELECT SUM(sizeBytes) / 1073741824.0 as usedStorage FROM data_file")
    suspend fun getUsedStorage(): String?

    @Query("SELECT * FROM data_file")
    suspend fun getAllDataFiles(): List<DataFileEntity>

    @Query("select * from data_file where id = :id")
    suspend fun getDataFileById(id: UUID): DataFileEntity?

    @Query("SELECT * FROM data_file WHERE checksum IS :checksum")
    suspend fun getDataFileByChecksum(checksum: String?): DataFileEntity?

    @Query("select * from data_file where id IN(:ids)")
    suspend fun getDataFilesByIds(ids: List<UUID>): List<DataFileEntity>

    @Query("select * from data_file where uploadStatus = :uploadStatus")
    suspend fun getDataFilesByUploadStatus(uploadStatus: UploadStatus): List<DataFileEntity>

    @Query("SELECT * FROM data_file_link")
    suspend fun getAllDataFileLinks(): List<DataFileLinkEntity>

    @Query("SELECT * FROM data_file_link WHERE starred = :starred")
    suspend fun getDataFileLinksStarred(starred: Boolean): List<DataFileLinkEntity>

    @Query("SELECT * FROM data_file_link WHERE starred = :starred")
    fun getDataFileLinksStarredFlow(starred: Boolean): Flow<List<DataFileLinkEntity>>

    @Query("select * from data_file_link where id = :id")
    suspend fun getDataFileLinkById(id: UUID): DataFileLinkEntity?

    @Query("select * from data_file_link where id IN (:dataFileLinkIds)")
    suspend fun getDataFileLinksByIds(dataFileLinkIds: List<UUID>): List<DataFileLinkEntity>

    @Query("select * from data_file_link where dataFileId= :dataFileId")
    suspend fun getDataFileLinksByDataFileId(dataFileId: UUID): List<DataFileLinkEntity>

    @Query("select * from data_file_link where folderId IS :folderId")
    fun getDataFileLinksByFolderIdFlow(folderId: UUID?): Flow<List<DataFileLinkEntity>>

    @Query("select * from data_file_link where folderId IS :folderId")
    suspend fun getDataFileLinksByFolderId(folderId: UUID?): List<DataFileLinkEntity>

    @Update
    suspend fun update(vararg dataFiles: DataFileEntity)

    @Update
    suspend fun update(vararg dataFileLinks: DataFileLinkEntity)

    @Delete
    suspend fun delete(dataFile: DataFileEntity)

    @Delete
    suspend fun delete(dataFileLink: DataFileLinkEntity)

    @Query("DELETE FROM data_file WHERE id IN(:ids)")
    suspend fun deleteDataFilesByIds(ids: List<UUID>)

    @Query("DELETE FROM data_file_link WHERE id IN(:ids)")
    suspend fun deleteDataFileLinksByIds(ids: List<UUID>)

    @Query("delete from data_file_link where folderId IN(:folderIds)")
    suspend fun deleteDataFileLinksByFolderIds(folderIds: List<UUID>)
}