package com.bytemedrive.file.shared

import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.bytemedrive.database.FileUpload
import com.bytemedrive.file.root.Chunk
import com.bytemedrive.file.root.DataFileLink
import com.bytemedrive.file.root.EventFileUploaded
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.folder.Folder
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Base64
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import java.io.File as JavaFile

class FileManager(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
) {

    suspend fun uploadFile(fileUpload: FileUpload, tmpFolder: File, file: File) = withContext(Dispatchers.IO) {
        val dataFileId = UUID.fromString(fileUpload.id)

        val tmpOriginalFile = File(file.path)

        val tmpEncryptedFile = File.createTempFile("$dataFileId-encrypted", null, tmpFolder)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(tmpOriginalFile.inputStream(), tmpEncryptedFile.outputStream(), secretKey)

        val chunks = getChunks(tmpEncryptedFile, tmpFolder)
        val contentType = getContentTypeFromFile(file) ?: UNKNOWN_MIME_TYPE

        AppState.customer.value?.wallet?.let { wallet ->
            fileRepository.upload(wallet, chunks)
            eventPublisher.publishEvent(
                EventFileUploaded(
                    dataFileId,
                    chunks.map { it.id },
                    chunks.map { it.viewId },
                    fileUpload.name,
                    tmpEncryptedFile.length(),
                    ShaService.checksum(tmpOriginalFile.inputStream()),
                    contentType,
                    Base64.getEncoder().encodeToString(secretKey.encoded),
                    UUID.randomUUID(),
                    fileUpload.folderId?.let { UUID.fromString(fileUpload.folderId) }
                )
            )
        }
    }

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
                fileRepository.download(chunkViewId)?.bodyAsChannel()?.toInputStream()?.use { it.copyTo(outputStream, BUFFER_SIZE) }
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

    private fun getContentTypeFromFile(file: File): String? = file.toURI().toURL().openConnection().contentType

    companion object {

        const val CHUNK_SIZE_BYTES = 16 * 1024 * 1024
        const val BUFFER_SIZE = 1024
        const val UNKNOWN_MIME_TYPE = "unknown"
    }
}