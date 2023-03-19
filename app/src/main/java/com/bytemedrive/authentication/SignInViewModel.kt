package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.event.EventRepository
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import kotlinx.coroutines.launch

class SignInViewModel(
    private val eventRepository: EventRepository,
    private val shaService: ShaService,
    private val aesService: AesService,
    private val encryptedStorage: EncryptedStorage
) : ViewModel() {

    fun signIn(context: Context, email: String, password: String) {
        val salt = shaService.hashSha3(email).toByteArray()
        val aesKeyFromPassword = aesService.getAESKeyFromPassword(password, salt).encoded.toString()

        encryptedStorage.initializeSharedPreferences(context, aesKeyFromPassword)
        encryptedStorage.saveCustomerCredentials(email, password)

        viewModelScope.launch {
            // TODO: load events from BE -> decrypt
            val data = eventRepository.fetch(shaService.hashSha3(email))
        }
    }
}