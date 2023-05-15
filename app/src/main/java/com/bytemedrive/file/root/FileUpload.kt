package com.bytemedrive.file.root

import java.util.UUID

data class FileUpload(val id: UUID, val dataBase64: String, val wallet: UUID)