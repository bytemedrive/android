package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.MasterKeys
import com.bytemedrive.customer.Customer
import com.bytemedrive.customer.CustomerConverter
import com.bytemedrive.event.Event
import com.bytemedrive.event.EventRepository
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Base64

class SignInViewModel(private val eventRepository: EventRepository) : ViewModel() {

    private var _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private var _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun signIn(context: Context, onFailure: () -> Job) = effect {
        val username = _username.value
        val password = _password.value.toCharArray()

        val data = eventRepository.fetch(ShaService.hashSha3(username))

        if (data.isEmpty()) {
            onFailure()
        } else {
            try {
                data.forEach {
                    val eventEncrypted = Base64.getDecoder().decode(it)
                    val eventBytes = AesService.decrypt(eventEncrypted, password, ShaService.hashSha3(username).encodeToByteArray())
                    val event = jacksonObjectMapper().readValue(String(eventBytes), Event::class.java)

                    CustomerConverter.convert(event)
                }


                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                EncryptedStorage.initializeSharedPreferences(context, masterKeyAlias)
                EncryptedStorage.saveCustomerPassword(password)

                Customer.setUsername(username)
            } catch (exception: Exception) {
                onFailure()
            }
        }
    }

    fun validateForm(): String? =
        when {
            (_username.value.isEmpty()) -> "Username is required"
            (_password.value.isEmpty()) -> "Password is required"

            else -> {
                null
            }
        }

    fun setUsername(value: String) {
        _username.value = value
    }

    fun setPassword(value: String) {
        _password.value = value
    }

    private fun effect(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }
}