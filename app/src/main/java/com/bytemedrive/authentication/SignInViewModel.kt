package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.MasterKeys
import com.bytemedrive.customer.Customer
import com.bytemedrive.event.EventRepository
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.upload.EventFileUploaded
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Base64

class SignInViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun signIn(context: Context, username: String, password: CharArray) {

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedStorage.initializeSharedPreferences(context, masterKeyAlias)
        EncryptedStorage.saveCustomerPassword(password)

        Customer.setUsername(username)

        viewModelScope.launch {
            val data = eventRepository.fetch(ShaService.hashSha3(username))

            data.forEach {
                val eventEncrypted = Base64.getDecoder().decode(it)
                val eventBytes = AesService.decrypt(eventEncrypted, password, ShaService.hashSha3(email).encodeToByteArray())
                val event = Json.decodeFromString<EventFileUploaded>(eventBytes.toString())
            }
        }
    }
}