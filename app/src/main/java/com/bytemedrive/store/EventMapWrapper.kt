package com.bytemedrive.store

import java.time.ZonedDateTime
import java.util.UUID

data class EventMapWrapper(val id: UUID, val eventType: EventType, val publishedAt: ZonedDateTime, val data: Map<String, Any>)
