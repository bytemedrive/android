package com.bytemedrive.file.root

import java.io.File
import java.util.UUID

data class FileUpload(val id: UUID, val fileViewId: UUID, val file: File)