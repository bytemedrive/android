package com.bytemedrive.file.shared

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import com.bytemedrive.database.FileUpload
import com.bytemedrive.file.root.Chunk
import com.bytemedrive.file.root.DataFileLink
import com.bytemedrive.file.root.EventFileCopied
import com.bytemedrive.file.root.EventFileUploadCompleted
import com.bytemedrive.file.root.EventFileUploadStarted
import com.bytemedrive.file.root.EventThumbnailCompleted
import com.bytemedrive.file.root.EventThumbnailStarted
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.root.UploadChunk
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
import java.time.ZonedDateTime
import java.util.Base64
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.roundToInt
import java.io.File as JavaFile

class FileManager(
    private val context: Context,
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
) {

    private val TAG = FileManager::class.qualifiedName

    suspend fun downloadFile(dataFileLinkId: UUID) =
        AppState.customer!!.dataFilesLinks.value.find { it.id == dataFileLinkId }?.let { dataFileLink ->
            AppState.customer!!.dataFiles.value.find { it.id == dataFileLink.dataFileId }?.let { dataFile ->
                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, dataFileLink.name)
                    put(MediaStore.Downloads.MIME_TYPE, dataFile.contentType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                val encryptedFile = rebuildFile(dataFile.chunks.map { it.viewId }, "${dataFileLink.id}-encrypted", dataFile.contentType!!, context.cacheDir)

                val sizeOfChunks = dataFile.chunks.sumOf(UploadChunk::sizeBytes)

                if (sizeOfChunks != encryptedFile.length()) {
                    Log.e(TAG, "Encrypted file size ${encryptedFile.length()} is not same as encrypted file chunks size $sizeOfChunks")
                } else {
                    AesService.decryptWithKey(encryptedFile.inputStream(), contentResolver.openOutputStream(uri!!)!!, dataFile.secretKey!!, encryptedFile.length())
                }
            }
        }

    suspend fun uploadFile(fileUpload: FileUpload, tmpFolder: File, file: File) = withContext(Dispatchers.IO) {
        val dataFileId = fileUpload.id
        val folder = fileUpload.folderId

        val tmpOriginalFile = File(file.path)

        val checksum = ShaService.checksum(tmpOriginalFile.inputStream())
        val sameDataFile = AppState.customer!!.dataFiles.value.find { it.checksum == checksum }

        if (sameDataFile != null) {
            eventPublisher.publishEvent(EventFileCopied(sameDataFile.id, UUID.randomUUID(), folder, "Copy of ${sameDataFile.name}"))
        } else {
            val tmpEncryptedFile = File.createTempFile("$dataFileId-encrypted", ".${file.extension}", tmpFolder)

            val secretKey = AesService.generateNewFileSecretKey()
            AesService.encryptWithKey(tmpOriginalFile.inputStream(), tmpEncryptedFile.outputStream(), secretKey, tmpOriginalFile.length())

            val chunks = getChunks(tmpEncryptedFile, tmpFolder)
            val contentType = getContentTypeFromFile(file) ?: UNKNOWN_MIME_TYPE
            val exifOrientation = if (contentType.contains("image"))
                ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) else
                null

            Log.i(TAG, "File ${file.name} split into ${chunks.size} chunks")

            AppState.customer?.wallet?.let { wallet ->
                eventPublisher.publishEvent(
                    EventFileUploadStarted(
                        dataFileId,
                        chunks.map { UploadChunk(it.id, it.viewId, it.file.length()) },
                        checksum,
                        contentType,
                        Base64.getEncoder().encodeToString(secretKey.encoded),
                        ZonedDateTime.now(),
                        exifOrientation
                    )
                )
                fileRepository.upload(wallet, chunks)
                eventPublisher.publishEvent(EventFileUploadCompleted(dataFileId, ZonedDateTime.now()))
                tmpOriginalFile.delete()
                tmpEncryptedFile.delete()
                chunks.forEach { it.file.delete() }
            }
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

    suspend fun rebuildFile(viewIds: List<UUID>, fileName: String, contentType: String, outputDirectory: JavaFile): JavaFile {
        val resultFile = JavaFile.createTempFile(fileName, ".${MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)}", outputDirectory)

        resultFile.outputStream().use { outputStream ->
            viewIds.forEach { chunkViewId ->
                fileRepository.download(chunkViewId)?.bodyAsChannel()?.toInputStream()?.use { it.copyTo(outputStream, BUFFER_SIZE_DEFAULT) }
            }
        }

        return resultFile
    }

    suspend fun uploadThumbnail(bytes: ByteArray, directory: File, sourceDataFileId: UUID, contentType: String, resolution: Resolution) {
        val thumbnailDataFileId = UUID.randomUUID()
        val tmpEncryptedFile = File.createTempFile("$thumbnailDataFileId-encrypted", ".${MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)}", directory)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(bytes.inputStream(), tmpEncryptedFile.outputStream(), secretKey, bytes.size.toLong())

        val chunks = getChunks(tmpEncryptedFile, directory)

        AppState.customer?.wallet?.let { wallet ->
            eventPublisher.publishEvent(
                EventThumbnailStarted(
                    sourceDataFileId,
                    chunks.map { UploadChunk(it.id, it.viewId, it.file.length()) },
                    bytes.size.toLong(),
                    ShaService.checksum(bytes.inputStream()),
                    contentType,
                    Base64.getEncoder().encodeToString(secretKey.encoded),
                    resolution,
                    ZonedDateTime.now()
                )
            )

            fileRepository.upload(wallet, chunks)
            eventPublisher.publishEvent(EventThumbnailCompleted(sourceDataFileId, resolution, ZonedDateTime.now()))
        }
    }

    fun getThumbnail(original: Bitmap, resolution: Resolution): Bitmap {
        val ratio = resolution.value.toDouble() / original.height

        return ThumbnailUtils.extractThumbnail(original, (original.width * ratio).roundToInt(), (original.height * ratio).roundToInt())
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
        val file = JavaFile.createTempFile(id.toString(), ".${original.extension}", outputFolder)

        original.inputStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.skip(start)

                val buffer = ByteArray(BUFFER_SIZE_DEFAULT)
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

    private fun getContentTypeFromFile(file: File): String? {
        val extension = file.extension.lowercase(Locale.getDefault())
        val mimeTypeMap = MimeTypeMap.getSingleton()

        return mimeTypeMap.getMimeTypeFromExtension(extension)
    }

    companion object {

        const val CHUNK_SIZE_BYTES = 16 * 1024 * 1024
        const val BUFFER_SIZE_DEFAULT = 1024
        const val UNKNOWN_MIME_TYPE = "unknown"

        fun computeBufferSize(fileSize: Long): Int {
            var newSize = BUFFER_SIZE_DEFAULT

            while ((fileSize % newSize) < (newSize / 2.0)) {
                newSize += 1
            }

            return newSize
        }

        fun getThumbnailName(fileId: UUID, resolution: Resolution): String {
            return "${fileId}_${resolution.value}"
        }
    }
}