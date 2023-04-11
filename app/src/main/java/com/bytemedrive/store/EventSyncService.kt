package com.bytemedrive.store

import android.content.Context
import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.HttpClient
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.ShaService
import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.util.Base64

class EventSyncService(private val httpClient: HttpClient) {

    suspend fun syncEvent(context: Context) {
        val knownEvents = AppState.events
        val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
        val usernameSha3 = EncryptedPrefs.getInstance(context).getUsername()?.let { ShaService.hashSha3(it) }

        if (credentialsSha3 != null && usernameSha3 != null) {
            val encryptedEvents: List<EncryptedEvent> = httpClient.create(credentialsSha3).get(Endpoint.EVENTS_WITH_OFFSET.buildUrl(usernameSha3, knownEvents.size)).body()

            encryptedEvents.stream()
                .filter { shouldBeApplied(it, knownEvents) }
                .map {
                    // TODO in the future there will be possible more keys than one
                    val secretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(it.keys[0])
                    if (secretKey != null) {
                        val eventBytes = AesService.decryptWithKey(Base64.getDecoder().decode(it.eventDataBase64), secretKey.getSecretKey())
                        val eventMapWrapper = jsonMapper().readValue(eventBytes, EventMapWrapper::class.java)
                        val dataAsJson = jsonMapper().writeValueAsString(eventMapWrapper.data)
                        val eventData = jsonMapper().readValue(dataAsJson, eventMapWrapper.eventType.clazz)
                        val eventObjectWrapper = EventObjectWrapper(eventMapWrapper.id, eventMapWrapper.eventType, eventMapWrapper.publishedAt, eventData as Convertable)
                        if (AppState.customer.value == null) {
                            AppState.customer.value = CustomerAggregate()
                        }
                        AppState.events.add(eventObjectWrapper)
                        AppState.customer.value?.let { eventData.convert(it) }
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