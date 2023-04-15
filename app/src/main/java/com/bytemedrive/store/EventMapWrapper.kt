package com.bytemedrive.store

import java.time.ZonedDateTime
import java.util.UUID

data class EventMapWrapper(val id: UUID, val eventType: EventType, val publishedAt: ZonedDateTime, val data: Map<String, Any>) {

    fun toEventObjectWrapper(): EventObjectWrapper {
        val dataAsJson = StoreJsonConfig.mapper.writeValueAsString(data)
        val eventData = StoreJsonConfig.mapper.readValue(dataAsJson, eventType.clazz)
        return EventObjectWrapper(id, eventType, publishedAt, eventData as Convertable)
    }
}
