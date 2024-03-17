package com.bytemedrive.file.shared.control

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.bytemedrive.file.shared.entity.FileListItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FileListItemDao {

    @Query(
        """
        SELECT id, name, 'FOLDER' AS type, starred, 'COMPLETED' AS uploadStatus, parent AS folderId
        FROM folder
        WHERE parent IS :folderId
        UNION ALL
        SELECT id, name, 'FILE' AS type, starred, uploadStatus, folderId
        FROM data_file_link
        WHERE folderId IS :folderId
        """
    )
    suspend fun getAll(folderId: UUID?): List<FileListItemEntity>

    @Query(
        """
        SELECT id, name, 'FOLDER' AS type, starred, 'COMPLETED' AS uploadStatus, parent AS folderId
        FROM folder
        WHERE parent IS :folderId
        UNION ALL
        SELECT id, name, 'FILE' AS type, starred, uploadStatus, folderId
        FROM data_file_link
        WHERE folderId IS :folderId
        """
    )
    fun getAllPaged(folderId: UUID?): PagingSource<Int, FileListItemEntity>

    @Query(
        """
        SELECT id, name, 'FOLDER' AS type, starred, 'COMPLETED' AS uploadStatus, parent AS folderId 
        FROM folder 
        WHERE starred = :starred
        UNION ALL
            SELECT id, name, 'FILE' AS type, starred, uploadStatus, folderId 
            FROM data_file_link 
            WHERE starred = :starred
        """
    )
    fun getAllStarredPaged(starred: Boolean): PagingSource<Int, FileListItemEntity>

    @Query(
        """
        SELECT id, name, 'FOLDER' AS type, starred, 'COMPLETED' AS uploadStatus, parent AS folderId 
        FROM folder 
        WHERE starred = :starred
        UNION ALL
            SELECT id, name, 'FILE' AS type, starred, uploadStatus, folderId 
            FROM data_file_link 
            WHERE starred = :starred
        """
    )
    fun getAllStarredFlow(starred: Boolean): Flow<List<FileListItemEntity>>
}