package com.bytemedrive.network

import android.util.Log
import com.bytemedrive.application.networkStatus
import com.bytemedrive.application.sharedPreferences
import com.bytemedrive.network.JsonConfig.mapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.JacksonConverter

class HttpClient {

    private val TAG = HttpClient::class.qualifiedName

    val client = getHttpClient()

    private fun getHttpClient(): HttpClient {
        val client = HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }

            install(Logging) {
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(mapper))
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        val exception = RequestFailedException(response.status, response.request.url.toString(), response.body())

                        Log.e(TAG, exception.message.orEmpty())

                        throw exception
                    }
                }
            }

            defaultRequest {
                url(sharedPreferences.backendUrl)
                contentType(ContentType.Application.Json)
            }
        }

        client.plugin(HttpSend).intercept { request ->
            Log.i(TAG, "Sending request ${request.method.value} ${request.url}")
            val start = System.currentTimeMillis()

            if (!networkStatus.connected.value) {
                throw NoInternetException()
            }

            val originalCall = execute(request)

            Log.i(TAG, "Response[${originalCall.response.status.value}] in ${System.currentTimeMillis() - start}ms for request ${request.method.value} ${request.url}")
            originalCall
        }

        return client
    }
}