package com.bytemedrive.file.root

import com.bytemedrive.application.httpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.streams.asInput
import java.util.UUID

class FileRepository {

    suspend fun upload(walletId: UUID, chunks: List<Chunk>) {
        chunks.forEach {
            httpClient.submitFormWithBinaryData(
                url = "wallets/$walletId/files",
                formData = formData {
                    append(
                        "data",
                        InputProvider(it.file.length()) { it.file.inputStream().asInput() },
                        Headers.build {
                            append(HttpHeaders.ContentType, "multipart/form-data")
                            append(HttpHeaders.ContentDisposition, "filename=${it.file.name}")
                        }
                    )
                    append("viewId", it.viewId.toString())
                    append("id", it.id.toString())
                }
            )
        }
    }

    suspend fun download(id: UUID) = httpClient.get("files/$id") { header("Accept", "application/octet-stream") }

    suspend fun remove(walletId: UUID, id: UUID) = httpClient.delete("wallets/$walletId/files/$id")
}