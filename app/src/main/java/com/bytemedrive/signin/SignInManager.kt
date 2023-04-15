package com.bytemedrive.signin

import android.content.Context
import android.util.Log
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.CustomerAggregate
import com.bytemedrive.store.EncryptedPrefs
import com.bytemedrive.store.EncryptionAlgorithm
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.store.EventsSecretKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.time.Duration.Companion.seconds

class SignInManager(private val signInRepository: SignInRepository, private val eventSyncService: EventSyncService) {

    private val TAG = SignInManager::class.qualifiedName

    private var jobSync: Job? = null

    fun autoSignIn(context: Context) {
        val username = EncryptedPrefs.getInstance(context).getUsername()
        val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
        val eventsSecretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(EncryptionAlgorithm.AES256)
        if (username != null && credentialsSha3 != null && eventsSecretKey != null) {
            Log.i(TAG, "Try autologin for username: $username")
            signInSuccess(username, credentialsSha3, eventsSecretKey, context)
        } else {
            Log.i(TAG, "Autologin not possible")
            logout(context)
        }
    }

    suspend fun signIn(username: String, password: CharArray, context: Context): Boolean {
        try {
            val usernameSha3 = ShaService.hashSha3(username)
            val credentialsSha3 = ShaService.hashSha3("${username}:${password.concatToString()}")
            val privateKeys = signInRepository.getPrivateKeys(usernameSha3, credentialsSha3)
            if (privateKeys.isEmpty()) {
                logout(context)
                return false
            } else {
                // TODO expecting one key but in future there might be more keys
                val eventsSecretKey = privateKeys[0]
                val secretKeyAsBytes =
                    AesService.decryptWithPassword(Base64.getDecoder().decode(eventsSecretKey.keyBase64), password, usernameSha3.toByteArray(StandardCharsets.UTF_8))
                signInSuccess(
                    usernameSha3,
                    credentialsSha3,
                    EventsSecretKey(eventsSecretKey.id, eventsSecretKey.algorithm, Base64.getEncoder().encodeToString(secretKeyAsBytes)),
                    context
                )
                return true
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Sign in failed for username: $username", exception)
            logout(context)
            return false
        }
    }


    fun signInSuccess(username: String, credentialsSha3: String, eventsSecretKey: EventsSecretKey, context: Context) {
        EncryptedPrefs.getInstance(context).storeUsername(username)
        EncryptedPrefs.getInstance(context).storeCredentialsSha3(credentialsSha3)
        EncryptedPrefs.getInstance(context).storeEventsSecretKey(eventsSecretKey)
        val events = EncryptedPrefs.getInstance(context).getEvents()
        val customer = CustomerAggregate()
        events.stream().map { it.data.convert(customer) }
        AppState.customer.value = customer
        AppState.authorized.value = true
        startEventAutoSync(context)
    }

    fun logout(context: Context) {
        jobSync?.cancel()
        AppState.customer.value = null
        AppState.authorized.value = false
        EncryptedPrefs.getInstance(context).clean()
    }

    private fun startEventAutoSync(context: Context) {
        jobSync = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                eventSyncService.syncEvents(context)
                delay(32.seconds)
            }
        }
    }
}