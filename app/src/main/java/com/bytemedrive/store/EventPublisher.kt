package com.bytemedrive.store

import android.content.Context
import com.bytemedrive.event.EventType
import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.HttpClient
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import okhttp3.internal.immutableListOf
import java.time.ZonedDateTime
import java.util.Base64
import java.util.UUID

class EventPublisher(private val httpClient: HttpClient) {

    suspend fun publishEvent(event: Convertable, context: Context) {
        val usernameSha3 = EncryptedPrefs.getInstance(context).getUsername()?.let { ShaService.hashSha3(it) }
        val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
        val eventsSecretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(EncryptionAlgorithm.AES256)

        if (eventsSecretKey != null && eventsSecretKey != null && usernameSha3 != null && credentialsSha3 != null) {
            val eventType = EventType.of(event.javaClass)
            val eventWrapper = EventObjectWrapper(UUID.randomUUID(), eventType, ZonedDateTime.now(), event)
            val jsonWrapperData = jacksonObjectMapper().writeValueAsBytes(eventWrapper)
            val jsonWrapperEncrypted = AesService.encryptWithKey(jsonWrapperData, eventsSecretKey.getSecretKey())
            val jsonWrapperEncryptedBase64 = Base64.getEncoder().encodeToString(jsonWrapperEncrypted)
            val encryptedEvent = EncryptedEvent(eventWrapper.id, immutableListOf(eventsSecretKey.id), jsonWrapperEncryptedBase64, ZonedDateTime.now())
            httpClient.create(credentialsSha3).post(Endpoint.EVENTS.buildUrl(usernameSha3)) { setBody(encryptedEvent) }
        }
    }
}