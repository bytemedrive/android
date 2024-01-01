package com.bytemedrive.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey
    val id: UUID,
    val eventType: EventType,
    val publishedAt: ZonedDateTime,
    val dataJson: String
)
