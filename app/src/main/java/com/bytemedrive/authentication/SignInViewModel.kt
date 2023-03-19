package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.event.EventRepository
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import kotlinx.coroutines.launch

class SignInViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun signIn(context: Context, email: String, password: CharArray) {
        val salt = ShaService.hashSha3(email).toByteArray()
        val aesKeyFromPassword = AesService.getAESKeyFromPassword(password, salt).encoded.toString()

        EncryptedStorage.initializeSharedPreferences(context, aesKeyFromPassword)
        EncryptedStorage.saveCustomerCredentials(email, password)

        viewModelScope.launch {
            // TODO: load events from BE -> decrypt
            val data = eventRepository.fetch(ShaService.hashSha3(email))
        }
    }
}