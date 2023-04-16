package com.bytemedrive.network

import com.bytemedrive.config.ConfigProperty
import com.bytemedrive.network.JsonConfig.mapper
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson

class HttpClient {

    val client = getHttpClient()

    private fun getHttpClient(): HttpClient {
        val client = HttpClient(OkHttp) {
            expectSuccess = true

            install(Logging) {
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(mapper))
            }

            defaultRequest {
                url(ConfigProperty.backendUrl)
                contentType(ContentType.Application.Json)
            }
        }

        return client
    }
}