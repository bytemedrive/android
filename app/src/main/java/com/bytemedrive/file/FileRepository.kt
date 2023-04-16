package com.bytemedrive.file

import com.bytemedrive.application.httpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class FileRepository {

    suspend fun upload(body: FileUpload) = httpClient.post("files") { setBody(body) }
}