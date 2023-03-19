package com.bytemedrive.file

import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class FileRepository(private val httpClient: HttpClient) {
    suspend fun upload(body: FileUpload) = httpClient.client.post(Endpoint.FILES.url) { setBody(body) }
}