package com.bytemedrive.store

import com.bytemedrive.event.EventType
import java.time.ZonedDateTime
import java.util.UUID

data class EventObjectWrapper(val id: UUID, val eventType: EventType, val publishedAt: ZonedDateTime, val data: Convertable)
