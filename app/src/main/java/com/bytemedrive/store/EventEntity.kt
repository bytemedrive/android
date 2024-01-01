package com.bytemedrive.store

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "event_type") val eventType: EventType,
    @ColumnInfo(name = "published_at") val publishedAt: ZonedDateTime,
    @ColumnInfo(name = "data_json") val dataJson: String
)
