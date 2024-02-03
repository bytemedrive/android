package com.bytemedrive.folder

import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class FolderManager(
    private val externalScope: CoroutineScope,
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val folderManager: FolderManager,
    private val fileManager: FileManager,
    private val folderRepository: FolderRepository,
    private val dataFileRepository: DataFileRepository,
    private val customerRepository: CustomerRepository,
) {
    fun removeFolder(id: UUID) = externalScope.launch {
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        customerRepository.getCustomer()?.let { customer ->
            folderRepository.getFolderById(id)?.let { folder ->
                fileManager.findAllFilesRecursively(id, folders, dataFileLinks).forEach { dataFileLink ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                    eventPublisher.publishEvent(EventFileDeleted(listOf(dataFileLink.id)))

                    if (physicalFileRemovable && customer.walletId != null) {
                        fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                    }
                }
                (folderManager.findAllFoldersRecursively(id, folders) + folder).forEach {
                    eventPublisher.publishEvent(EventFolderDeleted(listOf(it.id)))
                }
            }
        }
    }

    fun findAllFoldersRecursively(folderId: UUID, allFolders: List<Folder>): List<Folder> {
        val objectsInFolder = allFolders.filter { it.parent == folderId }

        val objectsInSubFolders = mutableListOf<Folder>()
        for (subfolder in objectsInFolder) {
            objectsInSubFolders.addAll(findAllFoldersRecursively(subfolder.id, allFolders))
        }

        return objectsInFolder + objectsInSubFolders
    }
}