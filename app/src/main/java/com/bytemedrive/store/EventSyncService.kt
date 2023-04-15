package com.bytemedrive.store

import android.content.Context
import android.util.Log
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import java.util.Base64
import kotlin.streams.toList

class EventSyncService(private val storeRepository: StoreRepository) {


    suspend fun syncEvents(context: Context) {
        Log.d("com.bytemedrive.store", "Events sync start")
        val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
        val usernameSha3 = EncryptedPrefs.getInstance(context).getUsername()?.let { ShaService.hashSha3(it) }

        if (credentialsSha3 != null && usernameSha3 != null) {
            Log.d("com.bytemedrive.store", "Events sync for usernameSha3: $usernameSha3")
            val offset = EncryptedPrefs.getInstance(context).getEventsCount()
            val newEvents: Array<EventObjectWrapper> = storeRepository.getEncryptedEvents(usernameSha3, credentialsSha3, offset).stream()
                .map {
                    // TODO in the future there will be possible more keys than one
                    val secretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(it.keys[0])!!
                    val eventBytes = AesService.decryptWithKey(Base64.getDecoder().decode(it.eventDataBase64), secretKey.getSecretKey())
                    val eventMapWrapper = StoreJsonConfig.mapper.readValue(eventBytes, EventMapWrapper::class.java)
                    eventMapWrapper.toEventObjectWrapper()
                }.toList()
                .toTypedArray()
            if (newEvents.isNotEmpty()) {
                Log.i("com.bytemedrive.store", "New events of ${newEvents.size} count was fetch via auto sync.")
                addEvents(context, *newEvents)
            }
        }
    }


    fun addEvents(context: Context, vararg events: EventObjectWrapper) {
        var allEvents = EncryptedPrefs.getInstance(context).storeEvent(*events)
        val customer = CustomerAggregate()
        allEvents.stream().map { it.data.convert(customer) }
        Log.i("com.bytemedrive.store", "Customer refreshed.")
        AppState.customer.value = customer
    }
}