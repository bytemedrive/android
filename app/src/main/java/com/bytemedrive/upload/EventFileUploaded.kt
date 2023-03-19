package com.bytemedrive.upload

import com.bytemedrive.event.Event
import com.bytemedrive.event.EventType
import kotlinx.serialization.Serializable

@Serializable
data class EventFileUploaded(
    val id: String,
    val chunkIds: List<String>,
    val fileName: String,
    val fileSizeBytes: Long,
    val checksum: String,
    val password: CharArray,
    val contentType: String?,
) : Event(EventType.FILE_UPLOADED)
