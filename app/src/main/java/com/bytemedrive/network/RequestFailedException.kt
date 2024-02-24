package com.bytemedrive.network

import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.http.HttpStatusCode

class RequestFailedException(val status: HttpStatusCode, val requestUrl: String, val errorResponse: ErrorResponse)
    : RuntimeException("Request failed. Url=$requestUrl. Status=$status. Response=$errorResponse") {

    data class ErrorResponse(val errorId: String, val message: String, val errorCode: ErrorCode?)

    enum class ErrorCode {

        WALLET_LOW_GBM;

        companion object {

            @JsonValue
            fun findByName(name: String) = ErrorCode.values().find { it.name == name }
        }
    }
}