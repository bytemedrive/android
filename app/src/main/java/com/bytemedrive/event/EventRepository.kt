package com.bytemedrive.event

import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.RestApiBuilder
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class EventRepository(private val restApiBuilder: RestApiBuilder) {
    suspend fun fetch(idHashed: String): List<EventsResponse> =
        restApiBuilder.client.get(Endpoint.EVENTS.buildUrl(idHashed)).body() // TODO: didnt find way ho to pass path params, seems only query params possible :(

    suspend fun upload(idHashed: String, body: EventsRequest) {
        restApiBuilder.client.post(Endpoint.EVENTS.buildUrl(idHashed)) { setBody(body) }
    }
}