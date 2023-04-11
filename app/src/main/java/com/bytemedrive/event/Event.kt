package com.bytemedrive.event

import java.time.ZonedDateTime
import java.util.UUID

data class Event<T>(val id: UUID, val eventType: EventType, val publishedAt: ZonedDateTime, val data: T)
