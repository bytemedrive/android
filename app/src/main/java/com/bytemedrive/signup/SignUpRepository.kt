package com.bytemedrive.signup

import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.HttpClient
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.EncryptedSecretKey
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class SignUpRepository(private val httpClient: HttpClient) {
    suspend fun getPublicKeys(username: String): List<EncryptedSecretKey> =
        httpClient.create().get(Endpoint.PUBLIC_KEYS.buildUrl(ShaService.hashSha3(username))).body()

    suspend fun signUp(username: String, customerSignUp: CustomerSignUp): HttpResponse =
        httpClient.create().post(Endpoint.CUSTOMER.buildUrl(ShaService.hashSha3(username))) { setBody(customerSignUp) }

}