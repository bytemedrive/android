package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.MasterKeys
import com.bytemedrive.customer.Customer
import com.bytemedrive.customer.EventCustomerSignedUp
import com.bytemedrive.event.Event
import com.bytemedrive.event.EventRepository
import com.bytemedrive.event.EventType
import com.bytemedrive.event.EventsRequest
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

class SignUpViewModel(private val eventRepository: EventRepository) : ViewModel() {

    private var _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private var _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private var _passwordConfirm = MutableStateFlow("")
    val passwordConfirm: StateFlow<String> = _passwordConfirm

    private var _termsAndConditions = MutableStateFlow(false)
    val termsAndConditions: StateFlow<Boolean> = _termsAndConditions

    fun signUp(context: Context, onFailure: () -> Job) = effect {
        val username = _username.value
        val eventPassword = _password.value.toCharArray()
        val data = eventRepository.fetch(ShaService.hashSha3(username))

        if (data.isNotEmpty()) {
            onFailure()
        } else {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedStorage.initializeSharedPreferences(context, masterKeyAlias)

            val event = Event(EventType.CUSTOMER_SIGNED_UP, EventCustomerSignedUp(username))
            val eventSalt = ShaService.hashSha3(username).toByteArray().copyOfRange(0, 16)

            val eventBytes = jacksonObjectMapper().writeValueAsBytes(event)
            val eventEncrypted = AesService.encrypt(eventBytes, eventPassword, eventSalt)
            val eventBase64 = Base64.getEncoder().encodeToString(eventEncrypted)

            eventRepository.upload(ShaService.hashSha3(username), EventsRequest(eventBase64))

            EncryptedStorage.saveCustomerPassword(eventPassword)

            Customer.setUsername(username)
        }
    }

    fun validateForm(): String? = when {
        (_username.value.length !in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH) -> "Username should be from 1 to 64 characters"
        (_username.value.contains(" ")) -> "Username should be without spaces"
        (_password.value.length < PASSWORD_MIN_LENGTH) -> "Password should have at least 8 characters"
        (_password.value != _passwordConfirm.value) -> "Passwords do not match. Try again"
        (!_termsAndConditions.value) -> "Terms and conditions are required"

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

    fun setPasswordConfirm(value: String) {
        _passwordConfirm.value = value
    }

    fun setTermsAndConditions(value: Boolean) {
        _termsAndConditions.value = value
    }

    private fun effect(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }

    companion object {

        private const val USERNAME_MIN_LENGTH = 1
        private const val USERNAME_MAX_LENGTH = 64
        private const val PASSWORD_MIN_LENGTH = 8
    }
}