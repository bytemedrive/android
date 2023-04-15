package com.bytemedrive.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.launch
import java.util.Base64
import java.util.UUID

class UploadViewModel(private val fileRepository: FileRepository, private val eventPublisher: EventPublisher) : ViewModel() {

    fun uploadFile(bytes: ByteArray, fileName: String, contentType: String) {

        val secretKey = AesService.generateNewFileSecretKey()
        val fileEncrypted = AesService.encryptWithKey(bytes, secretKey)
        val fileBase64 = Base64.getEncoder().encodeToString(fileEncrypted)
        val fileId = UUID.randomUUID()
        val chunkId = UUID.randomUUID() // TODO: for now we have one chunk (split will be implemented later)

        viewModelScope.launch {
            fileRepository.upload(FileUpload(chunkId.toString(), fileBase64))
            eventPublisher.publishEvent(
                EventFileUploaded(
                    fileId,
                    listOf(chunkId),
                    fileName,
                    bytes.size.toLong(),
                    ShaService.hashSha1(bytes),
                    contentType,
                    Base64.getEncoder().encodeToString(secretKey.encoded)
                )
            )
        }
    }
}