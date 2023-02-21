package com.bytemedrive.event

import kotlinx.serialization.Serializable

@Serializable
data class EventsResponse(val position: Int, val dataBase64: String)
