package com.bytemedrive.file.root

import java.util.UUID

data class FileUpload(val fileId: UUID, val fileViewId: UUID, val dataBase64: String)