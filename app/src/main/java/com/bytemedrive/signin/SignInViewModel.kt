package com.bytemedrive.signin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.CustomerAggregate
import com.bytemedrive.store.EncryptedPrefs
import com.bytemedrive.store.EventsSecretKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.Base64

class SignInViewModel(private val signInRepository: SingInRepository) : ViewModel() {

    private var _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private var _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun signIn(context: Context, onFailure: () -> Job) = effect {
        val username = _username.value.trim()
        val password = _password.value.toCharArray()

        val usernameSha3 = ShaService.hashSha3(username)
        val credentialsSha3 = ShaService.hashSha3("${username}:${password.concatToString()}")
        try {
            val privateKeys = signInRepository.getPrivateKeys(usernameSha3, credentialsSha3)
            if (privateKeys.isEmpty()) {
                onFailure()
            } else {
                EncryptedPrefs.getInstance(context).storeUsername(username)
                EncryptedPrefs.getInstance(context).storeCredentialsSha3(credentialsSha3)
                privateKeys.stream().map {
                    val secretKeyAsBytes = AesService.decryptWithPassword(Base64.getDecoder().decode(it.keyBase64), password, usernameSha3.toByteArray(StandardCharsets.UTF_8))
                    EncryptedPrefs.getInstance(context).storeEventsSecretKey(EventsSecretKey(it.id, it.algorithm, Base64.getEncoder().encodeToString(secretKeyAsBytes)))
                }
                AppState.loginSuccess()
            }
        } catch (exception: Exception){
            onFailure()
            Log.e("com.bytemedrive.signin", "Signip failed for username: $username", exception)
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