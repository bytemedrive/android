package com.bytemedrive.database

import java.util.UUID

data class FileUpload(val id: UUID, val name: String, val path: String, val folderId: UUID?)