package com.bytemedrive.folder

import java.util.UUID

class FolderManager {
    fun findAllFoldersRecursively(folderId: UUID, allFolders: List<Folder>): List<Folder> {
        val objectsInFolder = allFolders.filter { it.parent == folderId }

        val objectsInSubFolders = mutableListOf<Folder>()
        for (subfolder in objectsInFolder) {
            objectsInSubFolders.addAll(findAllFoldersRecursively(subfolder.id, allFolders))
        }

        return objectsInFolder + objectsInSubFolders
    }
}