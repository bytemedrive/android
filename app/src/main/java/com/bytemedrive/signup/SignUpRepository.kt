package com.bytemedrive.signup

import com.bytemedrive.application.httpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SignUpRepository {

    suspend fun signUp(usernameSha3: String, customerSignUp: CustomerSignUp) = httpClient.post("customers/${usernameSha3}") { setBody(customerSignUp) }

}