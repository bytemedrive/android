package com.bytemedrive.folder

import java.util.UUID

class FolderManager {
    fun findAllFoldersRecursively(folderId: UUID, allFolders: List<FolderEntity>): List<FolderEntity> {
        val objectsInFolder = allFolders.filter { it.parent == folderId }

        val objectsInSubFolders = mutableListOf<FolderEntity>()
        for (subfolder in objectsInFolder) {
            objectsInSubFolders.addAll(findAllFoldersRecursively(subfolder.id, allFolders))
        }

        return objectsInFolder + objectsInSubFolders
    }
}