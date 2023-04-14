package com.bytemedrive.store

import android.content.Context
import android.util.Log
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import java.util.Base64

class EventSyncService(private val storeRepository: StoreRepository) {


    fun addEvent(eventObjectWrapper: EventObjectWrapper, context: Context) {
        // TODO store in encrypted preferences
        AppState.events.add(eventObjectWrapper)
        AppState.customer.value?.let { eventObjectWrapper.data.convert(it) }
    }

    suspend fun syncEvents(context: Context) {
        Log.d("com.bytemedrive.store", "Events sync start")
        val knownEvents = AppState.events
        val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
        val usernameSha3 = EncryptedPrefs.getInstance(context).getUsername()?.let { ShaService.hashSha3(it) }

        if (credentialsSha3 != null && usernameSha3 != null) {
            Log.d("com.bytemedrive.store", "Events sync for usernameSha3: $usernameSha3")
            storeRepository.getEncryptedEvents(usernameSha3, credentialsSha3, knownEvents.size).stream()
                .filter { shouldBeApplied(it, knownEvents) }
                .map {
                    // TODO in the future there will be possible more keys than one
                    val secretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(it.keys[0])
                    if (secretKey != null) {
                        val eventBytes = AesService.decryptWithKey(Base64.getDecoder().decode(it.eventDataBase64), secretKey.getSecretKey())
                        val eventMapWrapper = StoreJsonConfig.mapper.readValue(eventBytes, EventMapWrapper::class.java)
                        val dataAsJson = StoreJsonConfig.mapper.writeValueAsString(eventMapWrapper.data)
                        val eventData = StoreJsonConfig.mapper.readValue(dataAsJson, eventMapWrapper.eventType.clazz)
                        val eventObjectWrapper = EventObjectWrapper(eventMapWrapper.id, eventMapWrapper.eventType, eventMapWrapper.publishedAt, eventData as Convertable)

                        addEvent(eventObjectWrapper, context)
                    }

                }
        }
    }

    private fun shouldBeApplied(event: EncryptedEvent, knownEvents: List<EventObjectWrapper>): Boolean {
        for (knownEvent in knownEvents) {
            if (knownEvent.id == event.id) {
                return false
            }
        }
        return true
    }


}