package com.bytemedrive.network

import com.bytemedrive.config.ConfigProperty
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClient() {
    val client = getHttpClient()

    private fun getHttpClient(): HttpClient {
        val client = HttpClient(OkHttp) {
            expectSuccess = true

            install(Logging) {
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            defaultRequest {
                url(ConfigProperty.backendUrl)
                contentType(ContentType.Application.Json)
            }
        }

        client.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)

            if (originalCall.response.status.value == 401) {
                execute(request)
            } else {
                originalCall
            }
        }

        return client
    }
}