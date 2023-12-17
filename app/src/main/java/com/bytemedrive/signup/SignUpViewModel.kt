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
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID

class SignUpViewModel(
    private val signUpRepository: SignUpRepository,
    private val walletRepository: WalletRepository,
    private val eventPublisher: EventPublisher,
    private val signInManager: SignInManager
) : ViewModel() {

    private val TAG = SignUpViewModel::class.qualifiedName

    val username = MutableStateFlow("")

    val password = MutableStateFlow(charArrayOf())

    val passwordConfirm = MutableStateFlow(charArrayOf())

    val termsAndConditions = MutableStateFlow(false)

    fun signUp(context: Context, onFailure: () -> Job) = effect {
        val username = username.value.trim()
        val password = password.value
        val usernameSha3 = ShaService.hashSha3(username)

        try {
            val credentialsSha3 = ShaService.hashSha3("${username}:${password.concatToString()}")
            val eventsSecretKey = AesService.generateNewEventsSecretKey()
            val encryptedEventsSecretKey = AesService.encryptWithPassword(eventsSecretKey.encoded, password, usernameSha3.toByteArray(StandardCharsets.UTF_8))
            val aesKey = EncryptedSecretKey(UUID.randomUUID(), EncryptionAlgorithm.AES256, String(Base64.getEncoder().encode(encryptedEventsSecretKey), StandardCharsets.UTF_8))
            val customerSignUp = CustomerSignUp(credentialsSha3, aesKey)
            val walletId = UUID.randomUUID()

            signUpRepository.signUp(usernameSha3, customerSignUp)
            walletRepository.createWallet(walletId)
            signInManager.signInSuccess(username, credentialsSha3, EventsSecretKey(aesKey.id, aesKey.algorithm, eventsSecretKey), context)

            val eventSignUp = EventCustomerSignedUp(username, walletId, ZonedDateTime.now())

            eventPublisher.publishEvent(eventSignUp)
        } catch (exception: UnknownHostException) {
           throw exception
        } catch (exception: Exception) {
            onFailure()
            Log.e(TAG, "Signup failed for username: $username", exception)
        }
    }

    fun validateForm(): String = when {
        (username.value.length !in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH) -> "Username should be from 1 to 64 characters"
        (username.value.contains(" ")) -> "Username should be without spaces"
        (password.value.size < PASSWORD_MIN_LENGTH) -> "Password should have at least 8 characters"
        (!password.value.contentEquals(passwordConfirm.value)) -> "Passwords do not match. Try again"
        (!termsAndConditions.value) -> "Terms and conditions are required"
        else -> ""
    }

    private fun effect(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }

    companion object {

        private const val USERNAME_MIN_LENGTH = 1
        private const val USERNAME_MAX_LENGTH = 64
        private const val PASSWORD_MIN_LENGTH = 8
    }
}