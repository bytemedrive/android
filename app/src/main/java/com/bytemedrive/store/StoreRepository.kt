package com.bytemedrive.store

import com.bytemedrive.network.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class StoreRepository(private val httpClient: HttpClient) {

    suspend fun getEncryptedEvents(usernameSha3: String, credentialsSha3: String, offset: Long): List<EncryptedEvent> =
        httpClient.create().get("customers/${usernameSha3}/events?offset=${offset}") {
            headers { append("Authorization", "Hash $credentialsSha3") }
        }.body()

    suspend fun storeEncryptedEvent(usernameSha3: String, credentialsSha3: String, encryptedEvent: EncryptedEvent) =
        httpClient.create().post("customers/${usernameSha3}/events") {
            headers { append("Authorization", "Hash $credentialsSha3") }
            setBody(encryptedEvent)
        }

}