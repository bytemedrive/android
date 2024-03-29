package com.bytemedrive.signin

import android.content.Context
import android.util.Log
import com.bytemedrive.application.encryptedSharedPreferences
import com.bytemedrive.application.networkStatus
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.service.ServiceManager
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EncryptionAlgorithm
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.store.EventsSecretKey
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SignInManager(
    private val signInRepository: SignInRepository,
    private val walletRepository: WalletRepository,
    private val eventSyncService: EventSyncService,
    private val database: ByteMeDatabase,
    private val serviceManager: ServiceManager
) {

    private val TAG = SignInManager::class.qualifiedName

    private var jobSync: Job? = null
    private var jobPolling: Job? = null

    fun autoSignIn(context: Context) = CoroutineScope(Dispatchers.Default).launch {
        val username = encryptedSharedPreferences.username
        val credentialsSha3 = encryptedSharedPreferences.credentialsSha3
        val eventsSecretKey = encryptedSharedPreferences.getEventsSecretKey(EncryptionAlgorithm.AES256)

        if (username != null && credentialsSha3 != null && eventsSecretKey != null) {
            Log.i(TAG, "Try autologin for username: $username")
            signInSuccess(username, credentialsSha3, eventsSecretKey, context)
        } else {
            Log.i(TAG, "Autologin not possible")
            signOut(context)
        }
    }

    suspend fun signIn(username: String, password: CharArray, context: Context): Boolean {
        try {
            val usernameSha3 = ShaService.hashSha3(username)
            val credentialsSha3 = ShaService.hashSha3("${username}:${password.concatToString()}")
            val privateKeys = signInRepository.getPrivateKeys(usernameSha3, credentialsSha3)

            return if (privateKeys.isEmpty()) {
                signOut(context)

                false
            } else {
                // TODO expecting one key but in future there might be more keys
                val eventsSecretKey = privateKeys[0]
                val secretKeyAsBytes =
                    AesService.decryptBytesWithPassword(Base64.getDecoder().decode(eventsSecretKey.keyBase64), password, usernameSha3.toByteArray(StandardCharsets.UTF_8))
                signInSuccess(
                    username,
                    credentialsSha3,
                    EventsSecretKey(eventsSecretKey.id, eventsSecretKey.algorithm, Base64.getEncoder().encodeToString(secretKeyAsBytes)),
                    context
                )

                true
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Sign in failed for username: $username", exception)
            signOut(context)

            return false
        }
    }

    fun signInSuccess(username: String, credentialsSha3: String, eventsSecretKey: EventsSecretKey, context: Context) {
        try {
            encryptedSharedPreferences.username = username
            encryptedSharedPreferences.credentialsSha3 = credentialsSha3
            encryptedSharedPreferences.storeEventsSecretKey(eventsSecretKey)

            AppState.authorized.update { true }
            startEventAutoSync()
            startPollingData()
            serviceManager.startServices(context)
        } catch (e: Exception) {
            Log.e(TAG, "")
            signOut(context)
        }
    }

    fun signOut(context: Context) = CoroutineScope(Dispatchers.IO).launch {
        jobSync?.cancel()
        jobPolling?.cancel()
        AppState.authorized.update { false }
        database.clearAllTables()
        encryptedSharedPreferences.clean()
        serviceManager.stopServices(context)
    }

    private fun startEventAutoSync() {
        jobSync = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (!networkStatus.connected.value) {
                    continue
                }

                eventSyncService.syncEvents()
                delay(32.seconds)
            }
        }
    }

    private fun startPollingData() {
        jobPolling = CoroutineScope(Dispatchers.IO).launch {
            val username: String? = encryptedSharedPreferences.username

            while (isActive) {
                if (!networkStatus.connected.value) {
                    continue
                }

                val customer = username?.let { database.customerDao().getByUsername(it) }

                if (customer != null) {
                    val balance = customer.walletId?.let { walletRepository.getWallet(it) }?.balanceGbm

                    if (customer.balanceGbm != balance) {
                        database.customerDao().update(customer.copy(balanceGbm = balance))
                    }
                }

                delay(1.minutes)
            }
        }
    }
}