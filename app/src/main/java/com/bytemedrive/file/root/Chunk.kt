package com.bytemedrive.file.root

import java.io.File
import java.util.UUID

data class Chunk(val id: UUID, val viewId: UUID, val file: File)
