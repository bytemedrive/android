package com.bytemedrive.file.root

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes.*
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.Base64
import java.util.UUID
import kotlin.math.roundToInt

class UploadViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
    private val fileManager: FileManager,
) : ViewModel() {

    private val TAG = UploadViewModel::class.qualifiedName

    fun uploadFile(inputStream: InputStream, tmpFolder: File, fileName: String, folderId: String?, contentType: String, onSuccess: () -> Unit) {
        val fileId = UUID.randomUUID()
        val tmpOriginalFile = File.createTempFile(fileId.toString(), null, tmpFolder)
        inputStream.copyTo(tmpOriginalFile.outputStream(), FileManager.BUFFER_SIZE)

        val tmpEncryptedFile = File.createTempFile("$fileId-encrypted", null, tmpFolder)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(tmpOriginalFile.inputStream(), tmpEncryptedFile.outputStream(), secretKey)

        val chunks = fileManager.getChunks(tmpEncryptedFile, tmpFolder)

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
                        ShaService.checksum(tmpOriginalFile.inputStream()),
                        contentType,
                        Base64.getEncoder().encodeToString(secretKey.encoded),
                        false,
                        folderId?.let { UUID.fromString(folderId) }
                    )
                )

                when (contentType) {
                    IMAGE_JPEG -> {
                        val bytes = tmpOriginalFile.readBytes()

                        getThumbnails(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)).forEach {
                            val stream = ByteArrayOutputStream()
                            it.value.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                            uploadThumbnail(stream.toByteArray(), tmpFolder, fileId, contentType, it.key)
                        }
                    }
                }

                onSuccess()
            }
        }
    }

    private suspend fun uploadThumbnail(bytes: ByteArray, folder: File, fileId: UUID, contentType: String, resolution: Resolution) {
        val thumbnailId = UUID.randomUUID()
        val tmpEncryptedFile = File.createTempFile("$thumbnailId-encrypted", null, folder)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(bytes.inputStream(), tmpEncryptedFile.outputStream(), secretKey)

        val chunks = fileManager.getChunks(tmpEncryptedFile, folder)

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

    private fun getThumbnails(original: Bitmap): Map<Resolution, Bitmap> = Resolution.values().associateWith {
        val ratio = it.value.toDouble() / original.height

        ThumbnailUtils.extractThumbnail(original, (original.width * ratio).roundToInt(), (original.height * ratio).roundToInt())
    }
}