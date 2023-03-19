package com.bytemedrive.event

import kotlinx.serialization.Serializable

@Serializable
data class EventFileUploaded(
    val fileIds: List<String>,
    val fileName: String,
    val fileSizeBytes: Int,
    val checksum: String,
    val password: CharArray,
    val contentType: String?,
) : Event(EventType.FILE_UPLOADED)
