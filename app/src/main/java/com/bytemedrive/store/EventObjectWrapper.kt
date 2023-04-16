package com.bytemedrive.store

import com.bytemedrive.network.JsonConfig.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.ZonedDateTime
import java.util.UUID

data class EventObjectWrapper(val id: UUID, val eventType: EventType, val publishedAt: ZonedDateTime, val data: Convertable) {

    fun toEventMapWrapper(): EventMapWrapper {
        val dataAsJson = mapper.writeValueAsString(data)
        val dataAsMap = mapper.readValue<Map<String, Any>>(dataAsJson)
        return EventMapWrapper(id, eventType, publishedAt, dataAsMap)
    }
}
