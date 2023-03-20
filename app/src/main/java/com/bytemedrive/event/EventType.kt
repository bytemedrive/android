package com.bytemedrive.event

import com.bytemedrive.upload.EventFileUploaded
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class EventType(name: String) {
    FILE_UPLOADED("file-uploaded")
}