package com.bytemedrive.file.root

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes.*
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.Base64
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.roundToInt

class UploadViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
) : ViewModel() {

    private val TAG = UploadViewModel::class.qualifiedName

    fun uploadFile(inputStream: InputStream, folder: File, fileName: String, folderId: String?, contentType: String, onSuccess: () -> Unit) {
        val fileId = UUID.randomUUID()
        val tmpFile = File.createTempFile(fileId.toString(), null, folder)
        inputStream.copyTo(tmpFile.outputStream(), BUFFER_SIZE)

        val tmpEncryptedFile = File.createTempFile("$fileId-encrypted", null, folder)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(tmpFile.inputStream(), tmpEncryptedFile.outputStream(), secretKey)

        val chunks = getChunks(tmpEncryptedFile, folder)

        viewModelScope.launch {
            AppState.customer.value?.wallet?.let { wallet ->
                fileRepository.upload(wallet, chunks)
                eventPublisher.publishEvent(
                    EventFileUploaded(
                        fileId,
                        chunks.map { it.id },
                        chunks.map { it.viewId },
                        fileName,
                        tmpEncryptedFile.length(),
                        "xxx",
                        contentType,
                        Base64.getEncoder().encodeToString(secretKey.encoded),
                        false,
                        folderId?.let { UUID.fromString(folderId) }
                    )
                )

                when (contentType) {
                    IMAGE_JPEG -> {
                        val bytes = tmpFile.readBytes()

                        getThumbnails(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)).forEach {
                            val stream = ByteArrayOutputStream()
                            it.value.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                            uploadThumbnail(stream.toByteArray(), folder, fileId, contentType, it.key)
                        }
                    }
                }

                onSuccess()
            }
        }
    }

    private suspend fun uploadThumbnail(bytes: ByteArray, folder: File, fileId: UUID, contentType: String, resolution: Resolution) {
        val thumbnailId = UUID.randomUUID()
        val tmpFile = File.createTempFile(fileId.toString(), null, folder)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(bytes.inputStream(), tmpFile.outputStream(), secretKey)

        val chunks = getChunks(tmpFile, folder)

        AppState.customer.value?.wallet?.let { wallet ->
            eventPublisher.publishEvent(
                EventThumbnailUploaded(
                    thumbnailId,
                    chunks.map { it.id },
                    chunks.map { it.viewId },
                    resolution,
                    fileId,
                    contentType,
                    Base64.getEncoder().encodeToString(secretKey.encoded)
                )
            )

            fileRepository.upload(wallet, chunks)
        }
    }

    private fun getThumbnails(original: Bitmap): Map<Resolution, Bitmap> = Resolution.values().associate {
        val ratio = it.value.toDouble() / original.height

        it to ThumbnailUtils.extractThumbnail(original, (original.width * ratio).roundToInt(), (original.height * ratio).roundToInt())
    }

    private fun getChunks(original: File, folder: File): List<Chunk> {
        val sizeBytes = original.length()

        if (sizeBytes <= CHUNK_SIZE_BYTES) {
            val sizeFirst = (0..sizeBytes).random()
            val sizeSecond = sizeBytes - sizeFirst

            val chunkFirst = chunk(original, folder, 0, sizeFirst)
            val chunkSecond = chunk(original, folder, sizeFirst, sizeSecond)

            return listOf(chunkFirst, chunkSecond)
        }

        val chunksCount = ceil(sizeBytes / CHUNK_SIZE_BYTES.toDouble()).toInt()
        var idealChunkSize = sizeBytes / chunksCount
        var lastPosition = 0L

        return (0..chunksCount.minus(1)).map { chunkIndex ->
            if (chunkIndex + 1 == chunksCount) {
                chunk(original, folder, lastPosition, sizeBytes - lastPosition)
            } else {
                val size = (0..(2 * idealChunkSize)).random()

                val chunk = chunk(original, folder, lastPosition, size)

                lastPosition += size
                idealChunkSize = (sizeBytes - lastPosition) / (chunksCount - (chunkIndex + 1))

                chunk
            }
        }
    }

    private fun chunk(original: File, outputFolder: File, start: Long, length: Long): Chunk {
        val id = UUID.randomUUID()
        val viewId = UUID.randomUUID()
        val file = File.createTempFile(id.toString(), null, outputFolder)

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