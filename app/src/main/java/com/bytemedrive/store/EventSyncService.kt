package com.bytemedrive.store

import android.util.Log
import com.bytemedrive.application.encryptedSharedPreferences
import com.bytemedrive.network.JsonConfig.mapper
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.wallet.WalletRepository
import java.util.Base64
import kotlin.streams.toList

class EventSyncService(private val storeRepository: StoreRepository, private val walletRepository: WalletRepository) {

    private val TAG = EventSyncService::class.qualifiedName

    suspend fun syncEvents() {
        Log.d(TAG, "Events sync start")

        val credentialsSha3 = encryptedSharedPreferences.credentialsSha3
        val usernameSha3 = encryptedSharedPreferences.username?.let { ShaService.hashSha3(it) }
        val offset = encryptedSharedPreferences.eventsCount

        if (credentialsSha3 != null && usernameSha3 != null) {
            Log.d(TAG, "Events sync for usernameSha3: $usernameSha3")

            val newEvents: Array<EventObjectWrapper> = storeRepository.getEncryptedEvents(usernameSha3, credentialsSha3, offset).stream()
                .map {
                    // TODO in the future there will be possible more keys than one
                    val secretKey = encryptedSharedPreferences.getEventsSecretKey(it.keys[0])!!
                    val eventBytes = AesService.decryptWithKey(Base64.getDecoder().decode(it.eventDataBase64), secretKey.getSecretKey())
                    val eventMapWrapper = mapper.readValue(eventBytes, EventMapWrapper::class.java)
                    eventMapWrapper.toEventObjectWrapper()
                }.toList().toTypedArray()

            if (newEvents.isNotEmpty()) {
                Log.i(TAG, "New events of ${newEvents.size} count was fetch via auto sync.")
                addEvents(*newEvents)
            }
        }
    }

    suspend fun addEvents(vararg events: EventObjectWrapper) {
        val allEvents = encryptedSharedPreferences.storeEvent(*events)

        if (allEvents.isNotEmpty()) {
            val customer = CustomerAggregate()

            allEvents.stream().forEach { it.data.convert(customer) }
            customer.balanceGbm = walletRepository.getWallet(customer.wallet!!).balanceGbm

            Log.i(TAG, "Customer refreshed.")
            AppState.customer.value = customer
            AppState.authorized.value = true
        }
    }
}