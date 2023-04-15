package com.bytemedrive.store

import android.util.Log
import com.bytemedrive.MainActivity.Companion.encryptedSharedPreferences
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import java.util.Base64
import kotlin.streams.toList

class EventSyncService(private val storeRepository: StoreRepository) {


    suspend fun syncEvents() {
        Log.d("com.bytemedrive.store", "Events sync start")
        val credentialsSha3 = encryptedSharedPreferences?.getCredentialsSha3()
        val usernameSha3 = encryptedSharedPreferences?.getUsername()?.let { ShaService.hashSha3(it) }
        val offset = encryptedSharedPreferences?.getEventsCount()

        if (credentialsSha3 != null && usernameSha3 != null && offset != null) {
            Log.d("com.bytemedrive.store", "Events sync for usernameSha3: $usernameSha3")
            val newEvents: Array<EventObjectWrapper> = storeRepository.getEncryptedEvents(usernameSha3, credentialsSha3, offset).stream()
                .map {
                    // TODO in the future there will be possible more keys than one
                    val secretKey = encryptedSharedPreferences?.getEventsSecretKey(it.keys[0])!!
                    val eventBytes = AesService.decryptWithKey(Base64.getDecoder().decode(it.eventDataBase64), secretKey.getSecretKey())
                    val eventMapWrapper = StoreJsonConfig.mapper.readValue(eventBytes, EventMapWrapper::class.java)
                    eventMapWrapper.toEventObjectWrapper()
                }.toList()
                .toTypedArray()
            if (newEvents.isNotEmpty()) {
                Log.i("com.bytemedrive.store", "New events of ${newEvents.size} count was fetch via auto sync.")
                addEvents(*newEvents)
            }
        }
    }


    fun addEvents(vararg events: EventObjectWrapper) {
        var allEvents = encryptedSharedPreferences?.storeEvent(*events)
        if (!allEvents.isNullOrEmpty()) {
            val customer = CustomerAggregate()
            allEvents.stream().forEach { it.data.convert(customer) }
            Log.i("com.bytemedrive.store", "Customer refreshed.")
            AppState.customer.value = customer
            AppState.authorized.value = true
        }
    }
}