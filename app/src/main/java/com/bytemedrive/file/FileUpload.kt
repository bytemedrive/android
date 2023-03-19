package com.bytemedrive.file

import kotlinx.serialization.Serializable

@Serializable
data class FileUpload(val id: String, val dataBase64: String)