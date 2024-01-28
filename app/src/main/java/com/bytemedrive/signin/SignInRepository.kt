package com.bytemedrive.signin

import com.bytemedrive.application.httpClient
import com.bytemedrive.store.EncryptedSecretKey
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SignInRepository(
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getPrivateKeys(usernameSha3: String, credentialsSha3: String): List<EncryptedSecretKey> = withContext(ioDispatcher) {
        httpClient.get("customers/${usernameSha3}/private-keys") {
            headers { append("Authorization", "Hash $credentialsSha3") }
        }.body()
    }
}