package com.bytemedrive.datafile.control

import com.bytemedrive.datafile.entity.DataFileLink
import kotlinx.coroutines.flow.map
import java.util.UUID

class DataFileRepository(
    private val dataFileDao: DataFileDao
) {

    fun getDataFileLinksByFolderIdFlow(folderId: UUID?) = dataFileDao.getDataFileLinksByFolderIdFlow(folderId).map { it.map(::DataFileLink) }

    suspend fun getDataFileLinkById(dataFileLinkId: UUID) = dataFileDao.getDataFileLinkById(dataFileLinkId)?.let(::DataFileLink)

    suspend fun getAllDataFileLinks()  = dataFileDao.getAllDataFileLinks().map(::DataFileLink)
}