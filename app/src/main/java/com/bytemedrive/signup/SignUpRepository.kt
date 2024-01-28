package com.bytemedrive.signup

import com.bytemedrive.application.httpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SignUpRepository(
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun signUp(usernameSha3: String, customerSignUp: CustomerSignUp) = withContext(ioDispatcher) {
        httpClient.post("customers/${usernameSha3}") { setBody(customerSignUp) }
    }
}