package com.bytemedrive.store

import android.content.Context
import android.util.Log
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import okhttp3.internal.immutableListOf
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID

class EventPublisher(private val storeRepository: StoreRepository, private val eventSyncService: EventSyncService) {

    suspend fun publishEvent(event: Convertable, context: Context) {
        val usernameSha3 = EncryptedPrefs.getInstance(context).getUsername()?.let { ShaService.hashSha3(it) }
        val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
        val eventsSecretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(EncryptionAlgorithm.AES256)

        if (eventsSecretKey != null && eventsSecretKey != null && usernameSha3 != null && credentialsSha3 != null) {
            val eventType = EventType.of(event.javaClass)
            val eventWrapper = EventObjectWrapper(UUID.randomUUID(), eventType, ZonedDateTime.now(), event)
            Log.i("com.bytemedrive.store", "Publishing event ${eventWrapper.eventType} with id: ${eventWrapper.id}")
            val jsonWrapperData = StoreJsonConfig.mapper.writeValueAsBytes(eventWrapper)
            val jsonWrapperEncrypted = AesService.encryptWithKey(jsonWrapperData, eventsSecretKey.getSecretKey())
            val jsonWrapperEncryptedBase64 = Base64.getEncoder().encodeToString(jsonWrapperEncrypted)
            val encryptedEvent = EncryptedEvent(eventWrapper.id, immutableListOf(eventsSecretKey.id), jsonWrapperEncryptedBase64, ZonedDateTime.now())
            storeRepository.storeEncryptedEvent(usernameSha3, credentialsSha3, encryptedEvent)
            eventSyncService.addEvents(context, eventWrapper)
        }
    }
}