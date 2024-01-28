package com.bytemedrive.price

import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PricesRepository(
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getPrices(): Prices = withContext(ioDispatcher) {
        httpClient.get("prices").body()
    }
}