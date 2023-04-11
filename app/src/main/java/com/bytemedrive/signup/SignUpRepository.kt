package com.bytemedrive.signup

import com.bytemedrive.network.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SignUpRepository(private val httpClient: HttpClient) {

    suspend fun signUp(usernameSha3: String, customerSignUp: CustomerSignUp) =
        httpClient.create().post("customers/${usernameSha3}") { setBody(customerSignUp) }

}