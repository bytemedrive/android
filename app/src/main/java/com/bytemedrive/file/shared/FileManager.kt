package com.bytemedrive.file.shared

import com.bytemedrive.file.root.Chunk
import com.bytemedrive.file.root.DataFileLink
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.folder.Folder
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.util.UUID
import kotlin.math.ceil
import java.io.File as JavaFile

class FileManager(
    private val fileRepository: FileRepository,
) {

    fun findAllFilesRecursively(folderId: UUID, allFolders: List<Folder>, allFiles: List<DataFileLink>): List<DataFileLink> {
        val filesToRemove = allFiles.filter { it.folderId == folderId }
        val subFolders = allFolders.filter { it.parent == folderId }

        val filesInSubFolders = mutableListOf<DataFileLink>()
        for (subfolder in subFolders) {
            filesInSubFolders.addAll(findAllFilesRecursively(subfolder.id, allFolders, allFiles))
        }

        return filesToRemove + filesInSubFolders
    }

    fun getChunks(original: JavaFile, chunksFolder: JavaFile): List<Chunk> {
        val sizeBytes = original.length()

        if (sizeBytes <= CHUNK_SIZE_BYTES) {
            return splitSmallFile(original, chunksFolder)
        }

        return splitLargeFile(original, chunksFolder)
    }

    suspend fun rebuildFile(viewIds: List<UUID>, fileName: String, outputDirectory: JavaFile): JavaFile {
        val resultFile = JavaFile.createTempFile(fileName, null, outputDirectory)

        resultFile.outputStream().use { outputStream ->
            viewIds.forEach { chunkViewId ->
                fileRepository.download(chunkViewId).bodyAsChannel().toInputStream().use { it.copyTo(outputStream, BUFFER_SIZE) }
            }
        }

        return resultFile
    }

    private fun splitSmallFile(original: JavaFile, chunksFolder: JavaFile): List<Chunk> {
        val sizeBytes = original.length()
        val sizeFirst = (0..sizeBytes).random()
        val sizeSecond = sizeBytes - sizeFirst

        val chunkFirst = createChunk(original, chunksFolder, 0, sizeFirst)
        val chunkSecond = createChunk(original, chunksFolder, sizeFirst, sizeSecond)

        return listOf(chunkFirst, chunkSecond)
    }

    private fun splitLargeFile(original: JavaFile, chunksFolder: JavaFile): List<Chunk> {
        var remainingSizeBytes = original.length()
        var remainingChunksCount = ceil(remainingSizeBytes / CHUNK_SIZE_BYTES.toDouble()).toInt()
        var idealChunkSize = remainingSizeBytes / remainingChunksCount
        var lastPosition = 0L

        return (1..remainingChunksCount).map {
            if (remainingChunksCount == 1) {
                createChunk(original, chunksFolder, lastPosition, remainingSizeBytes)
            } else {
                val chunkSize = (0..(2 * idealChunkSize)).random()
                val chunk = createChunk(original, chunksFolder, lastPosition, chunkSize)

                remainingSizeBytes -= chunkSize
                remainingChunksCount--
                lastPosition += chunkSize
                idealChunkSize = remainingSizeBytes / remainingChunksCount

                chunk
            }
        }
    }

    private fun createChunk(original: JavaFile, outputFolder: JavaFile, start: Long, length: Long): Chunk {
        val id = UUID.randomUUID()
        val viewId = UUID.randomUUID()
        val file = JavaFile.createTempFile(id.toString(), null, outputFolder)

        original.inputStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.skip(start)

                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var bytesRemaining = length

                while (inputStream.read(buffer).also { bytesRead = it } != -1 && bytesRemaining > 0) {
                    if (bytesRead <= bytesRemaining) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesRemaining -= bytesRead
                    } else {
                        outputStream.write(buffer, 0, bytesRemaining.toInt())
                        break
                    }
                }
            }
        }

        return Chunk(id, viewId, file)
    }

    companion object {

        const val CHUNK_SIZE_BYTES = 16 * 1024 * 1024
        const val BUFFER_SIZE = 1024
    }
}