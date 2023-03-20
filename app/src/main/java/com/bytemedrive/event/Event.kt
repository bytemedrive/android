package com.bytemedrive.event

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = EventSerializer::class)
data class Event<T>(val eventType: EventType, val data: T)
