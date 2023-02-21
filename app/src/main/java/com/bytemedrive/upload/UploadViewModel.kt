package com.bytemedrive.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.event.EventRepository
import com.bytemedrive.event.EventsRequest
import com.bytemedrive.privacy.AesService
import kotlinx.coroutines.launch

class UploadViewModel(private val eventRepository: EventRepository, private val aesService: AesService) : ViewModel() {
    val salt = "dummmySaltToByte".toByteArray()
    val password: String = "dummyPassword"

    fun uploadFile(bytes: ByteArray) {
//        val encryptedFile = aesService.encrypt(bytes, password, salt)

        // send file to BE
        // TODO: encrypt with symmetric encryption and generated random password -> convert to base64 -> upload to BE
        // TODO: Create event EventFileUploaded -> encrypt with symmetric encryption and user password (saved in storage since login - not ready) -> send to BE POST http://localhost:8080/api/customers/{customerIdHash}/events

        viewModelScope.launch {
            eventRepository.upload("hashed user's email", EventsRequest("fooo")) // todo sends encrypted event
//            fileRepository.upload() todo sends encrypted file to BE  POST http://localhost:8080/api/files

        }
    }
}