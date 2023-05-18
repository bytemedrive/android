package com.bytemedrive.file.root

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID
import kotlin.math.roundToInt

class UploadViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher,
) : ViewModel() {

    private val TAG = UploadViewModel::class.qualifiedName

    fun uploadFile(bytes: ByteArray, fileName: String, folderId: String?, contentType: String, onSuccess: () -> Unit) {
        val secretKey = AesService.generateNewFileSecretKey()
        val fileEncrypted = AesService.encryptWithKey(bytes, secretKey)
        val fileBase64 = Base64.getEncoder().encodeToString(fileEncrypted)
        val fileId = UUID.randomUUID()
        val chunkId = UUID.randomUUID() // TODO: for now we have one chunk (split will be implemented later)

        viewModelScope.launch {
            AppState.customer.value?.wallet?.let { wallet ->
                fileRepository.upload(FileUpload(chunkId, fileBase64, wallet))
                eventPublisher.publishEvent(
                    EventFileUploaded(
                        fileId,
                        listOf(chunkId),
                        fileName,
                        bytes.size.toLong(),
                        ShaService.hashSha1(bytes),
                        contentType,
                        Base64.getEncoder().encodeToString(secretKey.encoded),
                        false,
                        folderId?.let { UUID.fromString(folderId) }
                    )
                )

                when (contentType) {
                    MimeTypes.IMAGE_JPEG -> {
                        getThumbnails(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)).forEach {
                            val stream = ByteArrayOutputStream()
                            it.value.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            val bytesThumbnail = stream.toByteArray()

                            uploadThumbnail(bytesThumbnail, fileId, contentType, it.key)
                        }
                    }
                }

                onSuccess()
            }
        }
    }

    private suspend fun uploadThumbnail(bytes: ByteArray, fileId: UUID, contentType: String, resolution: Resolution) {
        val secretKey = AesService.generateNewFileSecretKey()
        val fileEncrypted = AesService.encryptWithKey(bytes, secretKey)
        val fileBase64 = Base64.getEncoder().encodeToString(fileEncrypted)
        val thumbnailId = UUID.randomUUID()
        val chunkId = UUID.randomUUID() // TODO: for now we have one chunk (split will be implemented later)

        AppState.customer.value?.wallet?.let { wallet ->
            Log.i(TAG, "Uploading thumbnail with resolution ${resolution.value}. Chunk id=${chunkId}")
            eventPublisher.publishEvent(
                EventThumbnailUploaded(
                    thumbnailId,
                    listOf(chunkId),
                    resolution,
                    fileId,
                    contentType,
                    Base64.getEncoder().encodeToString(secretKey.encoded)
                )
            )

            fileRepository.upload(FileUpload(chunkId, fileBase64, wallet))
        }
    }

    private fun getThumbnails(original: Bitmap): Map<Resolution, Bitmap> = Resolution.values().associate {
        val ratio = it.value.toDouble() / original.height

        it to ThumbnailUtils.extractThumbnail(original, (original.width * ratio).roundToInt(), (original.height * ratio).roundToInt())
    }
}