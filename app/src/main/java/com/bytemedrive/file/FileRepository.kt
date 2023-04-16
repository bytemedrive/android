package com.bytemedrive.file

import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readBytes

class FileRepository {

    suspend fun upload(body: FileUpload) = httpClient.post("files") { setBody(body) }

    suspend fun download(id: String) = httpClient.get("files/$id") { header("Accept", "application/octet-stream") }.readBytes()
}