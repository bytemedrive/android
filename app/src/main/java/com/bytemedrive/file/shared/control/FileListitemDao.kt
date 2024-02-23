package com.bytemedrive.file.shared.control

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.file.shared.entity.FileListItemEntity
import java.util.UUID

@Dao
interface FileListItemDao {

    @Query("""
        SELECT id, name, 'FOLDER' AS type, starred, 'COMPLETED' AS uploadStatus, parent AS folderId
        FROM folder 
        WHERE parent IS :folderId 
        UNION ALL 
            SELECT id, name, 'FILE' AS type, starred, uploadStatus, folderId 
            FROM data_file_link 
            WHERE folderId IS :folderId
        """)
    fun getAllPaged(folderId: UUID?): PagingSource<Int, FileListItemEntity>

    @Query("""
        SELECT id, name, 'FOLDER' AS type, starred, 'COMPLETED' AS uploadStatus, parent AS folderId 
        FROM folder 
        WHERE parent = 1 AND folderId IS :folderId 
        UNION ALL
            SELECT id, name, 'FILE' AS type, starred, uploadStatus, folderId 
            FROM data_file_link 
            WHERE starred = 1 AND folderId IS :folderId
        """)
    fun getAllStarredPaged(folderId: UUID?): PagingSource<Int, FileListItemEntity>
}