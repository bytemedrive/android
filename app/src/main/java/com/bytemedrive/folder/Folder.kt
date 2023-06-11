package com.bytemedrive.folder

import java.util.UUID

data class Folder(
    val id: UUID,
    val name: String,
    val starred: Boolean = false,
    val parent: UUID?,
) {
    companion object {
        fun findAllFoldersRecursively(folderId: UUID, allFolders: List<Folder>): List<Folder> {
            val objectsInFolder = allFolders.filter { it.parent == folderId }

            val objectsInSubFolders = mutableListOf<Folder>()
            for (subfolder in objectsInFolder) {
                objectsInSubFolders.addAll(findAllFoldersRecursively(subfolder.id, allFolders))
            }

            return objectsInFolder + objectsInSubFolders
        }
    }
}
