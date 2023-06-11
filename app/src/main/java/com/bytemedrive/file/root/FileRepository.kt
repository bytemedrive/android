package com.bytemedrive.file.root

import com.bytemedrive.application.httpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readBytes
import java.util.UUID

class FileRepository {

    suspend fun upload(body: FileUpload) = httpClient.post("/wallets/${body.wallet}/files") { setBody(body) }

    suspend fun download(id: UUID) = httpClient.get("files/$id") { header("Accept", "application/octet-stream") }.readBytes()

    suspend fun remove(walletId: UUID, id: UUID) = httpClient.delete("/wallets/$walletId/files/$id")
}