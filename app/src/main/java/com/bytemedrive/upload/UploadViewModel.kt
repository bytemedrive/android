package com.bytemedrive.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.customer.Customer
import com.bytemedrive.event.Event
import com.bytemedrive.event.EventRepository
import com.bytemedrive.event.EventType
import com.bytemedrive.event.EventsRequest
import com.bytemedrive.file.FileRepository
import com.bytemedrive.file.FileUpload
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Base64
import java.util.UUID

class UploadViewModel(private val eventRepository: EventRepository, private val fileRepository: FileRepository) : ViewModel() {

    fun uploadFile(bytes: ByteArray, fileName: String, contentType: String?) {
        val username = Customer.username.value!!
        val fileSalt = AesService.getRandomBytes(16)
        val filePassword = AesService.getRandomCharArray(32)
        val fileEncrypted = AesService.encrypt(bytes, filePassword, fileSalt)
        val fileBase64 = Base64.getEncoder().encodeToString(fileEncrypted)
        val fileId = UUID.randomUUID().toString()
        val chunkId = UUID.randomUUID().toString() // TODO: for now we have one chunk (split will be implemented later) 

        val event = Event(EventType.FILE_UPLOADED, EventFileUploaded(fileId, listOf(chunkId), fileName, bytes.size.toLong(), ShaService.hashSha1(bytes), filePassword, contentType))
        val eventSalt = ShaService.hashSha3(username).toByteArray().copyOfRange(0, 16)
        val eventPassword = EncryptedStorage.getCustomerPassword()

        val eventBytes = Gson().toJson(event).encodeToByteArray()
        val eventEncrypted = AesService.encrypt(eventBytes, eventPassword, eventSalt)
        val eventBase64 = Base64.getEncoder().encodeToString(eventEncrypted)

        viewModelScope.launch {
            fileRepository.upload(FileUpload(fileId, fileBase64))
            eventRepository.upload(ShaService.hashSha3(username), EventsRequest(eventBase64))
        }
    }
}