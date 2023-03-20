package com.bytemedrive.event

import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class EventRepository(private val httpClient: HttpClient) {
    suspend fun fetch(idHashed: String): List<String> =
        httpClient.client.get(Endpoint.EVENTS.buildUrl(idHashed)).body()

    suspend fun upload(idHashed: String, body: EventsRequest) {
        httpClient.client.post(Endpoint.EVENTS.buildUrl(idHashed)) { setBody(body) }
    }
}