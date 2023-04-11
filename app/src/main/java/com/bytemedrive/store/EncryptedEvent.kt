package com.bytemedrive.store

import java.time.ZonedDateTime
import java.util.UUID

data class EncryptedEvent(val id: UUID, val keys: List<UUID>, val eventDataBase64: String, val createdAt: ZonedDateTime)
