package com.bytemedrive.upload

import androidx.lifecycle.ViewModel
import com.bytemedrive.file.FileRepository
import com.bytemedrive.store.EventPublisher

class UploadViewModel(private val fileRepository: FileRepository, private val eventPublisher: EventPublisher) : ViewModel() {

    fun uploadFile(bytes: ByteArray, fileName: String, contentType: String) {
/*
        val fileSalt = AesService.getRandomBytes(16)
        val filePassword = AesService.getRandomCharArray(32)
        val fileEncrypted = AesService.encrypt(bytes, filePassword, fileSalt)
        val fileBase64 = Base64.getEncoder().encodeToString(fileEncrypted)
        val fileId = UUID.randomUUID()
        val chunkId = UUID.randomUUID() // TODO: for now we have one chunk (split will be implemented later)

        viewModelScope.launch {
            eventPublisher.publishEvent(EventFileUploaded(fileId, listOf(chunkId), fileName, bytes.size.toLong(), ShaService.hashSha1(bytes), filePassword, contentType))
            fileRepository.upload(FileUpload(fileId.toString(), fileBase64))
            eventRepository.upload(ShaService.hashSha3(username), EventsRequest(eventBase64))
        }*/
    }
}