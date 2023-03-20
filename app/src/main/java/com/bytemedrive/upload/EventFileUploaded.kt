package com.bytemedrive.upload


data class EventFileUploaded(
    val id: String,
    val chunkIds: List<String>,
    val fileName: String,
    val fileSizeBytes: Long,
    val checksum: String,
    val password: CharArray,
    val contentType: String,
)
