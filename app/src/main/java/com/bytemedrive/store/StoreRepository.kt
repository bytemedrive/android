package com.bytemedrive.store

import android.util.Log
import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess

class StoreRepository {
    private val TAG = StoreRepository::class.qualifiedName

    suspend fun getEncryptedEvents(usernameSha3: String, credentialsSha3: String, offset: Long): List<EncryptedEvent> =
        httpClient.get("customers/$usernameSha3/events?offset=$offset") {
            headers { append("Authorization", "Hash $credentialsSha3") }
        }.body()

    suspend fun storeEncryptedEvent(usernameSha3: String, credentialsSha3: String, encryptedEvent: EncryptedEvent) =
        httpClient.post("customers/$usernameSha3/events") {
            headers { append("Authorization", "Hash $credentialsSha3") }
            setBody(encryptedEvent)
        }

    suspend fun deleteCustomer(usernameSha3: String, credentialsSha3: String): Boolean {
        Log.i(TAG, "Deleting customer $usernameSha3")

        return httpClient.delete("customers/$usernameSha3") {
            headers.append("Authorization", "Hash $credentialsSha3")
        }.status.isSuccess()
    }
}