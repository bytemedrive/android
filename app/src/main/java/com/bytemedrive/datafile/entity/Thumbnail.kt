package com.bytemedrive.datafile.entity

import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.root.UploadChunk

data class Thumbnail(
    val resolution: Resolution,
    val chunks: List<UploadChunk>,
    val sizeBytes: Long,
    val contentType: String,
    val secretKeyBase64: String,
    val uploadStatus: UploadStatus
)
