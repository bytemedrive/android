package com.bytemedrive.file.root

import com.bytemedrive.folder.Folder
import java.util.UUID
import javax.crypto.SecretKey

data class File(
    val id: UUID,
    val chunkId: UUID,
    val name: String,
    val sizeBytes: Long,
    val contentType: String,
    val secretKey: SecretKey,
    val starred: Boolean = false,
    val folderId: UUID?,
    val thumbnails: MutableList<Thumbnail> = mutableListOf()
) {

    data class Thumbnail(val id: UUID, val chunkId: UUID, val resolution: Resolution, val secretKey: SecretKey)

    companion object {
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
}
