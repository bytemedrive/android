package com.bytemedrive.file.root

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes.IMAGE_JPEG
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.Dispatchers
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

    fun uploadFile(
        inputStream: InputStream,
                   tmpFolder: File,
                   fileName: String,
                   folderId: String?,
                   contentType: String,
        onStart: (id: UUID, name: String) -> Unit,
                   onSuccess: (id: UUID) -> Unit
    ) {
        val dataFileId = UUID.randomUUID()

        onStart(dataFileId, fileName)

        val tmpOriginalFile = File.createTempFile(dataFileId.toString(), null, tmpFolder)
        inputStream.copyTo(tmpOriginalFile.outputStream(), FileManager.BUFFER_SIZE)

        viewModelScope.launch(Dispatchers.IO) {
            val tmpEncryptedFile = File.createTempFile("$dataFileId-encrypted", null, tmpFolder)

            val secretKey = AesService.generateNewFileSecretKey()
            AesService.encryptWithKey(tmpOriginalFile.inputStream(), tmpEncryptedFile.outputStream(), secretKey)

            val chunks = fileManager.getChunks(tmpEncryptedFile, tmpFolder)

            AppState.customer.value?.wallet?.let { wallet ->
                fileRepository.upload(wallet, chunks)
                eventPublisher.publishEvent(
                    EventFileUploaded(
                        dataFileId,
                        chunks.map { it.id },
                        chunks.map { it.viewId },
                        fileName,
                        tmpEncryptedFile.length(),
                        ShaService.checksum(tmpOriginalFile.inputStream()),
                        contentType,
                        Base64.getEncoder().encodeToString(secretKey.encoded),
                        UUID.randomUUID(),
                        folderId?.let { UUID.fromString(folderId) }
                    )
                )

                when (contentType) {
                    IMAGE_JPEG -> {
                        val bytes = tmpOriginalFile.readBytes()

                        getThumbnails(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)).forEach {
                            val stream = ByteArrayOutputStream()
                            it.value.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                            uploadThumbnail(stream.toByteArray(), tmpFolder, dataFileId, contentType, it.key)
                        }
                    }
                }

                onSuccess(dataFileId)
            }
        }
    }

    private suspend fun uploadThumbnail(bytes: ByteArray, file: File, sourceDataFileId: UUID, contentType: String, resolution: Resolution) {
        val thumbnailDataFileId = UUID.randomUUID()
        val tmpEncryptedFile = File.createTempFile("$thumbnailDataFileId-encrypted", null, file)

        val secretKey = AesService.generateNewFileSecretKey()
        AesService.encryptWithKey(bytes.inputStream(), tmpEncryptedFile.outputStream(), secretKey)

        val chunks = fileManager.getChunks(tmpEncryptedFile, file)

        AppState.customer.value?.wallet?.let { wallet ->
            eventPublisher.publishEvent(
                EventFileUploaded(
                    thumbnailDataFileId,
                    chunks.map { it.id },
                    chunks.map { it.viewId },
                    "thumbnail.jpg",
                    tmpEncryptedFile.length(),
                    ShaService.checksum(bytes.inputStream()),
                    contentType,
                    Base64.getEncoder().encodeToString(secretKey.encoded),
                    null,
                    null
                )
            )
            eventPublisher.publishEvent(
                EventThumbnailUploaded(
                    sourceDataFileId,
                    thumbnailDataFileId,
                    resolution,
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