package com.bytemedrive.event

import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class EventRepository(private val httpClient: HttpClient) {
    suspend fun fetch(usernameSha3: String): List<String> =
        httpClient.create().get(Endpoint.EVENTS_WITH_OFFSET.buildUrl(usernameSha3)).body()

    suspend fun upload(usernameSha3: String, body: EventsRequest) {
        httpClient.client.post(Endpoint.EVENTS.buildUrl(usernameSha3)) { setBody(body) }
    }
}