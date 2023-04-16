package com.bytemedrive.signin

import com.bytemedrive.httpClient
import com.bytemedrive.store.EncryptedSecretKey
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers

class SignInRepository {

    suspend fun getPrivateKeys(usernameSha3: String, credentialsSha3: String): List<EncryptedSecretKey> =
        httpClient.get("customers/${usernameSha3}/private-keys") {
            headers { append("Authorization", "Hash $credentialsSha3") }
        }.body()
}