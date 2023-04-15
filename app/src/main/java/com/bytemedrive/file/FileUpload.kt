package com.bytemedrive.file

import java.util.UUID

data class FileUpload(val id: UUID, val dataBase64: String, val wallet: UUID)