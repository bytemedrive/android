package com.bytemedrive.event

import kotlinx.serialization.Serializable

@Serializable
open class Event(val eventType: EventType)