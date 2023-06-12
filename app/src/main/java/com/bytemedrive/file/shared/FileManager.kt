package com.bytemedrive.file.shared

import com.bytemedrive.file.root.File
import com.bytemedrive.folder.Folder
import java.util.UUID

class FileManager {
    fun findAllFilesRecursively(folderId: UUID, allFolders: List<Folder>, allFiles: List<File>): List<File> {
        val filesToRemove = allFiles.filter { it.folderId == folderId }
        val subFolders = allFolders.filter { it.parent == folderId }

        val filesInSubFolders = mutableListOf<File>()
        for (subfolder in subFolders) {
            filesInSubFolders.addAll(findAllFilesRecursively(subfolder.id, allFolders, allFiles))
        }

        return filesToRemove + filesInSubFolders
    }
}