package com.bytemedrive.event

data class EventFileUploaded(
    val eventName: String,
    val id: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val checksum: String, // TODO: maybe different type?
    val symmetricKey: String,
    val contentType: String,
)
