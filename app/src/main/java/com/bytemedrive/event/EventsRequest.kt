package com.bytemedrive.event

import kotlinx.serialization.Serializable


@Serializable
data class EventsRequest(val dataBase64: String)
