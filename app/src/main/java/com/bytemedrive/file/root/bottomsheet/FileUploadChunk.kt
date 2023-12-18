package com.bytemedrive.file.root.bottomsheet

import java.util.UUID

data class FileUploadChunk(
    val id: UUID,
    val viewId: UUID,
    val sizeBytes: Long
)
