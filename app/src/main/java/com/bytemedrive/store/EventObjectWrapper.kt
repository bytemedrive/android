package com.bytemedrive.store

import com.fasterxml.jackson.module.kotlin.readValue
import java.time.ZonedDateTime
import java.util.UUID

data class EventObjectWrapper(val id: UUID, val eventType: EventType, val publishedAt: ZonedDateTime, val data: Convertable) {

    fun toEventMapWrapper(): EventMapWrapper {
        val dataAsJson = StoreJsonConfig.mapper.writeValueAsString(data)
        val dataAsMap = StoreJsonConfig.mapper.readValue<Map<String, Any>>(dataAsJson)
        return EventMapWrapper(id, eventType, publishedAt, dataAsMap)
    }
}
