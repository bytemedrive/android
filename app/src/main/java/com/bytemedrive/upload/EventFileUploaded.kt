package com.bytemedrive.upload

import java.util.UUID


data class EventFileUploaded(
    val id: UUID,
    val chunksIds: List<UUID>,
    val name: String,
    val sizeBytes: Long,
    val checksum: String,
    val password: CharArray,
    val contentType: String,
)
