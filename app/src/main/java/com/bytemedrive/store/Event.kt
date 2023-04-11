package com.bytemedrive.store

import java.time.ZonedDateTime
import java.util.UUID

data class Event(val id: UUID, val createdAt: ZonedDateTime, val event: Any)
