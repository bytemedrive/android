package com.bytemedrive.file.root

import android.util.Log
import com.bytemedrive.application.httpClient
import com.bytemedrive.network.RequestFailedException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID

class FileRepository(
    private val ioDispatcher: CoroutineDispatcher,
) {

    private val TAG = FileRepository::class.qualifiedName

    suspend fun upload(walletId: UUID, chunks: List<Chunk>) = withContext(ioDispatcher) {
        chunks.forEachIndexed { index, chunk ->
            Log.i(TAG, "Uploading chunk view id=${chunk.viewId} num=$index")
            httpClient.submitFormWithBinaryData(
                url = "wallets/$walletId/files",
                formData = formData {
                    append(
                        "data",
                        InputProvider(chunk.file.length()) { chunk.file.inputStream().asInput() },
                        Headers.build {
                            append(HttpHeaders.ContentType, "multipart/form-data")
                            append(HttpHeaders.ContentDisposition, "filename=${chunk.file.name}")
                        }
                    )
                    append("viewId", chunk.viewId.toString())
                    append("id", chunk.id.toString())
                }
            )
            Log.i(TAG, "Chunk view id=${chunk.viewId} was uploaded")
        }
    }

    suspend fun download(id: UUID): HttpResponse? = withContext(ioDispatcher) {
        try {
            httpClient.get("files/$id") { header("Accept", "application/octet-stream") }
        } catch (e: RequestFailedException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                Log.w(TAG, "File with id=$id not found")

                null
            } else {
                throw e
            }
        }
    }
}
