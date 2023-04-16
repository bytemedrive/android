package com.bytemedrive.store

import android.util.Log
import com.bytemedrive.encryptedSharedPreferences
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import okhttp3.internal.immutableListOf
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID

class EventPublisher(private val storeRepository: StoreRepository, private val eventSyncService: EventSyncService) {

    suspend fun publishEvent(event: Convertable) {
        val usernameSha3 = encryptedSharedPreferences.username?.let { ShaService.hashSha3(it) }
        val credentialsSha3 = encryptedSharedPreferences.credentialsSha3
        val eventsSecretKey = encryptedSharedPreferences.getEventsSecretKey(EncryptionAlgorithm.AES256)

        if (eventsSecretKey != null && usernameSha3 != null && credentialsSha3 != null) {
            val eventType = EventType.of(event.javaClass)
            val eventWrapper = EventObjectWrapper(UUID.randomUUID(), eventType, ZonedDateTime.now(), event)
            Log.i("com.bytemedrive.store", "Publishing event ${eventWrapper.eventType} with id: ${eventWrapper.id}")
            val jsonWrapperData = StoreJsonConfig.mapper.writeValueAsBytes(eventWrapper)
            val jsonWrapperEncrypted = AesService.encryptWithKey(jsonWrapperData, eventsSecretKey.getSecretKey())
            val jsonWrapperEncryptedBase64 = Base64.getEncoder().encodeToString(jsonWrapperEncrypted)
            val encryptedEvent = EncryptedEvent(eventWrapper.id, immutableListOf(eventsSecretKey.id), jsonWrapperEncryptedBase64, ZonedDateTime.now())
            storeRepository.storeEncryptedEvent(usernameSha3, credentialsSha3, encryptedEvent)
            eventSyncService.addEvents(eventWrapper)
        }
    }
}