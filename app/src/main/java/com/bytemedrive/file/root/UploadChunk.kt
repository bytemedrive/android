package com.bytemedrive.file.root

import java.util.UUID

data class UploadChunk(
    val id: UUID,
    val viewId: UUID,
    val sizeBytes: Long
)
