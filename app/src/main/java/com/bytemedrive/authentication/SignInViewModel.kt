package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.MasterKeys
import com.bytemedrive.customer.Customer
import com.bytemedrive.event.EventRepository
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import kotlinx.coroutines.launch

class SignInViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun signIn(context: Context, username: String, password: CharArray) {

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedStorage.initializeSharedPreferences(context, masterKeyAlias)
        EncryptedStorage.saveCustomerPassword(password)

        Customer.username = username

        viewModelScope.launch {
            // TODO: load events from BE -> decrypt
            val data = eventRepository.fetch(ShaService.hashSha3(username))
        }
    }
}