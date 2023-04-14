package com.bytemedrive.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.store.EncryptedSecretKey
import com.bytemedrive.store.EncryptionAlgorithm
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.store.EventsSecretKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID

class SignUpViewModel(private val signUpRepository: SignUpRepository, private val eventPublisher: EventPublisher, private val signInManager: SignInManager) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _passwordConfirm = MutableStateFlow("")
    val passwordConfirm: StateFlow<String> = _passwordConfirm

    private val _termsAndConditions = MutableStateFlow(false)
    val termsAndConditions: StateFlow<Boolean> = _termsAndConditions

    fun signUp(context: Context, onFailure: () -> Job) = effect {
        val username = _username.value.trim()
        val password = _password.value.toCharArray()
        val usernameSha3 = ShaService.hashSha3(username)
        try {
            val credentialsSha3 = ShaService.hashSha3("${username}:${password.concatToString()}")
            val eventsSecretKey = AesService.generateNewEventsSecretKey()
            val encryptedEventsSecretKey = AesService.encryptWithPassword(eventsSecretKey.encoded, password, usernameSha3.toByteArray(StandardCharsets.UTF_8))
            val aesKey = EncryptedSecretKey(UUID.randomUUID(), EncryptionAlgorithm.AES256, Base64.getEncoder().encodeToString(encryptedEventsSecretKey))
            val customerSignUp = CustomerSignUp(credentialsSha3, aesKey)
            signUpRepository.signUp(usernameSha3, customerSignUp)
            signInManager.signInSuccess(username, credentialsSha3, EventsSecretKey(aesKey.id, aesKey.algorithm, eventsSecretKey), context)
            val eventSignUp = EventCustomerSignedUp(username, UUID.randomUUID(), ZonedDateTime.now())
            eventPublisher.publishEvent(eventSignUp, context)
        } catch (exception: Exception) {
            onFailure()
            Log.e("com.bytemedrive.signup", "Signup failed for username: $username", exception)
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