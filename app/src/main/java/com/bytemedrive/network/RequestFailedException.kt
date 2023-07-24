package com.bytemedrive.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

class RequestFailedException(val response: HttpResponse) : RuntimeException("Request to url ${response.request.url} failed with status ${response.status}.")