package com.bytemedrive.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Base64
import java.util.UUID

class UploadViewModel(
    private val fileRepository: FileRepository,
    private val eventPublisher: EventPublisher
) : ViewModel() {

    fun uploadFile(bytes: ByteArray, fileName: String, folderId: String?, contentType: String, onSuccess: () -> Unit) {

        val secretKey = AesService.generateNewFileSecretKey()
        val fileEncrypted = AesService.encryptWithKey(bytes, secretKey)
        val fileBase64 = Base64.getEncoder().encodeToString(fileEncrypted)
        val fileId = UUID.randomUUID()
        val chunkId = UUID.randomUUID() // TODO: for now we have one chunk (split will be implemented later)

        viewModelScope.launch {
            AppState.customer.value!!.folders.find { it.id.toString() == folderId }.let { folder ->
                AppState.customer.value?.wallet?.let { wallet ->
                    eventPublisher.publishEvent(
                        EventFileUploaded(
                            fileId,
                            listOf(chunkId),
                            fileName,
                            bytes.size.toLong(),
                            ShaService.hashSha1(bytes),
                            contentType,
                            Base64.getEncoder().encodeToString(secretKey.encoded),
                            folder?.id
                        )
                    )
                    fileRepository.upload(FileUpload(chunkId, fileBase64, wallet))
                    onSuccess()
                }
            }
        }
    }
}